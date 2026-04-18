'use client';
import { useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { useAuthStore } from '@/lib/auth/store';
import { apiClient } from '@/lib/api/client';

export default function CallbackPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { setAccessToken, setUser } = useAuthStore();

  useEffect(() => {
    const code = searchParams.get('code');
    if (!code) {
      router.replace('/login');
      return;
    }

    const exchangeCode = async () => {
      try {
        const params = new URLSearchParams({
          grant_type: 'authorization_code',
          client_id: process.env.NEXT_PUBLIC_KEYCLOAK_CLIENT_ID!,
          code,
          redirect_uri: `${window.location.origin}/auth/callback`,
        });
        const res = await fetch(
          `${process.env.NEXT_PUBLIC_KEYCLOAK_URL}/realms/${process.env.NEXT_PUBLIC_KEYCLOAK_REALM}/protocol/openid-connect/token`,
          {
            method: 'POST',
            body: params,
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
          }
        );
        const { access_token } = await res.json();
        setAccessToken(access_token);
        const { data: user } = await apiClient.post('/api/v1/auth/sync');
        setUser(user);
        router.replace('/dashboard');
      } catch {
        router.replace('/login');
      }
    };
    exchangeCode();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className="min-h-screen flex items-center justify-center">
      <p className="text-slate-500">Iniciando sesión...</p>
    </div>
  );
}
