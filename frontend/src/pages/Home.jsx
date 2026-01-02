import React from "react";
import Navbar from "../components/Navbar";
import PageContainer from "../layout/PageContainer";
import { Link } from "react-router-dom";

export default function Home() {
  return (
    <div className="min-h-screen bg-neutral-50">
      <Navbar />
      <PageContainer>
        <h1 className="text-2xl font-semibold text-neutral-800 mb-4">Welcome to Smart Clinic</h1>
        <p className="text-neutral-600 mb-6">
          Manage doctor and patient workflows with secure access. Please sign in with your clinic-issued credentials
          to continue.
        </p>
        <Link
          to="/login"
          className="inline-flex items-center px-4 py-2 rounded bg-blue-600 text-white text-sm font-medium hover:bg-blue-700"
        >
          Go to Login
        </Link>
      </PageContainer>
    </div>
  );
}
