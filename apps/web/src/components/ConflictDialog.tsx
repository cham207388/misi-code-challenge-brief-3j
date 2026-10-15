import { Dialog } from "./ui/dialog";
import { Button } from "./ui/button";
import type { ApplicationDetail } from "../lib/applications";

export function ConflictDialog({
  open,
  mine,
  theirs,
  onReload,
  onKeepMine,
  onCancel,
}: {
  open: boolean;
  mine: Record<string, unknown> | null;
  theirs: ApplicationDetail | Record<string, unknown> | null;
  onReload: () => void;
  onKeepMine: () => void;
  onCancel: () => void;
}) {
  const fields = ["applicantName", "eventTitle", "status", "wizardStep", "version"] as const;
  return (
    <Dialog
      open={open}
      onOpenChange={(next) => {
        if (!next) {
          onCancel();
        }
      }}
      title="Someone else changed this record"
      description="Compare your draft with the server values, then reload theirs or keep yours and retry."
    >
      <div className="overflow-x-auto" aria-live="polite">
        <table className="w-full text-left text-sm">
          <thead>
            <tr>
              <th scope="col">Field</th>
              <th scope="col">Yours</th>
              <th scope="col">Theirs</th>
            </tr>
          </thead>
          <tbody>
            {fields.map((field) => (
              <tr key={field}>
                <th scope="row" className="pr-3 font-medium">
                  {field}
                </th>
                <td>{String(mine?.[field] ?? "—")}</td>
                <td>{String((theirs as Record<string, unknown> | null)?.[field] ?? "—")}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <div className="mt-6 flex flex-wrap gap-3">
        <Button type="button" onClick={onReload}>
          Reload theirs
        </Button>
        <Button type="button" variant="secondary" onClick={onKeepMine}>
          Keep mine and re-apply
        </Button>
        <Button type="button" variant="ghost" onClick={onCancel}>
          Cancel
        </Button>
      </div>
    </Dialog>
  );
}
