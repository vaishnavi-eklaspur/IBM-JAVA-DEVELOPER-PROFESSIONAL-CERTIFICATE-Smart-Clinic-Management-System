import api from "./api";

function transformError(error, fallbackMessage = "Request failed. Please try again.") {
  const backendMessage = error?.response?.data?.message;
  if (backendMessage) {
    return new Error(backendMessage);
  }
  return new Error(fallbackMessage);
}

export async function doctorLogin({ email, password }) {
  try {
    const response = await api.post("/doctor/login", { email, password });
    return response.data;
  } catch (error) {
    throw transformError(error, "Doctor login failed. Please verify your credentials.");
  }
}

export async function patientLogin({ email, password }) {
  try {
    const response = await api.post("/patient/login", { email, password });
    return response.data;
  } catch (error) {
    throw transformError(error, "Patient login failed. Please verify your credentials.");
  }
}

export async function doctorRegister({ name, email, speciality, password }) {
  try {
    const payload = {
      name: name?.trim(),
      email: email?.trim(),
      speciality: speciality?.trim(),
      password,
    };
    const response = await api.post("/doctor/register", payload);
    return response.data;
  } catch (error) {
    throw transformError(error, "Unable to register doctor account.");
  }
}

export async function patientRegister({ name, email, phone, password }) {
  try {
    const payload = {
      name: name?.trim(),
      email: email?.trim(),
      phone: phone?.trim(),
      password,
    };
    const response = await api.post("/patient/register", payload);
    return response.data;
  } catch (error) {
    throw transformError(error, "Unable to register patient account.");
  }
}
