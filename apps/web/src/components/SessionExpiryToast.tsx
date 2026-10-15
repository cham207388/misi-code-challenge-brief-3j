import { useEffect, useState } from "react";

const WARN_MS = 2 * 60 * 1000;

export function SessionExpiryToast({
  expiresAt,
  onStaySignedIn,
  onSignOut,
}: {
  expiresAt?: string | null;
  onStaySignedIn: () => Promise<void>;
  onSignOut: () => Promise<void>;
}) {
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    if (!expiresAt) {
      setVisible(false);
      return;
    }
    const expires = Date.parse(expiresAt);
    if (Number.isNaN(expires)) {
      return;
    }
    const warnDelay = Math.max(0, expires - WARN_MS - Date.now());
    const expireDelay = Math.max(0, expires - Date.now());
    const warnTimer = window.setTimeout(() => setVisible(true), warnDelay);
    const expireTimer = window.setTimeout(() => {
      void onSignOut();
    }, expireDelay);
    return () => {
      window.clearTimeout(warnTimer);
      window.clearTimeout(expireTimer);
    };
  }, [expiresAt, onSignOut]);

  if (!visible) {
    return null;
  }

  return (
    <div
      role="status"
      aria-live="polite"
      className="fixed bottom-4 right-4 z-50 max-w-sm rounded-2xl border border-gold/40 bg-parchment p-4 text-ink shadow-xl"
    >
      <p className="font-medium text-forest-deep">Your session expires in two minutes.</p>
      <p className="mt-1 text-sm text-slate">Stay signed in to keep working, or sign out now.</p>
      <div className="mt-3 flex flex-wrap gap-2">
        <button
          type="button"
          className="rounded-md bg-forest px-3 py-1.5 text-sm text-parchment"
          onClick={() => {
            setVisible(false);
            void onStaySignedIn();
          }}
        >
          Stay signed in
        </button>
        <button type="button" className="rounded-md border border-forest/30 px-3 py-1.5 text-sm" onClick={() => void onSignOut()}>
          Sign out
        </button>
      </div>
    </div>
  );
}
