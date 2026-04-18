'use client';
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface User {
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  centerRoles: Array<{ centerId: string; role: string }>;
}

interface AuthState {
  accessToken: string | null;
  user: User | null;
  isAuthenticated: boolean;
  setAccessToken: (token: string) => void;
  setUser: (user: User) => void;
  clearAuth: () => void;
  isAdmin: () => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      accessToken: null,
      user: null,
      isAuthenticated: false,
      setAccessToken: (token) => set({ accessToken: token, isAuthenticated: true }),
      setUser: (user) => set({ user }),
      clearAuth: () => set({ accessToken: null, user: null, isAuthenticated: false }),
      isAdmin: () => {
        const { user } = get();
        return user?.centerRoles?.some((r) => r.role === 'ADMIN') ?? false;
      },
    }),
    {
      name: 'padelcenter-auth',
      partialize: (state) => ({
        user: state.user,
        // accessToken deliberadamente excluido — solo en memoria
      }),
    }
  )
);
