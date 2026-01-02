import React from "react";
import { Link, useLocation } from "react-router-dom";
import LogoutButton from "./LogoutButton";
import { useAuth } from "../context/AuthContext";

export default function Navbar() {
  const location = useLocation();
  const { isAuthenticated, role } = useAuth();
  const dashboardPath = role === "DOCTOR" ? "/dashboard/doctor" : role === "PATIENT" ? "/dashboard/patient" : "/dashboard";

  return (
    <nav className="bg-neutral-100 border-b border-neutral-200 px-6 py-3 flex items-center justify-between">
      <div className="text-lg font-bold text-neutral-800 tracking-tight">Smart Clinic</div>
      <div className="flex gap-4 items-center text-sm">
        <Link to="/" className="text-neutral-700 hover:text-blue-700 transition-colors">
          Home
        </Link>
        {!isAuthenticated && (
          <>
            <Link to="/login" className="text-neutral-700 hover:text-blue-700 transition-colors">
              Sign In
            </Link>
            <Link
              to="/register"
              className="rounded-full border border-blue-600 px-4 py-1.5 font-medium text-blue-700 transition hover:bg-blue-50"
            >
              Sign Up
            </Link>
          </>
        )}
        {isAuthenticated && (
          <>
            <span className="uppercase text-neutral-500">{role}</span>
            {location.pathname !== dashboardPath && (
              <Link to={dashboardPath} className="text-neutral-700 hover:text-blue-700 transition-colors">
                Dashboard
              </Link>
            )}
            <LogoutButton />
          </>
        )}
      </div>
    </nav>
  );
}
