import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080/api",
  headers: {
    "Content-Type": "application/json",
  },
});

let currentToken = null;
let unauthorizedHandler = null;
let lastUnauthorizedToken = null;

export function setAuthToken(token) {
  currentToken = token || null;
  lastUnauthorizedToken = null;
}

export function registerUnauthorizedHandler(handler) {
  unauthorizedHandler = handler;
}

api.interceptors.request.use(
  (config) => {
    if (currentToken) {
      config.headers = config.headers || {};
      config.headers.Authorization = `Bearer ${currentToken}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status;
    const shouldHandle =
      (status === 401 || status === 403) && typeof unauthorizedHandler === "function" && currentToken;
    if (shouldHandle && lastUnauthorizedToken !== currentToken) {
      lastUnauthorizedToken = currentToken;
      unauthorizedHandler();
    }
    return Promise.reject(error);
  }
);

export default api;
