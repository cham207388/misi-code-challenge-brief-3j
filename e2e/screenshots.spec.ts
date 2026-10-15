import { expect, test } from "@playwright/test";

test("capture login and dashboard screenshots", async ({ page }) => {
  await page.goto("/login");
  await expect(page.getByRole("heading", { name: "Staff sign in" })).toBeVisible();
  await page.screenshot({ path: "../docs/screenshots/login.png", fullPage: true });

  await page.getByLabel("Username").fill("coordinator");
  await page.getByLabel("Password").fill("Coordinator!2026");
  await page.getByRole("button", { name: "Sign in" }).click();
  await expect(page.getByRole("heading", { name: "Operations Dashboard" })).toBeVisible();
  await page.screenshot({ path: "../docs/screenshots/dashboard.png", fullPage: true });
});
