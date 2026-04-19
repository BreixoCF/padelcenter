'use client';
import { useState } from 'react';
import { useListCenters } from '@/lib/api/generated/centers/centers';
import PageHeader from '@/components/shared/PageHeader';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import {
  Table, TableBody, TableCell, TableHead,
  TableHeader, TableRow,
} from '@/components/ui/table';
import { Plus, Eye } from 'lucide-react';
import Link from 'next/link';
import { GridSkeleton } from '@/components/shared/LoadingState';
import CreateCenterDialog from '@/components/admin/CreateCenterDialog';

export default function AdminCentersPage() {
  const [page, setPage] = useState(0);
  const [showCreate, setShowCreate] = useState(false);

  const { data, isLoading, refetch } = useListCenters({ page, size: 10 });
  const centers = data?.content ?? [];

  if (isLoading) return (
    <main className="container mx-auto px-4 py-8 max-w-6xl">
      <PageHeader title="Centros" />
      <GridSkeleton count={3} />
    </main>
  );

  return (
    <main className="container mx-auto px-4 py-8 max-w-6xl">
      <PageHeader
        title="Centros"
        description={`${data?.totalElements ?? 0} centros registrados`}
        action={
          <Button size="sm" onClick={() => setShowCreate(true)}>
            <Plus className="h-4 w-4 mr-1" />
            Nuevo centro
          </Button>
        }
      />

      <div className="border rounded-lg overflow-hidden">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Nombre</TableHead>
              <TableHead>Ciudad</TableHead>
              <TableHead>Email</TableHead>
              <TableHead>Teléfono</TableHead>
              <TableHead className="w-24">Acciones</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {centers.map((center: any) => (
              <TableRow key={center.centerId}>
                <TableCell className="font-medium">{center.name}</TableCell>
                <TableCell>
                  <Badge variant="secondary">{center.city}</Badge>
                </TableCell>
                <TableCell className="text-slate-500">{center.email}</TableCell>
                <TableCell className="text-slate-500">{center.phoneNumber}</TableCell>
                <TableCell>
                  <Link href={`/centers/${center.centerId}`}>
                    <Button size="icon" variant="ghost">
                      <Eye className="h-4 w-4" />
                    </Button>
                  </Link>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      {data && data.totalPages > 1 && (
        <div className="flex justify-center gap-2 mt-4">
          <Button variant="outline" size="sm"
            disabled={page === 0}
            onClick={() => setPage(p => p - 1)}>
            Anterior
          </Button>
          <span className="text-sm text-slate-500 flex items-center px-2">
            {page + 1} / {data.totalPages}
          </span>
          <Button variant="outline" size="sm"
            disabled={page >= data.totalPages - 1}
            onClick={() => setPage(p => p + 1)}>
            Siguiente
          </Button>
        </div>
      )}

      {showCreate && (
        <CreateCenterDialog
          onClose={() => setShowCreate(false)}
          onSuccess={() => { refetch(); setShowCreate(false); }}
        />
      )}
    </main>
  );
}
