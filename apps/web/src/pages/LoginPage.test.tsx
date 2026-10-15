import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router";
import { describe, expect, it, vi } from "vitest";
import { LoginPage } from "./LoginPage";

vi.mock("../lib/auth", () => ({
  useAuth: () => ({
    me: null,
    loading: false,
    login: vi.fn(),
    logout: vi.fn(),
    hasCapability: () => false,
  }),
}));

describe("LoginPage", () => {
  it("renders an accessible sign-in form without demo passwords", () => {
    render(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>,
    );
    expect(screen.getByRole("heading", { name: "Staff sign in" })).toBeInTheDocument();
    expect(screen.getByLabelText("Username")).toBeInTheDocument();
    expect(screen.getByLabelText("Password")).toBeInTheDocument();
    expect(screen.queryByText(/Coordinator!2026/)).not.toBeInTheDocument();
  });
});
