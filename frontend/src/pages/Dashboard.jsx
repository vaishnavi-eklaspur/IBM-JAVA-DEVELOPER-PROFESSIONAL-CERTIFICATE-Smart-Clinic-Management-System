import React, { useState } from "react";
import Navbar from "../components/Navbar";
import PageContainer from "../layout/PageContainer";

export default function Dashboard() {
  const [loading, setLoading] = useState(false);
  // Placeholder data
  const appointments = [];
  const doctors = [];

  return (
    <div className="min-h-screen bg-neutral-50">
      <Navbar />
      <PageContainer>
        <h2 className="text-2xl font-semibold text-neutral-800 mb-6">Dashboard</h2>
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
                {/* Render appointments here */}
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
                {/* Render doctors here */}
              </ul>
            )}
          </section>
        </div>
      </PageContainer>
    </div>
  );
}
