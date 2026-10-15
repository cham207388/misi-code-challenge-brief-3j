import { useState } from "react";
import { Link, useParams } from "react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api, ApiError } from "../lib/api";
import type { ApplicationDetail, AvailableAction } from "../lib/applications";
import { ErrorBanner } from "../lib/errors";
import { Button } from "../components/ui/button";
import { ConflictDialog } from "../components/ConflictDialog";

export function ApplicationDetailPage() {
  const { id = "" } = useParams();
  const queryClient = useQueryClient();
  const [reason, setReason] = useState("");
  const [conflict, setConflict] = useState<ApiError | null>(null);
  const detail = useQuery({
    queryKey: ["application", id],
    queryFn: () => api<ApplicationDetail>(`/api/applications/${id}`),
  });
  const actions = useQuery({
    queryKey: ["application-actions", id],
    queryFn: () => api<AvailableAction[]>(`/api/applications/${id}/actions`),
  });
  const transition = useMutation({
    mutationFn: (target: string) =>
      api<ApplicationDetail>(`/api/applications/${id}/transitions`, {
        method: "POST",
        headers: { "If-Match": detail.data?.etag ?? "" },
        body: JSON.stringify({ target, reason: reason || null, version: detail.data?.version }),
      }),
    onSuccess: (next) => {
      queryClient.setQueryData(["application", id], next);
      void actions.refetch();
    },
    onError: (error) => {
      if (error instanceof ApiError && error.status === 409) {
        setConflict(error);
      }
    },
  });

  if (detail.error instanceof ApiError && detail.error.status === 404) {
    return (
      <main id="main" className="px-6 py-16">
        <h1 className="font-serif text-3xl text-forest-deep">Application not found</h1>
        <Link to="/applications" className="mt-4 inline-block text-forest underline">
          Back to the register
        </Link>
      </main>
    );
  }

  return (
    <main id="main" className="px-4 py-8 sm:px-8">
      <p className="text-xs uppercase tracking-[0.18em] text-moss">Page 04</p>
      <h1 className="mt-2 font-serif text-3xl text-forest-deep">{detail.data?.reference ?? "Application"}</h1>
      <p className="mt-2 text-sm text-slate">{detail.data?.status}</p>
      {detail.error ? <ErrorBanner error={detail.error} onRetry={() => void detail.refetch()} /> : null}
      {transition.error && !(transition.error instanceof ApiError && transition.error.status === 409) ? (
        <ErrorBanner error={transition.error} />
      ) : null}
      <section className="mt-6 grid gap-4 md:grid-cols-2">
        <article className="rounded-2xl border border-forest/10 bg-parchment p-5">
          <h2 className="font-medium text-forest">Applicant</h2>
          <p className="mt-2 text-sm">{detail.data?.applicant?.fullName ?? "—"}</p>
          <p className="text-sm text-slate">{detail.data?.applicant?.email ?? ""}</p>
        </article>
        <article className="rounded-2xl border border-forest/10 bg-parchment p-5">
          <h2 className="font-medium text-forest">Event</h2>
          <p className="mt-2 text-sm">{detail.data?.event?.title ?? "—"}</p>
          <p className="text-sm text-slate">
            {detail.data?.event?.startsAt ? new Date(detail.data.event.startsAt).toLocaleString() : "Schedule pending"}
          </p>
        </article>
      </section>
      <section className="mt-6 rounded-2xl border border-forest/10 bg-parchment p-5">
        <h2 className="font-medium text-forest">Available actions</h2>
        <label className="mt-3 block text-sm">
          Reason (required for reject/cancel)
          <input
            className="mt-1 w-full rounded-lg border border-slate/30 px-3 py-2"
            value={reason}
            onChange={(event) => setReason(event.target.value)}
          />
        </label>
        <div className="mt-4 flex flex-wrap gap-2">
          {(actions.data ?? []).map((action) => (
            <Button
              key={action.target}
              type="button"
              variant={action.enabled ? "primary" : "secondary"}
              disabled={!action.enabled || transition.isPending}
              title={action.reason}
              onClick={() => transition.mutate(action.target)}
            >
              {action.target}
            </Button>
          ))}
        </div>
      </section>
      <ConflictDialog
        open={Boolean(conflict)}
        mine={{ status: detail.data?.status, version: detail.data?.version }}
        theirs={conflict?.current ?? null}
        onCancel={() => setConflict(null)}
        onReload={() => {
          setConflict(null);
          void detail.refetch();
        }}
        onKeepMine={() => {
          const target = (conflict?.current as { status?: string } | null)?.status;
          setConflict(null);
          if (target) {
            transition.mutate(String(detail.data?.status));
          }
        }}
      />
    </main>
  );
}
