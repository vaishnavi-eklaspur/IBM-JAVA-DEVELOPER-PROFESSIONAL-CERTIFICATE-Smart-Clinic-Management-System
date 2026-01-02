import React, { useEffect, useMemo, useState } from "react";
import Navbar from "../components/Navbar";
import SectionCard from "../components/dashboard/SectionCard";
import { useAuth } from "../context/AuthContext";
import { useApiResource } from "../hooks/useApiResource";
import {
  bookPatientAppointment,
  fetchDoctors,
  fetchPatientAppointments,
  fetchPatientPrescriptions,
  fetchPatientProfile,
} from "../services/patient";
import { fetchDoctorAvailability } from "../services/doctor";
import { formatDate, formatDateTime } from "../utils/formatters";

const statusStyles = {
  BOOKED: "bg-sky-100 text-sky-800",
  COMPLETED: "bg-emerald-100 text-emerald-800",
  CANCELLED: "bg-rose-100 text-rose-800",
};

function StatusBadge({ status }) {
  if (!status) {
    return <span className="rounded-full bg-slate-100 px-2.5 py-0.5 text-xs text-slate-600">Pending</span>;
  }
  const classes = statusStyles[status] || "bg-slate-100 text-slate-600";
  return <span className={`rounded-full px-2.5 py-0.5 text-xs font-medium ${classes}`}>{status}</span>;
}

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
  const base = "rounded-md px-3 py-2 text-sm";
  if (state.type === "error") {
    return <div className={`${base} border border-rose-100 bg-rose-50 text-rose-700`}>{state.message}</div>;
  }
  return <div className={`${base} border border-emerald-100 bg-emerald-50 text-emerald-700`}>{state.message}</div>;
}

function SlotButton({ slot, isSelected, onSelect }) {
  return (
    <button
      type="button"
      onClick={() => onSelect(slot)}
      className={`rounded-lg border px-3 py-2 text-sm font-medium transition ${
        isSelected ? "border-blue-500 bg-blue-50 text-blue-800" : "border-slate-200 bg-white text-slate-600"
      }`}
    >
      {slot}
    </button>
  );
}

function buildAppointmentTimestamp(date, slot) {
  if (!date || !slot) {
    return null;
  }
  const candidate = slot.includes("T") ? slot : `${date}T${slot}`;
  const parsed = new Date(candidate);
  if (Number.isNaN(parsed.getTime())) {
    return null;
  }
  return parsed.toISOString();
}

function Metric({ label, value }) {
  return (
    <div className="rounded-xl border border-slate-100 bg-white/70 px-3 py-2">
      <p className="text-xs uppercase tracking-widest text-slate-500">{label}</p>
      <p className="text-2xl font-semibold text-slate-900">{value}</p>
    </div>
  );
}

function AppointmentCard({ appointment }) {
  return (
    <div className="rounded-xl border border-slate-200 bg-white/80 p-4 shadow-sm">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-sm text-slate-500">Appointment #{appointment.id}</p>
          <p className="text-base font-semibold text-slate-900">{appointment.doctor?.name || "Doctor TBD"}</p>
          <p className="text-sm text-slate-600">{appointment.doctor?.speciality || "General"}</p>
        </div>
        <StatusBadge status={appointment.status} />
      </div>
      <div className="mt-4 flex flex-wrap gap-4 text-sm text-slate-600">
        <span className="font-medium text-slate-900">{formatDateTime(appointment.appointmentTime)}</span>
        <span>{appointment.doctor?.email}</span>
      </div>
    </div>
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

export default function PatientDashboard() {
  const { isAuthenticated, role } = useAuth();
  const [selectedDoctorId, setSelectedDoctorId] = useState(null);
  const [selectedDate, setSelectedDate] = useState(() => new Date().toISOString().slice(0, 10));
  const [selectedSlot, setSelectedSlot] = useState("");
  const [bookingFeedback, setBookingFeedback] = useState(null);
  const [bookingLoading, setBookingLoading] = useState(false);

  const {
    data: profile,
    loading: profileLoading,
    error: profileError,
  } = useApiResource(fetchPatientProfile);

  const {
    data: doctors,
    loading: doctorsLoading,
    error: doctorsError,
  } = useApiResource(fetchDoctors, {
    enabled: isAuthenticated && role === "PATIENT",
    requireAuth: true,
  });

  useEffect(() => {
    if (!selectedDoctorId && doctors?.length) {
      setSelectedDoctorId(String(doctors[0].id));
    }
  }, [doctors, selectedDoctorId]);

  useEffect(() => {
    setSelectedSlot("");
  }, [selectedDoctorId, selectedDate]);

  const {
    data: availability,
    loading: availabilityLoading,
    error: availabilityError,
    refresh: refreshAvailability,
  } = useApiResource(
    () => fetchDoctorAvailability(selectedDoctorId, selectedDate),
    {
      dependencies: [selectedDoctorId, selectedDate],
      enabled: Boolean(selectedDoctorId && selectedDate),
    }
  );

  const {
    data: appointments,
    loading: appointmentsLoading,
    error: appointmentsError,
    refresh: refreshAppointments,
  } = useApiResource(fetchPatientAppointments);

  const {
    data: prescriptions,
    loading: prescriptionsLoading,
    error: prescriptionsError,
  } = useApiResource(fetchPatientPrescriptions);

  const selectedDoctor = useMemo(() => {
    if (!doctors || !selectedDoctorId) {
      return null;
    }
    return doctors.find((doctor) => String(doctor.id) === String(selectedDoctorId)) || null;
  }, [doctors, selectedDoctorId]);

  const sortedAppointments = useMemo(() => {
    if (!appointments) {
      return [];
    }
    return [...appointments].sort((a, b) => new Date(b.appointmentTime) - new Date(a.appointmentTime));
  }, [appointments]);

  const appointmentStats = useMemo(() => {
    const base = { total: 0, booked: 0, completed: 0, cancelled: 0 };
    if (!appointments) {
      return base;
    }
    return appointments.reduce(
      (acc, item) => {
        acc.total += 1;
        if (item.status && acc[item.status.toLowerCase()] !== undefined) {
          acc[item.status.toLowerCase()] += 1;
        }
        return acc;
      },
      base
    );
  }, [appointments]);

  const handleBookAppointment = async (event) => {
    event.preventDefault();
    if (!selectedDoctorId || !selectedSlot) {
      setBookingFeedback({ type: "error", message: "Please choose a doctor and an available slot." });
      return;
    }
    const appointmentTime = buildAppointmentTimestamp(selectedDate, selectedSlot);
    if (!appointmentTime) {
      setBookingFeedback({ type: "error", message: "Selected slot is invalid." });
      return;
    }

    setBookingLoading(true);
    setBookingFeedback(null);
    try {
      await bookPatientAppointment({ doctorId: Number(selectedDoctorId), appointmentTime });
      setBookingFeedback({ type: "success", message: "Appointment booked successfully." });
      setSelectedSlot("");
      refreshAppointments();
      refreshAvailability();
    } catch (error) {
      setBookingFeedback({ type: "error", message: error.message });
    } finally {
      setBookingLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-white to-sky-50">
      <Navbar />
      <div className="mx-auto max-w-6xl px-6 py-10 space-y-8">
        <header className="space-y-2">
          <p className="text-sm uppercase tracking-[0.3em] text-slate-500">Patient Workspace</p>
          <h1 className="text-3xl font-semibold text-slate-900">Care overview</h1>
          <p className="text-base text-slate-600">
            Track upcoming visits, follow prescriptions, and keep your personal details current.
          </p>
        </header>

        <div className="grid gap-6 lg:grid-cols-3">
          <SectionCard
            tone="sky"
            eyebrow="Profile"
            title="Personal details"
            description="Information stored with the clinic"
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
                  <dt className="text-slate-500">Phone</dt>
                  <dd className="text-slate-900">{profile.phone || "Not provided"}</dd>
                </div>
                <div>
                  <dt className="text-slate-500">Patient ID</dt>
                  <dd className="text-slate-900">{profile.id}</dd>
                </div>
              </dl>
            )}
          </SectionCard>

          <SectionCard
            tone="emerald"
            eyebrow="Snapshot"
            title="Visit summary"
            description="Realtime counts from your record"
            className="lg:col-span-2"
          >
            {appointmentsLoading && <InlineLoader label="Loading appointment stats..." />}
            {appointmentsError && <ErrorBanner message={appointmentsError.message} />}
            {!appointmentsLoading && !appointmentsError && (
              <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
                <Metric label="Total" value={appointmentStats.total} />
                <Metric label="Booked" value={appointmentStats.booked} />
                <Metric label="Completed" value={appointmentStats.completed} />
                <Metric label="Cancelled" value={appointmentStats.cancelled} />
              </div>
            )}
          </SectionCard>
        </div>

        <SectionCard
          tone="sky"
          eyebrow="Schedule"
          title="Book an appointment"
          description="Reserve a slot without leaving the workspace"
        >
          {doctorsLoading && <InlineLoader label="Loading doctors..." />}
          {doctorsError && <ErrorBanner message={doctorsError.message} />}
          {!doctorsLoading && !doctorsError && (!doctors || doctors.length === 0) && (
            <p className="text-sm text-slate-600">No doctors are currently available. Please try again later.</p>
          )}
          {doctors && doctors.length > 0 && (
            <form className="space-y-4" onSubmit={handleBookAppointment}>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Doctor</label>
                <select
                  value={selectedDoctorId || ""}
                  onChange={(event) => setSelectedDoctorId(event.target.value)}
                  className="w-full rounded border border-slate-200 px-3 py-2 text-sm"
                  required
                >
                  <option value="" disabled>
                    Select a doctor
                  </option>
                  {doctors.map((doctor) => (
                    <option key={doctor.id} value={doctor.id}>
                      {doctor.name} · {doctor.speciality || "General"}
                    </option>
                  ))}
                </select>
                {selectedDoctor && (
                  <p className="mt-1 text-xs text-slate-500">
                    {selectedDoctor.email} • Doctor #{selectedDoctor.id}
                  </p>
                )}
              </div>
              <div className="grid gap-4 md:grid-cols-2">
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Date</label>
                  <input
                    type="date"
                    value={selectedDate}
                    onChange={(event) => setSelectedDate(event.target.value)}
                    className="w-full rounded border border-slate-200 px-3 py-2 text-sm"
                    required
                  />
                </div>
                <div>
                  <p className="text-sm font-medium text-slate-700 mb-1">Available slots</p>
                  {availabilityLoading && <InlineLoader label="Checking slots..." />}
                  {availabilityError && <ErrorBanner message={availabilityError.message} />}
                  {!availabilityLoading && !availabilityError && (!availability || availability.length === 0) && (
                    <p className="text-xs text-slate-500">All slots are booked for {formatDate(selectedDate)}.</p>
                  )}
                  <div className="mt-2 grid gap-2 sm:grid-cols-3">
                    {availability?.map((slot, index) => (
                      <SlotButton
                        key={`${slot}-${index}`}
                        slot={slot}
                        isSelected={selectedSlot === slot}
                        onSelect={setSelectedSlot}
                      />
                    ))}
                  </div>
                </div>
              </div>
              <FeedbackBanner state={bookingFeedback} />
              <button
                type="submit"
                disabled={bookingLoading || !selectedSlot}
                className="w-full rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white disabled:opacity-60"
              >
                {bookingLoading ? "Booking..." : "Book appointment"}
              </button>
            </form>
          )}
        </SectionCard>

        <SectionCard
          tone="slate"
          eyebrow="Appointments"
          title="All visits"
          description="Upcoming and past encounters"
        >
          {appointmentsLoading && <InlineLoader label="Loading appointments..." />}
          {appointmentsError && <ErrorBanner message={appointmentsError.message} />}
          {!appointmentsLoading && !appointmentsError && sortedAppointments.length === 0 && (
            <p className="text-sm text-slate-600">You have no appointments yet.</p>
          )}
          <div className="mt-4 grid gap-4">
            {sortedAppointments.map((appointment) => (
              <AppointmentCard key={appointment.id} appointment={appointment} />
            ))}
          </div>
        </SectionCard>

        <SectionCard
          tone="amber"
          eyebrow="Prescriptions"
          title="Medication guidance"
          description="Issued after each completed visit"
        >
          {prescriptionsLoading && <InlineLoader label="Loading prescriptions..." />}
          {prescriptionsError && <ErrorBanner message={prescriptionsError.message} />}
          {!prescriptionsLoading && !prescriptionsError && prescriptions?.length === 0 && (
            <p className="text-sm text-slate-600">No prescriptions recorded.</p>
          )}
          <div className="mt-4 grid gap-4">
            {prescriptions?.map((prescription) => (
              <PrescriptionCard key={prescription.id} prescription={prescription} />
            ))}
          </div>
        </SectionCard>
      </div>
    </div>
  );
}
