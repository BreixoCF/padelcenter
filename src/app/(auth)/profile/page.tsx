'use client';
import { useAuthStore } from '@/lib/auth/store';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';

export default function ProfilePage() {
  const { user } = useAuthStore();

  return (
    <main className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-semibold mb-6">Mi perfil</h1>
      {user && (
        <Card className="max-w-md">
          <CardHeader>
            <CardTitle>{user.firstName} {user.lastName}</CardTitle>
          </CardHeader>
          <CardContent className="text-sm text-muted-foreground">
            <p>{user.email}</p>
          </CardContent>
        </Card>
      )}
    </main>
  );
}
