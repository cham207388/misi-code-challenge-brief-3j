import { act, render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { SessionExpiryToast } from "./SessionExpiryToast";

describe("SessionExpiryToast", () => {
  it("warns two minutes before expiry", async () => {
    vi.useFakeTimers();
    const now = Date.parse("2026-10-15T12:00:00.000Z");
    vi.setSystemTime(now);
    const onStaySignedIn = vi.fn().mockResolvedValue(undefined);
    const onSignOut = vi.fn().mockResolvedValue(undefined);
    render(
      <SessionExpiryToast
        expiresAt="2026-10-15T12:02:01.000Z"
        onStaySignedIn={onStaySignedIn}
        onSignOut={onSignOut}
      />,
    );
    expect(screen.queryByText("Your session expires in two minutes.")).not.toBeInTheDocument();
    await act(async () => {
      await vi.advanceTimersByTimeAsync(1_200);
    });
    expect(screen.getByRole("status")).toHaveTextContent("Your session expires in two minutes.");
    vi.useRealTimers();
  });
});
