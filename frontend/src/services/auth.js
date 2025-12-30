import api from "./api";

export async function loginUser(username, password) {
  try {
    const response = await api.post("/auth/login", { username, password });
    return response.data;
  } catch (error) {
    if (error.response && error.response.data && error.response.data.message) {
      throw new Error(error.response.data.message);
    }
    throw new Error("Login failed. Please try again.");
  }
}

export function storeAuth({ token, role }) {
  localStorage.setItem("jwt", token);
  localStorage.setItem("role", role);
}

export function clearAuth() {
  localStorage.removeItem("jwt");
  localStorage.removeItem("role");
}

export function isAuthenticated() {
  return Boolean(localStorage.getItem("jwt"));
}
