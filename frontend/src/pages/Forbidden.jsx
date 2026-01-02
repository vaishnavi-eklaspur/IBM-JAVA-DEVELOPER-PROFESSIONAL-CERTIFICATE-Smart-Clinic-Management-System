import React from "react";
import Navbar from "../components/Navbar";
import PageContainer from "../layout/PageContainer";

export default function Forbidden() {
  return (
    <div className="min-h-screen bg-neutral-50">
      <Navbar />
      <PageContainer>
        <div className="rounded-2xl border border-rose-100 bg-white/90 p-8 text-center shadow-sm">
          <p className="text-xs uppercase tracking-[0.4em] text-rose-400">Error 403</p>
          <h1 className="mt-3 text-3xl font-semibold text-neutral-900">Access restricted</h1>
          <p className="mt-4 text-sm text-neutral-600">
            You are signed in, but this workspace does not match your assigned role. Please use the navigation above to go
            back to your dashboard or sign out and switch roles.
          </p>
        </div>
      </PageContainer>
    </div>
  );
}
