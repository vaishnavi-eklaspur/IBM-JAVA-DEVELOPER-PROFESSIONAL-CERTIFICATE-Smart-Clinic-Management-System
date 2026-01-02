import React from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Dashboard() {
  const { role } = useAuth();

  if (role === "DOCTOR") {
    return <Navigate to="/dashboard/doctor" replace />;
  }

  if (role === "PATIENT") {
    return <Navigate to="/dashboard/patient" replace />;
  }

  return <Navigate to="/login" replace />;
}
