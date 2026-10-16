import { expect, type APIRequestContext } from "@playwright/test";

export async function csrfHeader(request: APIRequestContext): Promise<string> {
  const csrf = await request.get("/api/auth/csrf");
  expect(csrf.ok()).toBeTruthy();
  const body = (await csrf.json()) as { token?: string };
  if (body.token) {
    return body.token;
  }
  const cookies = await request.storageState();
  return cookies.cookies.find((cookie) => cookie.name === "XSRF-TOKEN")?.value ?? "";
}

export async function loginApi(request: APIRequestContext, username: string, password: string) {
  const token = await csrfHeader(request);
  const response = await request.post("/api/auth/login", {
    headers: { "Content-Type": "application/json", "X-XSRF-TOKEN": token },
    data: { username, password },
  });
  expect(response.ok(), await response.text()).toBeTruthy();
  const me = (await response.json()) as { username: string; sessionExpiresAt: string };
  expect(me.username).toBe(username);
  expect(me.sessionExpiresAt).toBeTruthy();
  await csrfHeader(request);
  return me;
}
