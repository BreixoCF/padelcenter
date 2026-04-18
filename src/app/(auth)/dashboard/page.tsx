'use client';
import { useAuthStore } from '@/lib/auth/store';

export default function DashboardPage() {
  const { user } = useAuthStore();

  return (
    <main className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-semibold mb-2">
        Hola, {user?.firstName ?? 'usuario'}
      </h1>
      <p className="text-muted-foreground">Bienvenido a PadelCenter.</p>
    </main>
  );
}
