export type ApplicationStatus =
  | "DRAFT"
  | "SUBMITTED"
  | "NEEDS_INFORMATION"
  | "ELIGIBLE"
  | "TENTATIVELY_RESERVED"
  | "PENDING_APPROVAL"
  | "APPROVED"
  | "PERMITTED"
  | "CHECKED_IN"
  | "EVENT_COMPLETED"
  | "CLOSED"
  | "REJECTED"
  | "CANCELLED";

export type ApplicationSummary = {
  id: string;
  reference: string;
  status: ApplicationStatus;
  applicantName: string | null;
  ownerId: string;
  cemeteryId: string | null;
  venueId: string | null;
  wizardStep: number;
  version: number;
  etag: string;
  createdAt: string;
  updatedAt: string;
};

export type ApplicationDetail = ApplicationSummary & {
  applicant: {
    id: string;
    fullName: string;
    email: string | null;
    phone: string | null;
    organization: string | null;
  } | null;
  event: {
    id: string;
    title: string | null;
    startsAt: string | null;
    endsAt: string | null;
    attendeeCount: number | null;
    notes: string | null;
  } | null;
  eventCategoryId: string | null;
  attachmentReady: boolean;
  rejectStage: string | null;
  lastReason: string | null;
  history: string[];
};

export type ApplicationPage = {
  content: ApplicationSummary[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type AvailableAction = {
  target: ApplicationStatus;
  enabled: boolean;
  reason: string;
};

export type LookupItem = { id: string; code: string; name: string };

export const WIZARD_STEPS = ["applicant", "event", "documents", "review"] as const;
export type WizardStep = (typeof WIZARD_STEPS)[number];

export function stepIndex(step: string | undefined): number {
  const index = WIZARD_STEPS.indexOf((step ?? "applicant") as WizardStep);
  return index >= 0 ? index : 0;
}
