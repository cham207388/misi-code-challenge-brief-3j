import { expect, test } from "@playwright/test";
import { csrfHeader, loginApi } from "./helpers";

test("403 422 and 409 are demonstrable through the API", async ({ request, playwright, baseURL }) => {
  const me = await loginApi(request, "coordinator", "Coordinator!2026");
  expect(me.sessionExpiresAt).toBeTruthy();

  const created = await request.post("/api/applications", {
    headers: { "Content-Type": "application/json", "X-XSRF-TOKEN": await csrfHeader(request) },
    data: {},
  });
  expect(created.status()).toBe(201);
  const draft = (await created.json()) as { id: string; etag: string };

  const unknown = await request.post(`/api/applications/${draft.id}/transitions`, {
    headers: {
      "Content-Type": "application/json",
      "X-XSRF-TOKEN": await csrfHeader(request),
      "If-Match": draft.etag,
    },
    data: { target: "CLOSED" },
  });
  expect(unknown.status()).toBe(422);
  const invalid = (await unknown.json()) as { type: string; allowedTransitions: string[] };
  expect(invalid.type).toContain("invalid-transition");
  expect(invalid.allowedTransitions.length).toBeGreaterThan(0);

  const stale = await request.patch(`/api/applications/${draft.id}`, {
    headers: {
      "Content-Type": "application/json",
      "X-XSRF-TOKEN": await csrfHeader(request),
      "If-Match": "\"99\"",
    },
    data: { wizardStep: 2, version: 99 },
  });
  expect(stale.status()).toBe(409);
  const conflict = (await stale.json()) as { type: string; current: { id: string }; etag: string };
  expect(conflict.type).toContain("conflict");
  expect(conflict.current.id).toBe(draft.id);

  const scheduler = await playwright.request.newContext({ baseURL });
  await loginApi(scheduler, "scheduler", "Scheduler!2026");
  const hiddenDraft = await scheduler.get(`/api/applications/${draft.id}`);
  expect(hiddenDraft.status()).toBe(403);
  await scheduler.dispose();

  const admin = await playwright.request.newContext({ baseURL });
  await loginApi(admin, "admin", "Admin!2026");
  const adminGet = await admin.get(`/api/applications/${draft.id}`);
  expect(adminGet.status()).toBe(403);
  const approve = await admin.post(`/api/applications/${draft.id}/transitions`, {
    headers: {
      "Content-Type": "application/json",
      "X-XSRF-TOKEN": await csrfHeader(admin),
      "If-Match": "\"0\"",
    },
    data: { target: "APPROVED" },
  });
  expect(approve.status()).toBe(403);
  await admin.dispose();
});
