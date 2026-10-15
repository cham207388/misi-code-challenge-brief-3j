import { useLayoutEffect, useState } from "react";
import { Navigate, Route, Routes, useLocation } from "react-router";
import { AuthProvider } from "./lib/auth";
import { RequireAuth, RequireCapability } from "./pages/AppShell";
import { DashboardPage } from "./pages/DashboardPage";
import { ForbiddenPage } from "./pages/ForbiddenPage";
import { LoginPage } from "./pages/LoginPage";
import { NotFoundPage } from "./pages/NotFoundPage";
import { SkeletonPage } from "./pages/SkeletonPage";

function RouteAnnouncer() {
  const location = useLocation();
  const [announcement, setAnnouncement] = useState("Memorial Event Operations");

  useLayoutEffect(() => {
    const heading = document.querySelector("h1");
    const label = heading?.textContent?.trim() || "Memorial Event Operations";
    document.title = `${label} · Memorial Event Operations`;
    setAnnouncement(label);
    if (heading instanceof HTMLElement) {
      heading.tabIndex = -1;
      heading.focus();
    }
  }, [location.pathname]);

  return (
    <div className="sr-only" aria-live="polite" aria-atomic="true">
      {announcement}
    </div>
  );
}

export function App() {
  return (
    <AuthProvider>
      <RouteAnnouncer />
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route element={<RequireAuth />}>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route
            path="/dashboard"
            element={
              <RequireCapability capability="DASHBOARD_VIEW">
                <DashboardPage />
              </RequireCapability>
            }
          />
          <Route path="/applications/new/:step" element={<SkeletonPage />} />
          <Route path="/applications/:id" element={<SkeletonPage />} />
          <Route path="/applications" element={<SkeletonPage />} />
          <Route path="/reviews" element={<SkeletonPage />} />
          <Route path="/information-requests" element={<SkeletonPage />} />
          <Route path="/calendar" element={<SkeletonPage />} />
          <Route path="/venues/availability" element={<SkeletonPage />} />
          <Route path="/resources" element={<SkeletonPage />} />
          <Route path="/approvals" element={<SkeletonPage />} />
          <Route path="/permits" element={<SkeletonPage />} />
          <Route path="/notifications" element={<SkeletonPage />} />
          <Route path="/roster" element={<SkeletonPage />} />
          <Route path="/check-in" element={<SkeletonPage />} />
          <Route path="/incidents" element={<SkeletonPage />} />
          <Route path="/reports" element={<SkeletonPage />} />
          <Route path="/admin/venues" element={<SkeletonPage />} />
          <Route path="/admin/users-rules" element={<SkeletonPage />} />
          <Route path="/admin/scenarios" element={<SkeletonPage />} />
          <Route path="/integrations/attempts/:id" element={<SkeletonPage />} />
          <Route path="/integrations/registry" element={<SkeletonPage />} />
          <Route path="/integrations/calendar" element={<SkeletonPage />} />
          <Route path="/integrations/messaging" element={<SkeletonPage />} />
          <Route path="/integrations/weather" element={<SkeletonPage />} />
          <Route path="/integrations/imports" element={<SkeletonPage />} />
          <Route path="/integrations/geo" element={<SkeletonPage />} />
          <Route path="/integrations" element={<SkeletonPage />} />
          <Route path="/documents" element={<SkeletonPage />} />
          <Route path="/403" element={<ForbiddenPage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Routes>
    </AuthProvider>
  );
}
