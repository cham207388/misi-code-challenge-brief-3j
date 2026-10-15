import { useState, type FormEvent } from "react";
import { Navigate, useLocation, useNavigate } from "react-router";
import { motion } from "motion/react";
import { ApiError, safeReturnUrl } from "../lib/api";
import { useAuth } from "../lib/auth";
import { ErrorBanner } from "../lib/errors";

export function LoginPage() {
  const { me, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const params = new URLSearchParams(location.search);
  const from = safeReturnUrl(params.get("returnUrl") ?? (location.state as { from?: string } | null)?.from ?? "/dashboard");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<unknown>(null);
  const [pending, setPending] = useState(false);
  const invalid = Boolean(error);

  if (me) {
    return <Navigate to={from} replace />;
  }

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setPending(true);
    setError(null);
    try {
      await login(username, password);
      navigate(from, { replace: true });
    } catch (cause) {
      setError(cause instanceof ApiError ? cause : new Error("Unable to sign in"));
    } finally {
      setPending(false);
    }
  }

  return (
    <main className="min-h-screen bg-[radial-gradient(circle_at_top,_#1b4d3e,_#12352b_55%,_#0c241d)] px-4 py-12 text-stone">
      <div className="mx-auto flex min-h-[80vh] max-w-5xl flex-col justify-center gap-10 lg:flex-row lg:items-center">
        <section className="max-w-lg text-parchment">
          <p className="text-sm uppercase tracking-[0.2em] text-gold">Willowmere Memorial District</p>
          <h1 className="mt-3 font-serif text-4xl leading-tight text-parchment">Memorial Event Operations</h1>
          <p className="mt-4 max-w-md text-sm leading-6 text-stone/90">
            Internal permit and case management for fictional cemeteries, venues and ceremonies. Sign in with your
            assigned staff account. Demo credentials are documented in the project README, not on this page.
          </p>
        </section>
        <motion.form
          onSubmit={onSubmit}
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.25 }}
          className="w-full max-w-md rounded-2xl bg-parchment p-6 text-ink shadow-xl"
          aria-labelledby="login-heading"
        >
          <h2 id="login-heading" className="font-serif text-2xl">
            Staff sign in
          </h2>
          <div className="mt-5 space-y-4">
            <label className="block text-sm font-medium" htmlFor="username">
              Username
              <input
                id="username"
                className="mt-1 w-full rounded-lg border border-slate/30 bg-white px-3 py-2"
                name="username"
                autoComplete="username"
                value={username}
                onChange={(event) => setUsername(event.target.value)}
                required
                aria-invalid={invalid}
                aria-describedby={invalid ? "login-error" : undefined}
              />
            </label>
            <label className="block text-sm font-medium" htmlFor="password">
              Password
              <input
                id="password"
                className="mt-1 w-full rounded-lg border border-slate/30 bg-white px-3 py-2"
                type="password"
                name="password"
                autoComplete="current-password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                required
                aria-invalid={invalid}
                aria-describedby={invalid ? "login-error" : undefined}
              />
            </label>
          </div>
          {error ? <ErrorBanner id="login-error" error={error} fallback="Unable to sign in" /> : null}
          <button
            type="submit"
            disabled={pending}
            className="mt-6 w-full rounded-lg bg-forest px-4 py-2.5 font-medium text-parchment disabled:opacity-60"
          >
            {pending ? "Signing in…" : "Sign in"}
          </button>
        </motion.form>
      </div>
    </main>
  );
}
