'use client';
import { useState, useEffect } from 'react';
import { useAuthStore } from '@/lib/auth/store';
import { useUpdateUser, useUpdatePassword } from '@/lib/api/generated/users/users';
import { useGetMeProfile } from '@/lib/api/generated/auth/auth';
import PageHeader from '@/components/shared/PageHeader';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useToast } from '@/components/ui/use-toast';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';

export default function ProfilePage() {
  const { user, setUser } = useAuthStore();
  const { toast } = useToast();

  const [profile, setProfile] = useState({
    firstName: user?.firstName ?? '',
    lastName: user?.lastName ?? '',
    email: user?.email ?? '',
    phoneNumber: '',
  });

  const [passwords, setPasswords] = useState({
    currentPassword: '',
    newPassword: '',
  });

  const { data: serverProfile } = useGetMeProfile();
  const { mutate: updateUser, isPending: isUpdating } = useUpdateUser();
  const { mutate: updatePassword, isPending: isChangingPass } = useUpdatePassword();

  useEffect(() => {
    if (serverProfile) {
      setProfile({
        firstName: serverProfile.firstName,
        lastName: serverProfile.lastName,
        email: serverProfile.email,
        phoneNumber: serverProfile.phoneNumber ?? '',
      });
    }
  }, [serverProfile]);

  const handleProfileSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!user?.userId) return;
    updateUser(
      { id: user.userId, data: profile },
      {
        onSuccess: (data) => {
          setUser(data as any);
          toast({ title: 'Perfil actualizado' });
        },
        onError: () => {
          toast({ title: 'Error al actualizar', variant: 'destructive' });
        },
      }
    );
  };

  const handlePasswordSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!user?.userId) return;
    updatePassword(
      { id: user.userId, data: passwords },
      {
        onSuccess: () => {
          toast({ title: 'Contraseña actualizada' });
          setPasswords({ currentPassword: '', newPassword: '' });
        },
        onError: (err: any) => {
          toast({
            title: 'Error al cambiar contraseña',
            description: err.response?.data?.detail,
            variant: 'destructive',
          });
        },
      }
    );
  };

  const initials = user
    ? `${user.firstName[0]}${user.lastName[0]}`.toUpperCase()
    : '';

  return (
    <main className="container mx-auto px-4 py-8 max-w-2xl">
      <PageHeader title="Mi perfil" />

      <div className="space-y-6">
        <Card>
          <CardHeader>
            <div className="flex items-center gap-4">
              <Avatar className="h-16 w-16">
                <AvatarFallback className="text-lg bg-slate-100">
                  {initials}
                </AvatarFallback>
              </Avatar>
              <div>
                <CardTitle className="text-lg">
                  {user?.firstName} {user?.lastName}
                </CardTitle>
                <p className="text-sm text-slate-500">{user?.email}</p>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleProfileSubmit} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label>Nombre</Label>
                  <Input value={profile.firstName}
                    onChange={e => setProfile(p => ({ ...p, firstName: e.target.value }))} />
                </div>
                <div className="space-y-2">
                  <Label>Apellidos</Label>
                  <Input value={profile.lastName}
                    onChange={e => setProfile(p => ({ ...p, lastName: e.target.value }))} />
                </div>
              </div>
              <div className="space-y-2">
                <Label>Email</Label>
                <Input type="email" value={profile.email}
                  onChange={e => setProfile(p => ({ ...p, email: e.target.value }))} />
              </div>
              <div className="space-y-2">
                <Label>Teléfono</Label>
                <Input value={profile.phoneNumber}
                  onChange={e => setProfile(p => ({ ...p, phoneNumber: e.target.value }))}
                  placeholder="+34 612 345 678" />
              </div>
              <Button type="submit" disabled={isUpdating}>
                {isUpdating ? 'Guardando...' : 'Guardar cambios'}
              </Button>
            </form>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-base">Cambiar contraseña</CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handlePasswordSubmit} className="space-y-4">
              <div className="space-y-2">
                <Label>Contraseña actual</Label>
                <Input type="password"
                  value={passwords.currentPassword}
                  onChange={e => setPasswords(p => ({ ...p, currentPassword: e.target.value }))}
                  required />
              </div>
              <div className="space-y-2">
                <Label>Nueva contraseña</Label>
                <Input type="password"
                  value={passwords.newPassword}
                  onChange={e => setPasswords(p => ({ ...p, newPassword: e.target.value }))}
                  minLength={8} required />
              </div>
              <Button type="submit" variant="outline" disabled={isChangingPass}>
                {isChangingPass ? 'Cambiando...' : 'Cambiar contraseña'}
              </Button>
            </form>
          </CardContent>
        </Card>
      </div>
    </main>
  );
}
