import React from "react";

export default function PageContainer({ children }) {
  return (
    <div className="max-w-3xl mx-auto px-4 py-8 bg-white rounded-lg shadow-sm mt-8">
      {children}
    </div>
  );
}
