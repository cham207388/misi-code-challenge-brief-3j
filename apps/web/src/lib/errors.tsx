import { useState } from "react";
import { ApiError } from "./api";

export function errorMessage(error: unknown, fallback = "Request failed"): string {
  if (error instanceof ApiError) {
    return error.detail;
  }
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return fallback;
}

export function ErrorBanner({
  error,
  id,
  fallback = "Request failed",
  onRetry,
}: {
  error: unknown;
  id?: string;
  fallback?: string;
  onRetry?: () => void;
}) {
  const apiError = error instanceof ApiError ? error : null;
  const [copied, setCopied] = useState(false);
  const correlationId = apiError?.correlationId ?? "";

  async function copy() {
    if (!correlationId) {
      return;
    }
    await navigator.clipboard.writeText(correlationId);
    setCopied(true);
  }

  return (
    <div id={id} role="alert" className="mt-4 rounded-md bg-clay/10 px-3 py-2 text-sm text-clay">
      <p>{errorMessage(error, fallback)}</p>
      {apiError?.allowedTransitions.length ? (
        <p className="mt-2 text-xs">Allowed next actions: {apiError.allowedTransitions.join(", ")}</p>
      ) : null}
      {correlationId ? (
        <p className="mt-2 flex flex-wrap items-center gap-2 font-mono text-xs">
          Correlation ID: {correlationId}
          <button type="button" className="rounded border border-clay/40 px-2 py-0.5" onClick={() => void copy()}>
            {copied ? "Copied" : "Copy"}
          </button>
        </p>
      ) : null}
      {onRetry || apiError?.status === 0 ? (
        <button type="button" className="mt-2 underline" onClick={onRetry}>
          Retry
        </button>
      ) : null}
    </div>
  );
}
