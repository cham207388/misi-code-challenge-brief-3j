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
}: {
  error: unknown;
  id?: string;
  fallback?: string;
}) {
  const correlationId = error instanceof ApiError ? error.correlationId : "";
  return (
    <p id={id} role="alert" className="mt-4 rounded-md bg-clay/10 px-3 py-2 text-sm text-clay">
      {errorMessage(error, fallback)}
      {correlationId ? <span className="mt-1 block font-mono text-xs">Correlation ID: {correlationId}</span> : null}
    </p>
  );
}
