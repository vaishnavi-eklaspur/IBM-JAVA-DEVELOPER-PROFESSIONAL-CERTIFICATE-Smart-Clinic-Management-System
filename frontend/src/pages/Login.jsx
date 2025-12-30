import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import PageContainer from "../layout/PageContainer";
import { loginUser, storeAuth } from "../services/auth";

export default function Login() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    try {
      const data = await loginUser(username, password);
      storeAuth({ token: data.token, role: data.role });
      navigate("/dashboard");
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-neutral-50">
      <Navbar />
      <PageContainer>
        <h2 className="text-2xl font-semibold text-neutral-800 mb-4">Login</h2>
        <form className="flex flex-col gap-4" onSubmit={handleSubmit}>
          <input
            type="text"
            placeholder="Username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            className="border border-neutral-200 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-100 transition"
            disabled={loading}
            autoFocus
          />
          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="border border-neutral-200 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-100 transition"
            disabled={loading}
          />
          <button
            type="submit"
            className={`bg-blue-700 text-white py-2 rounded transition font-medium ${loading ? "opacity-60 cursor-not-allowed" : "hover:bg-blue-100 hover:text-blue-700"}`}
            disabled={loading}
          >
            {loading ? "Logging in..." : "Login"}
          </button>
          {error && (
            <div className="text-red-600 text-sm mt-1" role="alert">{error}</div>
          )}
        </form>
      </PageContainer>
    </div>
  );
}
