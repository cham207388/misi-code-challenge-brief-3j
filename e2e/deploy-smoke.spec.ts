import { expect, test } from "@playwright/test";

test("post-deploy smoke: health, login, dashboard and register", async ({ page, request }) => {
  const health = await request.get("/actuator/health/readiness");
  expect(health.ok()).toBeTruthy();

  await page.goto("/login");
  await page.getByLabel("Username").fill("coordinator");
  await page.getByLabel("Password").fill("Coordinator!2026");
  await page.getByRole("button", { name: "Sign in" }).click();
  await expect(page.getByRole("heading", { name: "Operations Dashboard" })).toBeVisible();

  await page.goto("/applications");
  await expect(page.getByRole("heading", { name: "Application Register" })).toBeVisible();
});
