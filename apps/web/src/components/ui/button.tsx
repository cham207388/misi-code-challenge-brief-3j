import { Slot } from "@radix-ui/react-slot";
import type { ButtonHTMLAttributes } from "react";
import { cn } from "../../lib/cn";

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: "primary" | "secondary" | "ghost";
  asChild?: boolean;
};

export function Button({ className, variant = "primary", asChild = false, ...props }: ButtonProps) {
  const Comp = asChild ? Slot : "button";
  return (
    <Comp
      className={cn(
        "inline-flex items-center justify-center rounded-lg px-4 py-2.5 text-sm font-medium disabled:opacity-60",
        variant === "primary" && "bg-forest text-parchment",
        variant === "secondary" && "border border-forest/20 bg-white text-forest",
        variant === "ghost" && "text-forest underline",
        className,
      )}
      {...props}
    />
  );
}
