import React from "react";
import { useAuth } from "../context/AuthContext";

export default function LogoutButton() {
  const { logout } = useAuth();
  return (
    <button
      onClick={logout}
      className="ml-4 px-3 py-1 rounded bg-neutral-200 text-neutral-700 hover:bg-neutral-300 transition"
    >
      Logout
    </button>
  );
}
