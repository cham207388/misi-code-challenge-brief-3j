import { Navigate, useLocation, useNavigate } from "react-router";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { motion } from "motion/react";
import { ApiError, safeReturnUrl } from "../lib/api";
import { useAuth } from "../lib/auth";
import { ErrorBanner } from "../lib/errors";
import { Button } from "../components/ui/button";
import { Input } from "../components/ui/input";
import { Label } from "../components/ui/label";

const schema = z.object({
  username: z.string().min(1, "Username is required"),
  password: z.string().min(1, "Password is required"),
});

type Values = z.infer<typeof schema>;

export function LoginPage() {
  const { me, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const params = new URLSearchParams(location.search);
  const from = safeReturnUrl(params.get("returnUrl") ?? (location.state as { from?: string } | null)?.from ?? "/dashboard");
  const form = useForm<Values>({
    resolver: zodResolver(schema),
    defaultValues: { username: "", password: "" },
  });

  if (me) {
    return <Navigate to={from} replace />;
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
          onSubmit={form.handleSubmit(async (values) => {
            try {
              await login(values.username, values.password);
              navigate(from, { replace: true });
            } catch (cause) {
              form.setError("root", {
                message: cause instanceof ApiError ? cause.detail : "Unable to sign in",
              });
            }
          })}
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.25 }}
          className="w-full max-w-md rounded-2xl bg-parchment p-6 text-ink shadow-xl"
          aria-labelledby="login-heading"
          noValidate
        >
          <h2 id="login-heading" className="font-serif text-2xl">
            Staff sign in
          </h2>
          {params.get("signedOut") === "1" ? (
            <p role="status" className="mt-3 rounded-md bg-forest/10 px-3 py-2 text-sm text-forest">
              You have been signed out.
            </p>
          ) : null}
          {form.formState.errors.root || form.formState.errors.username || form.formState.errors.password ? (
            <div tabIndex={-1} className="mt-4 rounded-md bg-clay/10 px-3 py-2 text-sm text-clay" role="alert">
              {form.formState.errors.root?.message ?? "Enter a username and password."}
            </div>
          ) : null}
          <div className="mt-5 space-y-4">
            <Label htmlFor="username">
              Username
              <Input
                id="username"
                autoComplete="username"
                aria-invalid={Boolean(form.formState.errors.username || form.formState.errors.root)}
                aria-describedby={form.formState.errors.root ? "login-error" : undefined}
                {...form.register("username")}
              />
            </Label>
            <Label htmlFor="password">
              Password
              <Input
                id="password"
                type="password"
                autoComplete="current-password"
                aria-invalid={Boolean(form.formState.errors.password || form.formState.errors.root)}
                aria-describedby={form.formState.errors.root ? "login-error" : undefined}
                {...form.register("password")}
              />
            </Label>
          </div>
          {form.formState.errors.root ? (
            <ErrorBanner id="login-error" error={new ApiError(401, form.formState.errors.root.message ?? "Unable to sign in", "")} fallback="Unable to sign in" />
          ) : null}
          <Button type="submit" disabled={form.formState.isSubmitting} className="mt-6 w-full">
            {form.formState.isSubmitting ? "Signing in…" : "Sign in"}
          </Button>
        </motion.form>
      </div>
    </main>
  );
}
