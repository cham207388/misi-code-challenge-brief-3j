import { useMemo } from "react";
import { Link, Navigate, useNavigate, useParams, useSearchParams } from "react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { api, ApiError } from "../lib/api";
import type { ApplicationDetail, LookupItem, WizardStep } from "../lib/applications";
import { stepIndex, WIZARD_STEPS } from "../lib/applications";
import { ErrorBanner } from "../lib/errors";
import { Button } from "../components/ui/button";
import { Input } from "../components/ui/input";
import { Label } from "../components/ui/label";
import { ConflictDialog } from "../components/ConflictDialog";
import { useState } from "react";

const schema = z.object({
  applicantName: z.string().min(2, "Applicant name is required"),
  applicantEmail: z.string().email("Enter a valid email").or(z.literal("")),
  applicantPhone: z.string().max(40).optional(),
  organization: z.string().max(200).optional(),
  cemeteryId: z.string().uuid("Select a cemetery"),
  venueId: z.string().uuid("Select a venue"),
  eventCategoryId: z.string().uuid("Select a category"),
  eventTitle: z.string().min(2, "Event title is required"),
  startsAt: z.string().min(1, "Start time is required"),
  endsAt: z.string().min(1, "End time is required"),
  attendeeCount: z.number({ error: "At least one attendee" }).int().min(1, "At least one attendee"),
  notes: z.string().max(2000).optional(),
  attachmentReady: z.boolean(),
});

type FormValues = z.infer<typeof schema>;
const DRAFT_KEY = "memorial-ops.draft-id";

export function WizardPage() {
  const { step = "applicant" } = useParams();
  const [params, setParams] = useSearchParams();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [conflict, setConflict] = useState<ApiError | null>(null);
  const index = stepIndex(step);
  const validStep = WIZARD_STEPS.includes(step as WizardStep);

  const create = useMutation({
    mutationFn: () => api<ApplicationDetail>("/api/applications", { method: "POST", body: JSON.stringify({}) }),
    onSuccess: (detail) => {
      localStorage.setItem(DRAFT_KEY, detail.id);
      setParams({ id: detail.id }, { replace: true });
    },
  });

  const storedId = localStorage.getItem(DRAFT_KEY);
  const id = params.get("id") ?? storedId;

  const detail = useQuery({
    queryKey: ["application", id],
    enabled: Boolean(id),
    queryFn: () => api<ApplicationDetail>(`/api/applications/${id}`),
  });

  const cemeteries = useQuery({
    queryKey: ["lookups", "cemeteries"],
    queryFn: () => api<LookupItem[]>("/api/lookups/cemeteries"),
  });
  const venues = useQuery({
    queryKey: ["lookups", "venues"],
    queryFn: () => api<LookupItem[]>("/api/lookups/venues"),
  });
  const categories = useQuery({
    queryKey: ["lookups", "categories"],
    queryFn: () => api<LookupItem[]>("/api/lookups/event-categories"),
  });

  const defaults = useMemo<FormValues>(
    () => ({
      applicantName: detail.data?.applicant?.fullName ?? "",
      applicantEmail: detail.data?.applicant?.email ?? "",
      applicantPhone: detail.data?.applicant?.phone ?? "",
      organization: detail.data?.applicant?.organization ?? "",
      cemeteryId: detail.data?.cemeteryId ?? "",
      venueId: detail.data?.venueId ?? "",
      eventCategoryId: detail.data?.eventCategoryId ?? "",
      eventTitle: detail.data?.event?.title ?? "",
      startsAt: detail.data?.event?.startsAt?.slice(0, 16) ?? "",
      endsAt: detail.data?.event?.endsAt?.slice(0, 16) ?? "",
      attendeeCount: detail.data?.event?.attendeeCount ?? 1,
      notes: detail.data?.event?.notes ?? "",
      attachmentReady: detail.data?.attachmentReady ?? false,
    }),
    [detail.data],
  );

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    values: defaults,
  });

  const save = useMutation({
    mutationFn: (values: FormValues) =>
      api<ApplicationDetail>(`/api/applications/${id}`, {
        method: "PATCH",
        headers: { "If-Match": detail.data?.etag ?? `"${detail.data?.version ?? 0}"` },
        body: JSON.stringify({
          ...values,
          startsAt: values.startsAt ? new Date(values.startsAt).toISOString() : null,
          endsAt: values.endsAt ? new Date(values.endsAt).toISOString() : null,
          wizardStep: index + 1,
          version: detail.data?.version,
        }),
      }),
    onSuccess: (next) => {
      queryClient.setQueryData(["application", id], next);
    },
    onError: (error) => {
      if (error instanceof ApiError && error.status === 409) {
        setConflict(error);
      }
    },
  });

  const submit = useMutation({
    mutationFn: async () => {
      const saved = await save.mutateAsync(form.getValues());
      return api<ApplicationDetail>(`/api/applications/${id}/transitions`, {
        method: "POST",
        headers: { "If-Match": saved.etag },
        body: JSON.stringify({ target: "SUBMITTED", version: saved.version }),
      });
    },
    onSuccess: (next) => {
      localStorage.removeItem(DRAFT_KEY);
      void navigate(`/applications/${next.id}`);
    },
  });

  async function onNext(values: FormValues) {
    await save.mutateAsync(values);
    if (index < WIZARD_STEPS.length - 1) {
      void navigate(`/applications/new/${WIZARD_STEPS[index + 1]}?id=${id}`);
    }
  }

  const errors = Object.values(form.formState.errors)
    .map((error) => error?.message)
    .filter(Boolean);

  if (!validStep) {
    return <Navigate to="/applications/new/applicant" replace />;
  }

  return (
    <main id="main" className="px-4 py-8 sm:px-8">
      <p className="text-xs uppercase tracking-[0.18em] text-moss">Page 03</p>
      <h1 className="mt-2 font-serif text-3xl text-forest-deep">New Application</h1>
      <p className="mt-2 text-sm text-slate">
        Draft {detail.data?.reference ?? "is being created"} · resume stays on this URL.
      </p>
      <ol className="mt-4 flex flex-wrap gap-3 text-sm">
        {WIZARD_STEPS.map((item, itemIndex) => (
          <li key={item}>
            <Link
              to={`/applications/new/${item}${id ? `?id=${id}` : ""}`}
              className={itemIndex === index ? "font-semibold text-forest" : "text-slate underline"}
            >
              {itemIndex + 1}. {item}
            </Link>
          </li>
        ))}
      </ol>
      {!params.get("id") ? (
        <div className="mt-4 flex flex-wrap gap-3">
          <Button type="button" disabled={create.isPending} onClick={() => create.mutate()}>
            {create.isPending ? "Creating…" : "Start a new draft"}
          </Button>
          {storedId ? (
            <Button type="button" variant="secondary" onClick={() => setParams({ id: storedId }, { replace: true })}>
              Resume {storedId.slice(0, 8)}
            </Button>
          ) : null}
        </div>
      ) : null}
      {create.error || detail.error ? (
        <ErrorBanner error={create.error ?? detail.error} onRetry={() => (id ? void detail.refetch() : create.mutate())} />
      ) : null}
      {errors.length ? (
        <div tabIndex={-1} className="mt-4 rounded-md bg-clay/10 px-3 py-2 text-sm text-clay" role="alert">
          <p className="font-medium">Fix the following:</p>
          <ul className="mt-1 list-disc pl-5">
            {errors.map((message) => (
              <li key={message}>{message}</li>
            ))}
          </ul>
        </div>
      ) : null}
      <form className="mt-6 max-w-xl space-y-4" onSubmit={form.handleSubmit(onNext)} noValidate>
        {step === "applicant" ? (
          <>
            <Label htmlFor="applicantName">
              Applicant name
              <Input id="applicantName" aria-describedby={form.formState.errors.applicantName ? "applicantName-error" : undefined} {...form.register("applicantName")} />
            </Label>
            <Label htmlFor="applicantEmail">
              Email
              <Input id="applicantEmail" type="email" {...form.register("applicantEmail")} />
            </Label>
            <Label htmlFor="organization">
              Organization
              <Input id="organization" {...form.register("organization")} />
            </Label>
          </>
        ) : null}
        {step === "event" ? (
          <>
            <Label htmlFor="cemeteryId">
              Cemetery
              <select id="cemeteryId" className="mt-1 w-full rounded-lg border border-slate/30 bg-white px-3 py-2" {...form.register("cemeteryId")}>
                <option value="">Select…</option>
                {(cemeteries.data ?? []).map((item) => (
                  <option key={item.id} value={item.id}>
                    {item.name}
                  </option>
                ))}
              </select>
            </Label>
            <Label htmlFor="venueId">
              Venue
              <select id="venueId" className="mt-1 w-full rounded-lg border border-slate/30 bg-white px-3 py-2" {...form.register("venueId")}>
                <option value="">Select…</option>
                {(venues.data ?? []).map((item) => (
                  <option key={item.id} value={item.id}>
                    {item.name}
                  </option>
                ))}
              </select>
            </Label>
            <Label htmlFor="eventCategoryId">
              Category
              <select id="eventCategoryId" className="mt-1 w-full rounded-lg border border-slate/30 bg-white px-3 py-2" {...form.register("eventCategoryId")}>
                <option value="">Select…</option>
                {(categories.data ?? []).map((item) => (
                  <option key={item.id} value={item.id}>
                    {item.name}
                  </option>
                ))}
              </select>
            </Label>
            <Label htmlFor="eventTitle">
              Event title
              <Input id="eventTitle" {...form.register("eventTitle")} />
            </Label>
            <Label htmlFor="startsAt">
              Starts
              <Input id="startsAt" type="datetime-local" {...form.register("startsAt")} />
            </Label>
            <Label htmlFor="endsAt">
              Ends
              <Input id="endsAt" type="datetime-local" {...form.register("endsAt")} />
            </Label>
            <Label htmlFor="attendeeCount">
              Attendees
              <Input id="attendeeCount" type="number" min={1} {...form.register("attendeeCount", { valueAsNumber: true })} />
            </Label>
          </>
        ) : null}
        {step === "documents" ? (
          <label className="flex items-center gap-2 text-sm">
            <input type="checkbox" {...form.register("attachmentReady")} />
            Required attachment has been collected (checklist placeholder)
          </label>
        ) : null}
        {step === "review" ? (
          <section className="rounded-2xl border border-forest/10 bg-parchment p-4 text-sm">
            <p>
              {form.getValues("applicantName")} · {form.getValues("eventTitle")}
            </p>
            <p className="mt-2 text-slate">Submitting sends the case to eligibility review.</p>
          </section>
        ) : null}
        {save.error && save.error instanceof ApiError && save.error.status !== 409 ? <ErrorBanner error={save.error} /> : null}
        {submit.error ? <ErrorBanner error={submit.error} /> : null}
        <div className="flex flex-wrap gap-3">
          {index > 0 ? (
            <Button type="button" variant="secondary" onClick={() => void navigate(`/applications/new/${WIZARD_STEPS[index - 1]}?id=${id}`)}>
              Back
            </Button>
          ) : null}
          {step === "review" ? (
            <Button type="button" disabled={submit.isPending} onClick={() => void submit.mutate()}>
              {submit.isPending ? "Submitting…" : "Submit application"}
            </Button>
          ) : (
            <Button type="submit" disabled={save.isPending}>
              {save.isPending ? "Saving…" : "Save and continue"}
            </Button>
          )}
        </div>
      </form>
      <ConflictDialog
        open={Boolean(conflict)}
        mine={form.getValues() as unknown as Record<string, unknown>}
        theirs={conflict?.current ?? null}
        onCancel={() => setConflict(null)}
        onReload={() => {
          setConflict(null);
          void detail.refetch();
        }}
        onKeepMine={() => {
          setConflict(null);
          if (conflict?.current && typeof conflict.current.version === "number") {
            void save.mutateAsync({ ...form.getValues() });
          }
        }}
      />
    </main>
  );
}
