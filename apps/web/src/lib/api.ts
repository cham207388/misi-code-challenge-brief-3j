export class ApiError extends Error {
  constructor(
    public readonly status: number,
    public readonly detail: string,
    public readonly correlationId: string,
  ) {
    super(detail);
  }
}

let csrfToken: string | null = null;

export async function ensureCsrf(): Promise<string> {
  if (csrfToken) {
    return csrfToken;
  }
  const response = await fetch("/api/auth/csrf", { credentials: "include" });
  if (!response.ok) {
    throw new ApiError(response.status, "Unable to start a secure session", response.headers.get("X-Correlation-ID") ?? "");
  }
  const body = (await response.json()) as { token: string };
  csrfToken = body.token;
  return csrfToken;
}

export async function api<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers);
  headers.set("Accept", "application/json");
  if (init.body && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }
  if (init.method && init.method !== "GET") {
    headers.set("X-XSRF-TOKEN", await ensureCsrf());
  }
  const response = await fetch(path, { ...init, headers, credentials: "include" });
  const correlationId = response.headers.get("X-Correlation-ID") ?? "";
  if (response.status === 204) {
    return undefined as T;
  }
  const text = await response.text();
  const payload = text ? (JSON.parse(text) as Record<string, unknown>) : {};
  if (!response.ok) {
    const detail = typeof payload.detail === "string" ? payload.detail : "Request failed";
    throw new ApiError(response.status, detail, correlationId);
  }
  return payload as T;
}

export function clearCsrf() {
  csrfToken = null;
}
