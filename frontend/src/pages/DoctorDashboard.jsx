import React, { useMemo, useState } from "react";
import Navbar from "../components/Navbar";
import SectionCard from "../components/dashboard/SectionCard";
import { useApiResource } from "../hooks/useApiResource";
import {
  cancelAppointment,
  completeAppointment,
  createPrescription,
  fetchDoctorAppointments,
  fetchDoctorAvailability,
  fetchDoctorPrescriptions,
  fetchDoctorProfile,
} from "../services/doctor";
import { formatDate, formatDateTime } from "../utils/formatters";

function InlineLoader({ label }) {
  return <p className="text-sm text-slate-500">{label}</p>;
}

function ErrorBanner({ message }) {
  return (
    <div className="rounded-lg border border-rose-100 bg-rose-50 px-3 py-2 text-sm text-rose-700">
      {message}
    </div>
  );
}

function FeedbackBanner({ state }) {
  if (!state) {
    return null;
  }
  const base = "rounded-lg px-3 py-2 text-sm";
  if (state.type === "error") {
    return <div className={`${base} border border-rose-100 bg-rose-50 text-rose-700`}>{state.message}</div>;
  }
  return <div className={`${base} border border-emerald-100 bg-emerald-50 text-emerald-700`}>{state.message}</div>;
}

const ledgerStatusStyles = {
  BOOKED: "bg-sky-100 text-sky-800",
  COMPLETED: "bg-emerald-100 text-emerald-800",
  CANCELLED: "bg-rose-100 text-rose-800",
};

function StatusBadge({ status }) {
  if (!status) {
    return null;
  }
  const classes = ledgerStatusStyles[status] || "bg-slate-100 text-slate-600";
  return <span className={`rounded-full px-2.5 py-0.5 text-xs font-medium ${classes}`}>{status}</span>;
}

function resolvePatientLabel(appointment) {
  if (!appointment) {
    return "";
  }
  return (
    appointment.patientName ||
    appointment.patient?.name ||
    (appointment.patientId ? `Patient #${appointment.patientId}` : "Patient")
  );
}

function PrescriptionCard({ prescription }) {
  return (
    <div className="rounded-xl border border-slate-200 bg-white/80 p-4 shadow-sm">
      <div className="flex items-center justify-between">
        <p className="text-sm text-slate-500">Prescription #{prescription.id}</p>
        <span className="text-xs font-medium text-slate-500">Appointment #{prescription.appointmentId}</span>
      </div>
      <p className="mt-3 text-sm text-slate-700">{prescription.notes}</p>
    </div>
  );
}

export default function DoctorDashboard() {
  const [selectedDate, setSelectedDate] = useState(() => new Date().toISOString().slice(0, 10));
  const [activeActionId, setActiveActionId] = useState(null);
  const [actionFeedback, setActionFeedback] = useState(null);
  const [selectedPrescription, setSelectedPrescription] = useState(null);
  const [prescriptionNotes, setPrescriptionNotes] = useState("");
  const [prescriptionFeedback, setPrescriptionFeedback] = useState(null);
  const [prescriptionLoading, setPrescriptionLoading] = useState(false);

  const {
    data: profile,
    loading: profileLoading,
    error: profileError,
  } = useApiResource(fetchDoctorProfile);

  const doctorId = profile?.id;

  const {
    data: availability,
    loading: availabilityLoading,
    error: availabilityError,
  } = useApiResource(() => fetchDoctorAvailability(doctorId, selectedDate), {
    dependencies: [doctorId, selectedDate],
    enabled: Boolean(doctorId && selectedDate),
  });

  const {
    data: prescriptions,
    loading: prescriptionsLoading,
    error: prescriptionsError,
    refresh: refreshPrescriptions,
  } = useApiResource(fetchDoctorPrescriptions);

  const {
    data: appointments,
    loading: appointmentsLoading,
    error: appointmentsError,
    refresh: refreshAppointments,
  } = useApiResource(fetchDoctorAppointments);

  const prescriptionStats = useMemo(() => {
    if (!prescriptions) {
      return { total: 0 };
    }
    return { total: prescriptions.length };
  }, [prescriptions]);

  const sortedAppointments = useMemo(() => {
    if (!appointments) {
      return [];
    }
    return [...appointments].sort(
      (a, b) => new Date(a.appointmentTime).getTime() - new Date(b.appointmentTime).getTime()
    );
  }, [appointments]);

  const handleAppointmentAction = async (appointmentId, action) => {
    setActiveActionId(`${appointmentId}-${action}`);
    setActionFeedback(null);
    const executor = action === "complete" ? completeAppointment : cancelAppointment;
    try {
      await executor(appointmentId);
      setActionFeedback({ type: "success", message: `Appointment #${appointmentId} updated.` });
      refreshAppointments();
      refreshPrescriptions();
    } catch (error) {
      setActionFeedback({ type: "error", message: error.message });
    } finally {
      setActiveActionId(null);
    }
  };

  const handleSelectForPrescription = (appointment) => {
    setSelectedPrescription({
      appointmentId: appointment.id,
      patientName: resolvePatientLabel(appointment),
      time: appointment.appointmentTime,
    });
    setPrescriptionNotes("");
    setPrescriptionFeedback(null);
  };

  const handlePrescriptionSubmit = async (event) => {
    event.preventDefault();
    if (!selectedPrescription?.appointmentId || !prescriptionNotes.trim()) {
      setPrescriptionFeedback({
        type: "error",
        message: "Select a completed appointment and include therapy notes.",
      });
      return;
    }
    setPrescriptionLoading(true);
    setPrescriptionFeedback(null);
    try {
      await createPrescription({
        appointmentId: selectedPrescription.appointmentId,
        notes: prescriptionNotes.trim(),
      });
      setPrescriptionFeedback({ type: "success", message: "Prescription issued successfully." });
      setSelectedPrescription(null);
      setPrescriptionNotes("");
      refreshPrescriptions();
      refreshAppointments();
    } catch (error) {
      setPrescriptionFeedback({ type: "error", message: error.message });
    } finally {
      setPrescriptionLoading(false);
    }
  };

  const handleClearPrescription = () => {
    setSelectedPrescription(null);
    setPrescriptionNotes("");
    setPrescriptionFeedback(null);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-white to-emerald-50">
      <Navbar />
      <div className="mx-auto max-w-6xl px-6 py-10 space-y-8">
        <header className="space-y-2">
          <p className="text-sm uppercase tracking-[0.3em] text-slate-500">Doctor Workspace</p>
          <h1 className="text-3xl font-semibold text-slate-900">Clinical command center</h1>
          <p className="text-base text-slate-600">
            Review your profile, monitor capacity, close out appointments, and keep prescriptions current.
          </p>
        </header>

        <div className="grid gap-6 lg:grid-cols-3">
          <SectionCard
            tone="emerald"
            eyebrow="Profile"
            title="Clinician details"
            description="From the secure provider registry"
            className="lg:col-span-1"
          >
            {profileLoading && <InlineLoader label="Loading profile..." />}
            {profileError && <ErrorBanner message={profileError.message} />}
            {profile && (
              <dl className="space-y-3 text-sm text-slate-700">
                <div>
                  <dt className="text-slate-500">Name</dt>
                  <dd className="text-slate-900">{profile.name}</dd>
                </div>
                <div>
                  <dt className="text-slate-500">Email</dt>
                  <dd className="text-slate-900">{profile.email}</dd>
                </div>
                <div>
                  <dt className="text-slate-500">Speciality</dt>
                  <dd className="text-slate-900">{profile.speciality || "General"}</dd>
                </div>
                <div>
                  <dt className="text-slate-500">Doctor ID</dt>
                  <dd className="text-slate-900">{profile.id}</dd>
                </div>
              </dl>
            )}
          </SectionCard>

          <SectionCard
            tone="sky"
            eyebrow="Capacity"
            title="Daily availability"
            description="Open slots pulled live from scheduling"
            className="lg:col-span-2"
          >
            <div className="flex flex-wrap items-center gap-4">
              <label className="text-sm text-slate-600">
                Date
                <input
                  type="date"
                  value={selectedDate}
                  onChange={(event) => setSelectedDate(event.target.value)}
                  className="ml-2 rounded border border-slate-200 px-2 py-1 text-sm"
                />
              </label>
              {profile && (
                <p className="text-sm text-slate-500">Doctor #{profile.id}</p>
              )}
            </div>
            <div className="mt-4">
              {availabilityLoading && <InlineLoader label="Loading availability..." />}
              {availabilityError && <ErrorBanner message={availabilityError.message} />}
              {!availabilityLoading && !availabilityError && (!availability || availability.length === 0) && (
                <p className="text-sm text-slate-600">All slots are booked for {formatDate(selectedDate)}.</p>
              )}
              <div className="mt-4 grid gap-3 sm:grid-cols-2">
                {availability?.map((slot, index) => (
                  <div
                    key={`${slot}-${index}`}
                    className="rounded-xl border border-slate-200 bg-white/80 px-3 py-3 text-sm font-medium text-slate-800"
                  >
                    {formatDate(selectedDate)} · {slot}
                  </div>
                ))}
              </div>
            </div>
          </SectionCard>
        </div>

        <div className="grid gap-6 lg:grid-cols-3">
          <SectionCard
            tone="slate"
            eyebrow="Cases"
            title="Recent prescriptions"
            description="Completed appointments generating therapy"
            className="lg:col-span-2"
          >
            {prescriptionsLoading && <InlineLoader label="Loading prescriptions..." />}
            {prescriptionsError && <ErrorBanner message={prescriptionsError.message} />}
            {!prescriptionsLoading && !prescriptionsError && prescriptions?.length === 0 && (
              <p className="text-sm text-slate-600">No prescriptions issued yet.</p>
            )}
            <div className="mt-4 grid gap-4">
              {prescriptions?.map((item) => (
                <PrescriptionCard key={item.id} prescription={item} />
              ))}
            </div>
          </SectionCard>

          <SectionCard
            tone="amber"
            eyebrow="Appointments"
            title="Action center"
            description="Live ledger of your visits"
            className="lg:col-span-1"
          >
            {appointmentsLoading && <InlineLoader label="Loading appointments..." />}
            {appointmentsError && <ErrorBanner message={appointmentsError.message} />}
            {!appointmentsLoading && !appointmentsError && sortedAppointments.length === 0 && (
              <p className="text-sm text-slate-600">No appointments have been scheduled yet.</p>
            )}
            <div className="mt-4 space-y-4">
              {sortedAppointments.map((appointment) => {
                const canComplete = appointment.status === "BOOKED";
                const canCancel = appointment.status === "BOOKED";
                const canPrescribe = appointment.status === "COMPLETED";
                const completeBusy = activeActionId === `${appointment.id}-complete`;
                const cancelBusy = activeActionId === `${appointment.id}-cancel`;
                return (
                  <div
                    key={appointment.id}
                    className="rounded-xl border border-slate-200 bg-white/80 p-4 shadow-sm"
                  >
                    <div className="flex flex-wrap items-start justify-between gap-3">
                      <div>
                        <p className="text-xs uppercase tracking-widest text-slate-500">
                          Appointment #{appointment.id}
                        </p>
                        <p className="text-base font-semibold text-slate-900">
                          {formatDateTime(appointment.appointmentTime)}
                        </p>
                        <p className="text-sm text-slate-600">{resolvePatientLabel(appointment)}</p>
                      </div>
                      <div className="text-right">
                        <StatusBadge status={appointment.status} />
                        {appointment.reason && (
                          <p className="mt-2 text-xs text-slate-500">{appointment.reason}</p>
                        )}
                      </div>
                    </div>
                    <div className="mt-4 flex flex-wrap gap-2">
                      <button
                        type="button"
                        disabled={!canComplete || completeBusy}
                        onClick={() => handleAppointmentAction(appointment.id, "complete")}
                        className="rounded-full bg-emerald-600 px-3 py-1.5 text-xs font-semibold text-white disabled:opacity-50"
                      >
                        {completeBusy ? "Updating..." : "Mark complete"}
                      </button>
                      <button
                        type="button"
                        disabled={!canCancel || cancelBusy}
                        onClick={() => handleAppointmentAction(appointment.id, "cancel")}
                        className="rounded-full bg-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-700 disabled:opacity-50"
                      >
                        {cancelBusy ? "Updating..." : "Cancel"}
                      </button>
                      <button
                        type="button"
                        disabled={!canPrescribe}
                        onClick={() => handleSelectForPrescription(appointment)}
                        className="rounded-full border border-slate-300 px-3 py-1.5 text-xs font-semibold text-slate-700 disabled:opacity-40"
                      >
                        Prepare prescription
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
            <div className="mt-4">
              <FeedbackBanner state={actionFeedback} />
            </div>
          </SectionCard>
        </div>

        <div className="grid gap-6 lg:grid-cols-3">
          <SectionCard
            tone="emerald"
            eyebrow="New prescription"
            title="Issue therapy"
            description="Requires a completed appointment reference"
            className="lg:col-span-1"
          >
            <div className="space-y-4">
              {selectedPrescription ? (
                <div className="rounded-xl border border-emerald-100 bg-emerald-50/60 p-3 text-sm text-emerald-900">
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <p className="font-semibold">Appointment #{selectedPrescription.appointmentId}</p>
                      <p className="text-xs uppercase tracking-widest">
                        {selectedPrescription.patientName}
                      </p>
                      {selectedPrescription.time && (
                        <p className="text-xs text-emerald-800/70">
                          {formatDateTime(selectedPrescription.time)}
                        </p>
                      )}
                    </div>
                    <button
                      type="button"
                      onClick={handleClearPrescription}
                      className="text-xs font-medium text-emerald-700"
                    >
                      Clear
                    </button>
                  </div>
                </div>
              ) : (
                <p className="text-sm text-slate-600">
                  Select a completed appointment from the action center to enable prescribing.
                </p>
              )}
              <form className="space-y-3" onSubmit={handlePrescriptionSubmit}>
                <div className="flex flex-col gap-1">
                  <label className="text-sm font-medium text-slate-700">Therapy notes</label>
                  <textarea
                    required
                    rows={4}
                    disabled={!selectedPrescription}
                    value={prescriptionNotes}
                    onChange={(event) => setPrescriptionNotes(event.target.value)}
                    className="rounded border border-slate-200 px-3 py-2 text-sm disabled:opacity-40"
                  />
                </div>
                <button
                  type="submit"
                  disabled={!selectedPrescription || prescriptionLoading}
                  className="w-full rounded bg-emerald-600 px-4 py-2 text-sm font-medium text-white disabled:opacity-60"
                >
                  {prescriptionLoading ? "Submitting..." : "Issue prescription"}
                </button>
              </form>
              <div>
                <FeedbackBanner state={prescriptionFeedback} />
              </div>
              <p className="text-xs uppercase tracking-[0.3em] text-slate-500">
                Total issued • {prescriptionStats.total}
              </p>
            </div>
          </SectionCard>
        </div>
      </div>
    </div>
  );
}
