import { Link } from "react-router";

export function NotFoundPage() {
  return (
    <main id="main" className="px-6 py-16">
      <h1 className="font-serif text-3xl text-forest-deep">Page not found</h1>
      <p className="mt-3 text-slate">That route is not part of Memorial Event Operations.</p>
      <Link to="/dashboard" className="mt-6 inline-block text-forest underline">
        Return to the dashboard
      </Link>
    </main>
  );
}
