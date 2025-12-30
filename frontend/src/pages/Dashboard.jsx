import React, { useEffect, useState } from "react";
import Navbar from "../components/Navbar";
import PageContainer from "../layout/PageContainer";
import api from "../services/api";

export default function Dashboard() {
  const [loading, setLoading] = useState(true);
  const [appointments, setAppointments] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    async function fetchData() {
      setLoading(true);
      setError("");
      try {
        const [appointmentsRes, doctorsRes] = await Promise.all([
          api.get("/appointments"),
          api.get("/doctors"),
        ]);
        setAppointments(appointmentsRes.data || []);
        setDoctors(doctorsRes.data || []);
      } catch (err) {
        setError("Failed to load dashboard data.");
      } finally {
        setLoading(false);
      }
    }
    fetchData();
  }, []);

  return (
    <div className="min-h-screen bg-neutral-50">
      <Navbar />
      <PageContainer>
        <h2 className="text-2xl font-semibold text-neutral-800 mb-6">Dashboard</h2>
        {error && <div className="text-red-600 mb-4">{error}</div>}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Appointments Card */}
          <section className="bg-white rounded-lg shadow-sm p-6 transition hover:shadow-md">
            <h3 className="text-lg font-bold text-neutral-700 mb-2">Appointments</h3>
            {loading ? (
              <div className="text-slate-500 animate-pulse">Loading...</div>
            ) : appointments.length === 0 ? (
              <div className="text-slate-500">No appointments yet.</div>
            ) : (
              <ul>
                {appointments.map((appt) => (
                  <li key={appt.id} className="mb-2 p-2 rounded bg-neutral-100 hover:bg-neutral-200 transition">
                    {appt.patientName} — {appt.date}
                  </li>
                ))}
              </ul>
            )}
          </section>
          {/* Doctors Card */}
          <section className="bg-white rounded-lg shadow-sm p-6 transition hover:shadow-md">
            <h3 className="text-lg font-bold text-neutral-700 mb-2">Doctors</h3>
            {loading ? (
              <div className="text-slate-500 animate-pulse">Loading...</div>
            ) : doctors.length === 0 ? (
              <div className="text-slate-500">No doctors found.</div>
            ) : (
              <ul>
                {doctors.map((doc) => (
                  <li key={doc.id} className="mb-2 p-2 rounded bg-neutral-100 hover:bg-neutral-200 transition">
                    Dr. {doc.name} — {doc.speciality}
                  </li>
                ))}
              </ul>
            )}
          </section>
        </div>
      </PageContainer>
    </div>
  );
}
