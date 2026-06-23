'use client';
import { useAuthStore } from '@/lib/auth/store';

export function useAuth() {
  const { accessToken, user, isAuthenticated, setAccessToken, setUser, clearAuth, isAdmin } =
    useAuthStore();
  return { accessToken, user, isAuthenticated, setAccessToken, setUser, clearAuth, isAdmin };
}
