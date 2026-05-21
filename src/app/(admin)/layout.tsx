'use client';
import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/lib/auth/store';
import AppShell from '@/components/layout/AppShell';

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const { isAuthenticated, isAdmin } = useAuthStore();

  useEffect(() => {
    if (!isAuthenticated) router.replace('/login');
    else if (!isAdmin()) router.replace('/dashboard');
  }, [isAuthenticated, isAdmin, router]);

  if (!isAuthenticated || !isAdmin()) return null;
  return <AppShell>{children}</AppShell>;
}
