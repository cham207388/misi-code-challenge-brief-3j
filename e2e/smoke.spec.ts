import AxeBuilder from "@axe-core/playwright";
import { expect, test } from "@playwright/test";

test("health, login, dashboard and deep-link refresh", async ({ page, request }) => {
  const health = await request.get("/api/health");
  expect(health.ok()).toBeTruthy();
  const body = (await health.json()) as { status: string; components: { db: string } };
  expect(body.status).toBe("UP");
  expect(body.components.db).toBe("UP");

  await page.goto("/login");
  await expect(page.getByRole("heading", { name: "Staff sign in" })).toBeVisible();
  const loginAxe = await new AxeBuilder({ page }).withTags(["wcag2a", "wcag2aa"]).analyze();
  expect(loginAxe.violations).toEqual([]);

  await page.getByLabel("Username").fill("coordinator");
  await page.getByLabel("Password").fill("Coordinator!2026");
  await page.getByRole("button", { name: "Sign in" }).click();
  await expect(page.getByRole("heading", { name: "Operations Dashboard" })).toBeVisible();

  const dashboardAxe = await new AxeBuilder({ page }).withTags(["wcag2a", "wcag2aa"]).analyze();
  expect(dashboardAxe.violations).toEqual([]);

  await page.goto("/applications");
  await expect(page.getByRole("heading", { name: "Application Register" })).toBeVisible();
  await page.reload();
  await expect(page.getByRole("heading", { name: "Application Register" })).toBeVisible();

  await page.goto("/applications/new/applicant");
  await expect(page.getByRole("heading", { name: "New Application" })).toBeVisible();
  await page.getByRole("button", { name: "Start a new draft" }).click();
  await expect(page.getByText(/Draft APP-/)).toBeVisible();

  await page.getByRole("button", { name: "Sign out" }).click();
  await expect(page.getByText("You have been signed out.")).toBeVisible();

  await page.getByLabel("Username").fill("attendant1");
  await page.getByLabel("Password").fill("Attendant!2026");
  await page.getByRole("button", { name: "Sign in" }).click();
  await page.goto("/applications");
  await expect(page.getByRole("heading", { name: "Access denied" })).toBeVisible();
});
