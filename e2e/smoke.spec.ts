import { expect, test } from "@playwright/test";

test("health, login, dashboard and deep-link refresh", async ({ page, request }) => {
  const health = await request.get("/api/health");
  expect(health.ok()).toBeTruthy();
  const body = (await health.json()) as { status: string; components: { db: string } };
  expect(body.status).toBe("UP");
  expect(body.components.db).toBe("UP");

  await page.goto("/login");
  await expect(page.getByRole("heading", { name: "Staff sign in" })).toBeVisible();
  await page.getByLabel("Username").fill("coordinator");
  await page.getByLabel("Password").fill("Coordinator!2026");
  await page.getByRole("button", { name: "Sign in" }).click();
  await expect(page.getByRole("heading", { name: "Operations Dashboard" })).toBeVisible();

  await page.goto("/applications");
  await expect(page.getByRole("heading", { name: "Application Register" })).toBeVisible();
  await page.reload();
  await expect(page.getByRole("heading", { name: "Application Register" })).toBeVisible();
});
