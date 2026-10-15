import type { InputHTMLAttributes } from "react";
import { cn } from "../../lib/cn";

export function Input({ className, ...props }: InputHTMLAttributes<HTMLInputElement>) {
  return (
    <input
      className={cn("mt-1 w-full rounded-lg border border-slate/30 bg-white px-3 py-2 text-sm", className)}
      {...props}
    />
  );
}
