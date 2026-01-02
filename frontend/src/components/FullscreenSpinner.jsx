import React from "react";

export default function FullscreenSpinner({ label = "Preparing your workspace..." }) {
  return (
    <div className="flex min-h-screen items-center justify-center bg-white">
      <div className="flex flex-col items-center gap-3">
        <span className="h-10 w-10 animate-spin rounded-full border-2 border-slate-200 border-t-blue-600" />
        <p className="text-sm text-slate-500">{label}</p>
      </div>
    </div>
  );
}
