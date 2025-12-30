import React from "react";
import { Link } from "react-router-dom";

export default function Navbar() {
  return (
    <nav className="bg-neutral-100 border-b border-neutral-200 px-6 py-3 flex items-center justify-between">
      <div className="text-lg font-bold text-neutral-800 tracking-tight">Smart Clinic</div>
      <div className="flex gap-4">
        <Link to="/" className="text-neutral-700 hover:text-blue-700 transition-colors">Home</Link>
        <Link to="/login" className="text-neutral-700 hover:text-blue-700 transition-colors">Login</Link>
      </div>
    </nav>
  );
}
