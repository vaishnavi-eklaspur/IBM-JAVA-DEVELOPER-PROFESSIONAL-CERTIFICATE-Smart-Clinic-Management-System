import api from "./api";

function toError(error, fallbackMessage) {
  const backendMessage = error?.response?.data?.message;
  return new Error(backendMessage || fallbackMessage);
}

function normalizeList(payload) {
  if (Array.isArray(payload)) {
    return payload;
  }
  if (Array.isArray(payload?.content)) {
    return payload.content;
  }
  if (Array.isArray(payload?.data)) {
    return payload.data;
  }
  return [];
}

export async function fetchDoctors() {
  try {
    const response = await api.get("/doctors");
    return normalizeList(response.data);
  } catch (error) {
    throw toError(error, "Unable to load doctors.");
  }
}

export async function fetchPatientProfile() {
  try {
    const response = await api.get("/patient/profile");
    return response.data;
  } catch (error) {
    throw toError(error, "Unable to load patient profile.");
  }
}

export async function fetchPatientAppointments() {
  try {
    const response = await api.get("/patient/appointments");
    return response.data ?? [];
  } catch (error) {
    throw toError(error, "Unable to load appointments.");
  }
}

export async function bookPatientAppointment({ doctorId, appointmentTime }) {
  try {
    const payload = {
      doctorId,
      appointmentTime,
      doctor: doctorId ? { id: doctorId } : undefined,
    };
    const response = await api.post("/appointments", payload);
    return response.data;
  } catch (error) {
    throw toError(error, "Unable to book appointment.");
  }
}

export async function fetchPatientPrescriptions() {
  try {
    const response = await api.get("/prescriptions/patient");
    return response.data ?? [];
  } catch (error) {
    throw toError(error, "Unable to load prescriptions.");
  }
}
