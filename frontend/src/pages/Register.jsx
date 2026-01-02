import React, { useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import AuthLayout from "../layout/AuthLayout";
import { doctorRegister, patientRegister } from "../services/auth";

const roleOptions = [
  { value: "PATIENT", label: "Patient" },
  { value: "DOCTOR", label: "Doctor" },
];

export default function Register() {
  const navigate = useNavigate();
  const [role, setRole] = useState("PATIENT");
  const [form, setForm] = useState({
    name: "",
    email: "",
    password: "",
    confirmPassword: "",
    phone: "",
    speciality: "",
  });
  const [error, setError] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const roleSpecificField = useMemo(() => {
    if (role === "PATIENT") {
      return {
        label: "Mobile number",
        placeholder: "(+1) 555-1234",
        field: "phone",
      };
    }
    return {
      label: "Speciality",
      placeholder: "Cardiology",
      field: "speciality",
    };
  }, [role]);

  const handleInputChange = (field) => (event) => {
    setForm((prev) => ({ ...prev, [field]: event.target.value }));
  };

  const handleRoleChange = (event) => {
    setRole(event.target.value);
    setError(null);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError(null);
    if (form.password !== form.confirmPassword) {
      setError("Passwords do not match.");
      return;
    }
    if (!form.name.trim()) {
      setError("Full name is required.");
      return;
    }
    if (!form.email.trim()) {
      setError("Email is required.");
      return;
    }
    const roleFieldValue = form[roleSpecificField.field].trim();
    if (!roleFieldValue) {
      setError(`${roleSpecificField.label} is required.`);
      return;
    }

    setIsSubmitting(true);
    try {
      if (role === "PATIENT") {
        await patientRegister({
          name: form.name.trim(),
          email: form.email.trim(),
          phone: roleFieldValue,
          password: form.password,
        });
      } else {
        await doctorRegister({
          name: form.name.trim(),
          email: form.email.trim(),
          speciality: roleFieldValue,
          password: form.password,
        });
      }
      navigate("/login", {
        replace: true,
        state: {
          registeredEmail: form.email.trim(),
          message: "Account created successfully. Please sign in to continue.",
        },
      });
    } catch (err) {
      setError(err.message || "Registration failed.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <AuthLayout>
      <div className="space-y-8">
        <header>
          <p className="text-xs uppercase tracking-[0.3em] text-slate-500">Sign up</p>
          <h1 className="mt-2 text-3xl font-semibold text-slate-900">Create your Smart Clinic account</h1>
          <p className="mt-3 text-sm text-slate-600">
            Choose your role, complete the secure form, and you will be ready to access the tailored dashboard experience.
          </p>
        </header>

        <form className="space-y-4" onSubmit={handleSubmit}>
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Role</label>
            <div className="grid grid-cols-2 gap-3">
              {roleOptions.map((option) => (
                <label
                  key={option.value}
                  className={`rounded-xl border px-4 py-3 text-center text-sm font-medium transition ${
                    role === option.value
                      ? "border-blue-500 bg-blue-50 text-blue-800"
                      : "border-slate-200 bg-white text-slate-600 hover:border-slate-300"
                  }`}
                >
                  <input
                    type="radio"
                    name="role"
                    value={option.value}
                    checked={role === option.value}
                    onChange={handleRoleChange}
                    className="sr-only"
                  />
                  {option.label}
                </label>
              ))}
            </div>
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <div>
              <label className="mb-1 block text-sm font-medium text-slate-700">Full name</label>
              <input
                type="text"
                value={form.name}
                onChange={handleInputChange("name")}
                className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
                placeholder="Alex Martinez"
                required
              />
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium text-slate-700">Email</label>
              <input
                type="email"
                value={form.email}
                onChange={handleInputChange("email")}
                className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
                placeholder="you@clinic.com"
                required
              />
            </div>
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">{roleSpecificField.label}</label>
            <input
              type="text"
              value={form[roleSpecificField.field]}
              onChange={handleInputChange(roleSpecificField.field)}
              className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
              placeholder={roleSpecificField.placeholder}
              required
            />
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <div>
              <label className="mb-1 block text-sm font-medium text-slate-700">Password</label>
              <input
                type="password"
                value={form.password}
                onChange={handleInputChange("password")}
                className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
                placeholder="••••••••"
                required
              />
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium text-slate-700">Confirm password</label>
              <input
                type="password"
                value={form.confirmPassword}
                onChange={handleInputChange("confirmPassword")}
                className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
                placeholder="••••••••"
                required
              />
            </div>
          </div>

          {error && <p className="text-sm text-rose-600">{error}</p>}

          <button
            type="submit"
            className="w-full rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-blue-700 disabled:opacity-60"
            disabled={isSubmitting}
          >
            {isSubmitting ? "Creating account..." : "Create account"}
          </button>
        </form>

        <p className="text-sm text-slate-600">
          Already have an account?{" "}
          <Link to="/login" className="font-semibold text-blue-700 hover:underline">
            Sign in
          </Link>
          .
        </p>
      </div>
    </AuthLayout>
  );
}
