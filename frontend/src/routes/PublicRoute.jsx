import React from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function PublicRoute({ children }) {
  const { isAuthenticated, isInitializing, role } = useAuth();

  if (isInitializing) {
    return null;
  }

  if (isAuthenticated) {
    if (role === "DOCTOR") {
      return <Navigate to="/dashboard/doctor" replace />;
    }
    if (role === "PATIENT") {
      return <Navigate to="/dashboard/patient" replace />;
    }
    return <Navigate to="/dashboard" replace />;
  }

  return children;
}
