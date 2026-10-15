export function ForbiddenPage() {
  return (
    <main id="main" className="px-6 py-16">
      <h1 className="font-serif text-3xl text-forest-deep">Access denied</h1>
      <p className="mt-3 max-w-xl text-slate">
        Your role does not include this page. No case or applicant details are shown.
      </p>
    </main>
  );
}
