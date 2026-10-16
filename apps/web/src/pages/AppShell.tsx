import { NavLink, Navigate, Outlet, useLocation } from "react-router";
import { useState, type ReactNode } from "react";
import { Menu, Trees, X } from "lucide-react";
import { useAuth } from "../lib/auth";
import { NAV_ITEMS } from "../lib/nav";

export function RequireAuth() {
  const { me, loading } = useAuth();
  const location = useLocation();
  if (loading) {
    return <div className="p-8 text-slate">Loading session…</div>;
  }
  if (!me) {
    const returnUrl = encodeURIComponent(`${location.pathname}${location.search}`);
    return <Navigate to={`/login?returnUrl=${returnUrl}`} replace />;
  }
  return <AppShell />;
}

export function RequireCapability({
  capability,
  children,
}: {
  capability: string;
  children: ReactNode;
}) {
  const { hasCapability, loading } = useAuth();
  if (loading) {
    return <div className="p-8 text-slate">Loading session…</div>;
  }
  if (!hasCapability(capability)) {
    return <Navigate to="/403" replace />;
  }
  return children;
}

function AppShell() {
  const { me, logout, hasCapability } = useAuth();
  const [open, setOpen] = useState(false);
  const items = NAV_ITEMS.filter((item) => hasCapability(item.capability));
  const groups = [...new Set(items.map((item) => item.group))];

  return (
    <div className="min-h-screen bg-stone">
      <a href="#main" className="sr-only focus:not-sr-only focus:absolute focus:left-4 focus:top-4 focus:bg-parchment focus:px-3 focus:py-2">
        Skip to content
      </a>
      <header className="sticky top-0 z-20 flex items-center justify-between border-b border-forest/10 bg-parchment px-4 py-3 lg:hidden">
        <div className="flex items-center gap-2 font-serif text-lg">
          <Trees className="h-5 w-5 text-forest" aria-hidden="true" />
          Memorial Ops
        </div>
        <button type="button" className="rounded-md p-2" aria-expanded={open} aria-controls="app-nav" onClick={() => setOpen((value) => !value)}>
          {open ? <X className="h-5 w-5" aria-hidden="true" /> : <Menu className="h-5 w-5" aria-hidden="true" />}
          <span className="sr-only">Toggle navigation</span>
        </button>
      </header>
      <div className="lg:grid lg:grid-cols-[280px_1fr]">
        <nav
          id="app-nav"
          aria-label="Primary"
          className={`${open ? "block" : "hidden"} border-r border-forest/10 bg-forest-deep text-parchment lg:block lg:min-h-screen`}
        >
          <div className="hidden items-center gap-2 px-5 py-6 font-serif text-xl lg:flex">
            <Trees className="h-6 w-6 text-gold" aria-hidden="true" />
            Memorial Ops
          </div>
          <p className="px-5 pb-4 text-xs uppercase tracking-[0.18em] text-gold">{me?.displayName}</p>
          <div className="space-y-5 px-3 pb-8">
            {groups.map((group) => (
              <div key={group}>
                <p className="px-2 text-[11px] uppercase tracking-[0.16em] text-stone/70">{group}</p>
                <ul className="mt-1 space-y-0.5">
                  {items
                    .filter((item) => item.group === group)
                    .map((item) => (
                      <li key={item.path}>
                        <NavLink
                          to={item.path}
                          onClick={() => setOpen(false)}
                          className={({ isActive }) =>
                            `block rounded-lg px-2 py-2 text-sm ${isActive ? "bg-forest text-parchment" : "text-stone/90 hover:bg-forest/70"}`
                          }
                        >
                          <span className="mr-2 font-mono text-[11px] text-gold">{item.number}</span>
                          {item.title}
                        </NavLink>
                      </li>
                    ))}
                </ul>
              </div>
            ))}
          </div>
          <div className="px-5 pb-8 text-sm">
            <p className="text-stone/70">{me?.roles.join(", ")}</p>
            <button
              type="button"
              className="mt-3 rounded-md border border-gold/40 px-3 py-1.5 text-gold"
              onClick={() => void logout("/login?signedOut=1")}
            >
              Sign out
            </button>
          </div>
        </nav>
        <div>
          <Outlet />
        </div>
      </div>
    </div>
  );
}
