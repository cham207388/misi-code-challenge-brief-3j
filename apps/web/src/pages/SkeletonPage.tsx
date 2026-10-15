import { useLocation } from "react-router";
import { motion } from "motion/react";
import { NAV_ITEMS, PAGE_COPY } from "../lib/nav";
import { useAuth } from "../lib/auth";
import { Navigate } from "react-router";

export function SkeletonPage() {
  const location = useLocation();
  const { hasCapability } = useAuth();
  const item =
    [...NAV_ITEMS]
      .sort((left, right) => right.path.length - left.path.length)
      .find((candidate) => location.pathname.startsWith(candidate.path)) ?? NAV_ITEMS[0];
  if (!hasCapability(item.capability)) {
    return <Navigate to="/403" replace />;
  }
  const copy = PAGE_COPY[item.path] ?? "This page is part of the Sprint 0 walking skeleton.";

  return (
    <main id="main" className="px-4 py-8 sm:px-8">
      <p className="text-xs uppercase tracking-[0.18em] text-moss">Page {item.number}</p>
      <h1 className="mt-2 font-serif text-3xl text-forest-deep">{item.title}</h1>
      <motion.section
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        className="mt-6 rounded-2xl border border-forest/10 bg-parchment p-6 shadow-sm"
      >
        <h2 className="font-medium text-forest">Sprint 0 skeleton</h2>
        <p className="mt-2 max-w-3xl text-sm leading-6 text-slate">{copy}</p>
        <p className="mt-4 text-sm text-slate">
          Routed navigation, session login and health checks are live. Domain CRUD, workflow and integrations arrive in
          later sprints.
        </p>
      </motion.section>
    </main>
  );
}
