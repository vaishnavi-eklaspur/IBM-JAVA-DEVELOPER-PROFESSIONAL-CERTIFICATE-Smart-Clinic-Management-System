import React from "react";

export default function AuthLayout({ children }) {
  return (
    <div className="min-h-screen bg-neutral-50 flex items-center justify-center px-4 py-10">
      <div className="w-full max-w-md rounded-lg border border-neutral-200 bg-white px-6 py-7 shadow-sm">
        {children}
      </div>
    </div>
  );
}
