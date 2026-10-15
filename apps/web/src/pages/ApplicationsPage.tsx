import { Link } from "react-router";
import { useQuery } from "@tanstack/react-query";
import { api } from "../lib/api";
import type { ApplicationPage } from "../lib/applications";
import { ErrorBanner } from "../lib/errors";
import { useUrlState } from "../lib/use-url-state";
import { useAuth } from "../lib/auth";
import { Button } from "../components/ui/button";

const STATUSES = ["", "DRAFT", "SUBMITTED", "ELIGIBLE", "PENDING_APPROVAL", "APPROVED", "CANCELLED"];

export function ApplicationsPage() {
  const { hasCapability } = useAuth();
  const url = useUrlState();
  const query = useQuery({
    queryKey: ["applications", url.page, url.status],
    queryFn: () => {
      const params = new URLSearchParams({ page: String(url.page), size: "20" });
      if (url.status) {
        params.set("status", url.status);
      }
      return api<ApplicationPage>(`/api/applications?${params}`);
    },
  });

  return (
    <main id="main" className="px-4 py-8 sm:px-8">
      <p className="text-xs uppercase tracking-[0.18em] text-moss">Page 02</p>
      <div className="mt-2 flex flex-wrap items-end justify-between gap-4">
        <h1 className="font-serif text-3xl text-forest-deep">Application Register</h1>
        {hasCapability("APPLICATION_CREATE") ? (
          <Button asChild>
            <Link to="/applications/new/applicant">New application</Link>
          </Button>
        ) : null}
      </div>
      <form
        className="mt-6 flex flex-wrap items-end gap-3"
        onSubmit={(event) => {
          event.preventDefault();
          const data = new FormData(event.currentTarget);
          url.set({ status: String(data.get("status") ?? ""), page: 0 });
        }}
      >
        <label className="text-sm">
          Status
          <select
            name="status"
            defaultValue={url.status}
            className="mt-1 block rounded-lg border border-slate/30 bg-white px-3 py-2"
          >
            {STATUSES.map((status) => (
              <option key={status || "all"} value={status}>
                {status || "All open"}
              </option>
            ))}
          </select>
        </label>
        <Button type="submit" variant="secondary">
          Apply filters
        </Button>
        <Button type="button" variant="ghost" onClick={() => url.reset()}>
          Reset
        </Button>
      </form>
      {query.error ? <ErrorBanner error={query.error} onRetry={() => void query.refetch()} /> : null}
      <p className="sr-only" aria-live="polite">
        {query.data ? `${query.data.totalElements} applications` : "Loading applications"}
      </p>
      <div className="mt-6 hidden overflow-x-auto md:block">
        <table className="min-w-full text-left text-sm">
          <thead>
            <tr className="border-b border-forest/10">
              <th scope="col" aria-sort="descending">
                Reference
              </th>
              <th scope="col">Status</th>
              <th scope="col">Applicant</th>
              <th scope="col">Updated</th>
            </tr>
          </thead>
          <tbody>
            {(query.data?.content ?? []).map((row) => (
              <tr key={row.id} className="border-b border-forest/5">
                <td className="py-2">
                  <Link className="text-forest underline" to={`/applications/${row.id}`}>
                    {row.reference}
                  </Link>
                </td>
                <td>{row.status}</td>
                <td>{row.applicantName ?? "—"}</td>
                <td>{new Date(row.updatedAt).toLocaleString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <ul className="mt-6 space-y-3 md:hidden">
        {(query.data?.content ?? []).map((row) => (
          <li key={row.id} className="rounded-2xl border border-forest/10 bg-parchment p-4">
            <Link className="font-medium text-forest underline" to={`/applications/${row.id}`}>
              {row.reference}
            </Link>
            <p className="mt-1 text-sm text-slate">
              {row.status} · {row.applicantName ?? "Applicant hidden"}
            </p>
          </li>
        ))}
      </ul>
      <div className="mt-6 flex gap-3">
        <Button
          type="button"
          variant="secondary"
          disabled={url.page === 0}
          onClick={() => url.set({ page: Math.max(0, url.page - 1) })}
        >
          Previous
        </Button>
        <Button
          type="button"
          variant="secondary"
          disabled={(query.data?.page ?? 0) + 1 >= (query.data?.totalPages ?? 1)}
          onClick={() => url.set({ page: url.page + 1 })}
        >
          Next
        </Button>
      </div>
    </main>
  );
}
