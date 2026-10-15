import { expect, type APIRequestContext } from "@playwright/test";

export async function loginApi(request: APIRequestContext, username: string, password: string) {
  const csrf = await request.get("/api/auth/csrf");
  expect(csrf.ok()).toBeTruthy();
  const cookies = await request.storageState();
  const token = cookies.cookies.find((cookie) => cookie.name === "XSRF-TOKEN")?.value ?? "";
  const response = await request.post("/api/auth/login", {
    headers: { "Content-Type": "application/json", "X-XSRF-TOKEN": token },
    data: { username, password },
  });
  expect(response.ok()).toBeTruthy();
  return response.json() as Promise<{ username: string; sessionExpiresAt: string }>;
}

export async function csrfHeader(request: APIRequestContext): Promise<string> {
  const cookies = await request.storageState();
  return cookies.cookies.find((cookie) => cookie.name === "XSRF-TOKEN")?.value ?? "";
}
