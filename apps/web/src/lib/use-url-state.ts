import { useMemo } from "react";
import { useSearchParams } from "react-router";

export function useUrlState() {
  const [params, setParams] = useSearchParams();
  return useMemo(() => {
    const page = Number(params.get("page") ?? "0") || 0;
    const status = params.get("status") ?? "";
    const q = params.get("q") ?? "";
    const sort = params.get("sort") ?? "createdAt,desc";
    return {
      page,
      status,
      q,
      sort,
      set: (next: { page?: number; status?: string; q?: string; sort?: string }) => {
        const updated = new URLSearchParams(params);
        if (next.page !== undefined) {
          updated.set("page", String(next.page));
        }
        if (next.status !== undefined) {
          if (next.status) {
            updated.set("status", next.status);
          } else {
            updated.delete("status");
          }
        }
        if (next.q !== undefined) {
          if (next.q) {
            updated.set("q", next.q);
          } else {
            updated.delete("q");
          }
        }
        if (next.sort !== undefined) {
          updated.set("sort", next.sort);
        }
        setParams(updated, { replace: true });
      },
      reset: () => setParams(new URLSearchParams(), { replace: true }),
    };
  }, [params, setParams]);
}
