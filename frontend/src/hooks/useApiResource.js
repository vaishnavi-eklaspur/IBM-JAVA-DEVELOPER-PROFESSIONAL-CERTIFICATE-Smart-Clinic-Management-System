import { useCallback, useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext";

export function useApiResource(loader, { dependencies = [], enabled = true, requireAuth = true } = {}) {
  const { isAuthenticated, token } = useAuth();
  const hasAuth = !requireAuth || (isAuthenticated && Boolean(token));
  const isActive = Boolean(enabled && hasAuth);
  const memoizedLoader = useCallback(loader, dependencies);
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(() => isActive);
  const [error, setError] = useState(null);

  const execute = useCallback(async () => {
    if (!isActive) {
      return null;
    }
    setLoading(true);
    setError(null);
    try {
      const result = await memoizedLoader();
      setData(result);
      return result;
    } catch (err) {
      setError(err);
      throw err;
    } finally {
      setLoading(false);
    }
  }, [isActive, memoizedLoader]);

  useEffect(() => {
    let mounted = true;
    if (!isActive) {
      setLoading(false);
      setError(null);
      setData(null);
      return () => {
        mounted = false;
      };
    }
    setLoading(true);
    setError(null);
    memoizedLoader()
      .then((result) => {
        if (mounted) {
          setData(result);
        }
      })
      .catch((err) => {
        if (mounted) {
          setError(err);
        }
      })
      .finally(() => {
        if (mounted) {
          setLoading(false);
        }
      });
    return () => {
      mounted = false;
    };
  }, [memoizedLoader, isActive]);

  return { data, loading, error, refresh: execute };
}
