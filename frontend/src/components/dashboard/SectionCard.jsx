import React from "react";

const toneStyles = {
  slate: {
    border: "border-slate-200",
    header: "bg-gradient-to-r from-slate-50 via-white to-white",
  },
  sky: {
    border: "border-sky-100",
    header: "bg-gradient-to-r from-sky-50 via-white to-white",
  },
  emerald: {
    border: "border-emerald-100",
    header: "bg-gradient-to-r from-emerald-50 via-white to-white",
  },
  amber: {
    border: "border-amber-100",
    header: "bg-gradient-to-r from-amber-50 via-white to-white",
  },
};

export default function SectionCard({
  eyebrow,
  title,
  description,
  actions,
  tone = "slate",
  className = "",
  children,
}) {
  const styles = toneStyles[tone] || toneStyles.slate;

  return (
    <section
      className={`rounded-2xl border ${styles.border} bg-white/95 shadow-sm shadow-slate-100/60 backdrop-blur ${className}`}
    >
      <div className={`rounded-t-2xl border-b ${styles.border} px-6 py-4 ${styles.header}`}>
        <div className="flex items-start justify-between gap-4">
          <div>
            {eyebrow && <p className="text-xs uppercase tracking-[0.2em] text-slate-500">{eyebrow}</p>}
            <h2 className="text-lg font-semibold text-slate-900">{title}</h2>
            {description && <p className="text-sm text-slate-600">{description}</p>}
          </div>
          {actions && <div className="flex flex-shrink-0 gap-2">{actions}</div>}
        </div>
      </div>
      <div className="px-6 py-5">{children}</div>
    </section>
  );
}
