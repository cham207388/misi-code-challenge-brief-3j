import { Navigate, Route, Routes } from "react-router";
import { AuthProvider } from "./lib/auth";
import { RequireAuth } from "./pages/AppShell";
import { DashboardPage } from "./pages/DashboardPage";
import { ForbiddenPage } from "./pages/ForbiddenPage";
import { LoginPage } from "./pages/LoginPage";
import { NotFoundPage } from "./pages/NotFoundPage";
import { SkeletonPage } from "./pages/SkeletonPage";

export function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/403" element={<ForbiddenPage />} />
        <Route element={<RequireAuth />}>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<DashboardPage />} />
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
        </Route>
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </AuthProvider>
  );
}
