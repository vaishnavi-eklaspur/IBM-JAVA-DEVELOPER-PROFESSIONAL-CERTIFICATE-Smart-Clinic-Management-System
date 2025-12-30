import React from "react";
import Navbar from "../components/Navbar";
import PageContainer from "../layout/PageContainer";

export default function Login() {
  return (
    <div className="min-h-screen bg-neutral-50">
      <Navbar />
      <PageContainer>
        <h2 className="text-2xl font-semibold text-neutral-800 mb-4">Login</h2>
        <form className="flex flex-col gap-4">
          <input
            type="text"
            placeholder="Username"
            className="border border-neutral-200 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-100 transition"
          />
          <input
            type="password"
            placeholder="Password"
            className="border border-neutral-200 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-100 transition"
          />
          <button
            type="button"
            className="bg-blue-700 text-white py-2 rounded hover:bg-blue-100 hover:text-blue-700 transition"
          >
            Login
          </button>
        </form>
      </PageContainer>
    </div>
  );
}
