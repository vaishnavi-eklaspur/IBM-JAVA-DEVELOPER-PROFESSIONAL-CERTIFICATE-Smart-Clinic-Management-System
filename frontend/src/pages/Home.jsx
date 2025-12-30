import React from "react";
import Navbar from "../components/Navbar";
import PageContainer from "../layout/PageContainer";

export default function Home() {
  return (
    <div className="min-h-screen bg-neutral-50">
      <Navbar />
      <PageContainer>
        <h1 className="text-3xl font-bold text-neutral-800 mb-4">Smart Clinic Management System</h1>
        <p className="text-neutral-700 mb-2">A modern platform for managing appointments, doctors, and patients efficiently.</p>
        <ul className="list-disc pl-6 text-neutral-700">
          <li>Book and manage appointments</li>
          <li>Doctor and patient dashboards</li>
          <li>Prescription management</li>
        </ul>
      </PageContainer>
    </div>
  );
}
