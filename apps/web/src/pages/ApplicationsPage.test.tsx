import { render, screen } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router";
import { describe, expect, it, vi } from "vitest";
import { ApplicationsPage } from "./ApplicationsPage";

vi.mock("../lib/auth", () => ({
  useAuth: () => ({
    hasCapability: (capability: string) => capability === "APPLICATION_CREATE",
  }),
}));

vi.mock("../lib/api", () => ({
  api: async () => ({ content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 }),
}));

describe("ApplicationsPage", () => {
  it("renders the register heading and new application action", () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    render(
      <QueryClientProvider client={client}>
        <MemoryRouter>
          <ApplicationsPage />
        </MemoryRouter>
      </QueryClientProvider>,
    );
    expect(screen.getByRole("heading", { name: "Application Register" })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "New application" })).toBeInTheDocument();
  });
});
