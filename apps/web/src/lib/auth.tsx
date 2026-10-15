import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import { api, clearCsrf, ensureCsrf } from "./api";
import { SessionExpiryToast } from "../components/SessionExpiryToast";

export type Me = {
  id: string;
  username: string;
  displayName: string;
  roles: string[];
  capabilities: string[];
  sessionExpiresAt?: string | null;
};

type AuthState = {
  me: Me | null;
  loading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: (redirectTo?: string) => Promise<void>;
  refresh: () => Promise<void>;
  hasCapability: (capability: string) => boolean;
};

const AuthContext = createContext<AuthState | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [me, setMe] = useState<Me | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    ensureCsrf()
      .then(() => api<Me>("/api/me"))
      .then((user) => {
        if (!cancelled) {
          setMe(user);
        }
      })
      .catch(() => {
        if (!cancelled) {
          setMe(null);
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const value = useMemo<AuthState>(
    () => ({
      me,
      loading,
      login: async (username, password) => {
        await ensureCsrf();
        const user = await api<Me>("/api/auth/login", {
          method: "POST",
          body: JSON.stringify({ username, password }),
        });
        setMe(user);
      },
      logout: async (redirectTo?: string) => {
        try {
          await api<void>("/api/auth/logout", { method: "POST" });
        } catch {
          // still clear the local session
        }
        clearCsrf();
        if (redirectTo) {
          window.location.assign(redirectTo);
          return;
        }
        setMe(null);
      },
      refresh: async () => {
        const user = await api<Me>("/api/me");
        setMe(user);
      },
      hasCapability: (capability) => Boolean(me?.capabilities.includes(capability)),
    }),
    [me, loading],
  );

  return (
    <AuthContext.Provider value={value}>
      {children}
      <SessionExpiryToast
        expiresAt={me?.sessionExpiresAt}
        onStaySignedIn={value.refresh}
        onSignOut={() => value.logout("/login?signedOut=1")}
      />
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}
