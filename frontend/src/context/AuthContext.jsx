import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import FullscreenSpinner from "../components/FullscreenSpinner";
import { registerUnauthorizedHandler, setAuthToken } from "../services/api";
import { doctorLogin, patientLogin } from "../services/auth";

const AuthContext = createContext(null);
const STORAGE_KEY = "smart_clinic_auth";

const emptyState = {
  token: null,
  role: null,
  user: null,
};

function readPersistedState() {
  const persisted = localStorage.getItem(STORAGE_KEY);
  if (!persisted) {
    return { ...emptyState };
  }
  try {
    const parsed = JSON.parse(persisted);
    return {
      token: parsed.token || null,
      role: parsed.role || null,
      user: parsed.user || null,
    };
  } catch (error) {
    return { ...emptyState };
  }
}

export function AuthProvider({ children }) {
  const navigate = useNavigate();
  const [authState, setAuthState] = useState(() => {
    const initial = readPersistedState();
    setAuthToken(initial.token);
    return initial;
  });
  const [isInitializing, setIsInitializing] = useState(true);

  const persistState = useCallback((nextState) => {
    setAuthState(nextState);
    setAuthToken(nextState.token);
    if (nextState.token) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(nextState));
    } else {
      localStorage.removeItem(STORAGE_KEY);
    }
  }, []);

  const clearSession = useCallback(() => {
    persistState({ ...emptyState });
  }, [persistState]);

  const logout = useCallback(() => {
    clearSession();
    navigate("/login", { replace: true });
  }, [clearSession, navigate]);

  useEffect(() => {
    registerUnauthorizedHandler(logout);
  }, [logout]);

  useEffect(() => {
    setIsInitializing(false);
  }, []);

  const login = useCallback(
    async (roleType, credentials) => {
      let response;
      if (roleType === "DOCTOR") {
        response = await doctorLogin(credentials);
      } else if (roleType === "PATIENT") {
        response = await patientLogin(credentials);
      } else {
        throw new Error("Unsupported role selection");
      }

      const nextState = {
        token: response.token,
        role: response.role,
        user: response.email || credentials.email || null,
      };
      persistState(nextState);
      return nextState;
    },
    [persistState]
  );

  const value = useMemo(
    () => ({
      token: authState.token,
      role: authState.role,
      user: authState.user,
      isAuthenticated: Boolean(authState.token),
      isInitializing,
      login,
      logout,
    }),
    [authState, isInitializing, login, logout]
  );

  return (
    <AuthContext.Provider value={value}>
      {isInitializing ? <FullscreenSpinner label="Authenticating session..." /> : children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
