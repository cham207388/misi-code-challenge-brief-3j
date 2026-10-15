import { Link } from "react-router";
import { useQuery } from "@tanstack/react-query";
import { api } from "../lib/api";
import { useAuth } from "../lib/auth";

type Health = { status: string; components: Record<string, string> };

export function DashboardPage() {
  const { me } = useAuth();
  const health = useQuery({
    queryKey: ["health"],
    queryFn: () => api<Health>("/api/health"),
  });

  return (
    <main id="main" className="px-4 py-8 sm:px-8">
      <p className="text-xs uppercase tracking-[0.18em] text-moss">Page 01</p>
      <h1 className="mt-2 font-serif text-3xl text-forest-deep">Operations Dashboard</h1>
      <p className="mt-2 max-w-2xl text-sm text-slate">
        Welcome, {me?.displayName}. Calculated KPI cards arrive with the reporting slice. This skeleton shows live
        platform health so the walking demo is more than a static landing page.
      </p>
      <section className="mt-6 grid gap-4 md:grid-cols-3">
        <article className="rounded-2xl border border-forest/10 bg-parchment p-5">
          <h2 className="text-sm uppercase tracking-wide text-moss">Application health</h2>
          <p className="mt-3 font-serif text-3xl text-forest">{health.data?.status ?? "…"}</p>
          <p className="mt-1 text-sm text-slate">From `/api/health`</p>
        </article>
        <article className="rounded-2xl border border-forest/10 bg-parchment p-5">
          <h2 className="text-sm uppercase tracking-wide text-moss">Database</h2>
          <p className="mt-3 font-serif text-3xl text-forest">{health.data?.components.db ?? "…"}</p>
          <p className="mt-1 text-sm text-slate">Flyway baseline applied</p>
        </article>
        <article className="rounded-2xl border border-forest/10 bg-parchment p-5">
          <h2 className="text-sm uppercase tracking-wide text-moss">Open a queue</h2>
          <Link to="/applications" className="mt-3 inline-block text-forest underline">
            Application register
          </Link>
        </article>
      </section>
    </main>
  );
}
