export class ApiError extends Error {
  constructor(
    public readonly status: number,
    public readonly detail: string,
    public readonly correlationId: string,
    public readonly type = "",
    public readonly allowedTransitions: string[] = [],
    public readonly current: Record<string, unknown> | null = null,
    public readonly version: number | null = null,
    public readonly etag = "",
    public readonly fieldErrors: string[] = [],
  ) {
    super(detail);
  }
}

let csrfToken: string | null = null;

function readCookie(name: string): string | null {
  const prefix = `${name}=`;
  const found = document.cookie
    .split(";")
    .map((part) => part.trim())
    .find((part) => part.startsWith(prefix));
  return found ? decodeURIComponent(found.slice(prefix.length)) : null;
}

function safeReturnUrl(pathname: string, search = ""): string {
  const candidate = `${pathname}${search}`;
  if (candidate.startsWith("/") && !candidate.startsWith("//")) {
    return candidate;
  }
  return "/dashboard";
}

function redirectToLogin(status: number, path: string) {
  if (status !== 401 || typeof window === "undefined") {
    return;
  }
  if (path.startsWith("/api/auth/") || path === "/api/me" || path.startsWith("/api/health")) {
    return;
  }
  if (window.location.pathname === "/login") {
    return;
  }
  const returnUrl = encodeURIComponent(safeReturnUrl(window.location.pathname, window.location.search));
  window.location.assign(`/login?returnUrl=${returnUrl}`);
}

export async function ensureCsrf(): Promise<string> {
  if (csrfToken) {
    return csrfToken;
  }
  const response = await fetch("/api/auth/csrf", { credentials: "include" });
  if (!response.ok) {
    throw new ApiError(response.status, "Unable to start a secure session", response.headers.get("X-Correlation-ID") ?? "");
  }
  csrfToken = readCookie("XSRF-TOKEN");
  if (!csrfToken) {
    const body = (await response.json()) as { token: string };
    csrfToken = body.token;
  }
  return csrfToken;
}

function asStringArray(value: unknown): string[] {
  return Array.isArray(value) ? value.filter((item): item is string => typeof item === "string") : [];
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
  const idempotency = headers.get("Idempotency-Key") ?? (init.method && init.method !== "GET" ? crypto.randomUUID() : null);
  if (idempotency && !headers.has("Idempotency-Key")) {
    headers.set("Idempotency-Key", idempotency);
  }
  let response: Response;
  try {
    response = await fetch(path, { ...init, headers, credentials: "include" });
  } catch {
    throw new ApiError(0, "Network error. Check your connection and retry.", "");
  }
  const correlationId = response.headers.get("X-Correlation-ID") ?? "";
  if (response.status === 204) {
    return undefined as T;
  }
  const text = await response.text();
  const payload = text ? (JSON.parse(text) as Record<string, unknown>) : {};
  if (!response.ok) {
    redirectToLogin(response.status, path);
    const detail = typeof payload.detail === "string" ? payload.detail : "Request failed";
    throw new ApiError(
      response.status,
      detail,
      typeof payload.correlationId === "string" ? payload.correlationId : correlationId,
      typeof payload.type === "string" ? payload.type : "",
      asStringArray(payload.allowedTransitions),
      payload.current && typeof payload.current === "object" ? (payload.current as Record<string, unknown>) : null,
      typeof payload.version === "number" ? payload.version : null,
      typeof payload.etag === "string" ? payload.etag : response.headers.get("ETag") ?? "",
      asStringArray(payload.errors),
    );
  }
  return payload as T;
}

export function clearCsrf() {
  csrfToken = null;
}

export { safeReturnUrl };
