import * as DialogPrimitive from "@radix-ui/react-dialog";
import type { ReactNode } from "react";
import { cn } from "../../lib/cn";

export function Dialog({
  open,
  onOpenChange,
  title,
  children,
  description,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  title: string;
  description?: string;
  children: ReactNode;
}) {
  return (
    <DialogPrimitive.Root open={open} onOpenChange={onOpenChange}>
      <DialogPrimitive.Portal>
        <DialogPrimitive.Overlay className="fixed inset-0 z-40 bg-ink/40" />
        <DialogPrimitive.Content
          className={cn(
            "fixed left-1/2 top-1/2 z-50 w-[min(36rem,calc(100vw-2rem))] -translate-x-1/2 -translate-y-1/2 rounded-2xl bg-parchment p-6 shadow-xl",
          )}
          aria-describedby={description ? "dialog-description" : undefined}
        >
          <DialogPrimitive.Title className="font-serif text-2xl text-forest-deep">{title}</DialogPrimitive.Title>
          {description ? (
            <DialogPrimitive.Description id="dialog-description" className="mt-2 text-sm text-slate">
              {description}
            </DialogPrimitive.Description>
          ) : null}
          <div className="mt-4">{children}</div>
        </DialogPrimitive.Content>
      </DialogPrimitive.Portal>
    </DialogPrimitive.Root>
  );
}
