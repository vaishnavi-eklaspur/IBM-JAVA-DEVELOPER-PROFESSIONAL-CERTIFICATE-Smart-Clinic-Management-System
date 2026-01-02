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

export async function fetchDoctorProfile() {
  try {
    const response = await api.get("/doctors/me");
    return response.data;
  } catch (error) {
    throw toError(error, "Unable to load doctor profile.");
  }
}

export async function fetchDoctorAppointments() {
  try {
    const response = await api.get("/appointments/doctor");
    return response.data ?? [];
  } catch (error) {
    throw toError(error, "Unable to load appointments.");
  }
}

export async function fetchDoctorPrescriptions() {
  try {
    const response = await api.get("/prescriptions/doctor");
    return response.data ?? [];
  } catch (error) {
    throw toError(error, "Unable to load prescriptions.");
  }
}

export async function fetchDoctorAvailability(doctorId, date) {
  if (!doctorId || !date) {
    return [];
  }
  try {
    const response = await api.get(`/doctors/${doctorId}/availability`, { params: { date } });
    return response.data ?? [];
  } catch (error) {
    throw toError(error, "Unable to load availability.");
  }
}

export async function completeAppointment(appointmentId) {
  try {
    const response = await api.post(`/appointments/${appointmentId}/complete`);
    return response.data;
  } catch (error) {
    throw toError(error, "Unable to complete appointment.");
  }
}

export async function cancelAppointment(appointmentId) {
  try {
    const response = await api.post(`/appointments/${appointmentId}/cancel`);
    return response.data;
  } catch (error) {
    throw toError(error, "Unable to cancel appointment.");
  }
}

export async function createPrescription(payload) {
  try {
    const response = await api.post("/prescriptions", payload);
    return response.data;
  } catch (error) {
    throw toError(error, "Unable to create prescription.");
  }
}
