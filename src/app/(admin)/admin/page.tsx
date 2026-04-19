'use client';
import { useListCenters } from '@/lib/api/generated/centers/centers';
import { useListUsers } from '@/lib/api/generated/users/users';
import PageHeader from '@/components/shared/PageHeader';
import { Card, CardContent } from '@/components/ui/card';
import { Building2, Users, Trophy, Calendar } from 'lucide-react';
import Link from 'next/link';
import { Button } from '@/components/ui/button';

function StatCard({ icon: Icon, label, value, href }: {
  icon: any; label: string; value: number; href: string;
}) {
  return (
    <Link href={href}>
      <Card className="hover:shadow-md transition-shadow cursor-pointer">
        <CardContent className="p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-slate-500">{label}</p>
              <p className="text-2xl font-semibold mt-1">{value}</p>
            </div>
            <Icon className="h-8 w-8 text-slate-300" />
          </div>
        </CardContent>
      </Card>
    </Link>
  );
}

export default function AdminDashboard() {
  const { data: centersData } = useListCenters({ page: 0, size: 1 });
  const { data: usersData } = useListUsers({ page: 0, size: 1 });

  return (
    <main className="container mx-auto px-4 py-8 max-w-6xl">
      <PageHeader
        title="Panel de administración"
        description="Gestiona centros, pistas y torneos"
      />
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <StatCard icon={Building2} label="Centros"
          value={centersData?.totalElements ?? 0}
          href="/admin/centers" />
        <StatCard icon={Users} label="Usuarios"
          value={usersData?.totalElements ?? 0}
          href="/admin/users" />
        <StatCard icon={Trophy} label="Torneos"
          value={0} href="/admin/tournaments" />
        <StatCard icon={Calendar} label="Reservas hoy"
          value={0} href="/admin/bookings" />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card>
          <CardContent className="p-6">
            <h3 className="font-medium mb-3">Centros</h3>
            <p className="text-sm text-slate-500 mb-4">
              Gestiona los centros de pádel y sus pistas
            </p>
            <Link href="/admin/centers">
              <Button size="sm" variant="outline" className="w-full">
                Gestionar centros
              </Button>
            </Link>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-6">
            <h3 className="font-medium mb-3">Torneos</h3>
            <p className="text-sm text-slate-500 mb-4">
              Crea y gestiona torneos por centros
            </p>
            <Link href="/admin/tournaments">
              <Button size="sm" variant="outline" className="w-full">
                Gestionar torneos
              </Button>
            </Link>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-6">
            <h3 className="font-medium mb-3">Usuarios</h3>
            <p className="text-sm text-slate-500 mb-4">
              Consulta y gestiona los usuarios registrados
            </p>
            <Link href="/admin/users">
              <Button size="sm" variant="outline" className="w-full">
                Ver usuarios
              </Button>
            </Link>
          </CardContent>
        </Card>
      </div>
    </main>
  );
}
