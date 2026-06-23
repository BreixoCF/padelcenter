'use client';
import { useState } from 'react';
import { useListCenters, useDeleteCenter } from '@/lib/api/generated/centers/centers';
import PageHeader from '@/components/shared/PageHeader';
import { Button } from '@/components/ui/button';
import {
  Table, TableBody, TableCell, TableHead,
  TableHeader, TableRow,
} from '@/components/ui/table';
import {
  AlertDialog, AlertDialogAction, AlertDialogCancel,
  AlertDialogContent, AlertDialogDescription,
  AlertDialogFooter, AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import { Trash2, Plus, Eye, Settings } from 'lucide-react';
import Link from 'next/link';
import { useToast } from '@/components/ui/use-toast';
import { GridSkeleton } from '@/components/shared/LoadingState';
import CreateCenterDialog from '@/components/admin/CreateCenterDialog';

export default function AdminCentersPage() {
  const [page, setPage] = useState(0);
  const [deleteId, setDeleteId] = useState<string | null>(null);
  const [showCreate, setShowCreate] = useState(false);
  const { toast } = useToast();

  const { data, isLoading, refetch } = useListCenters({ page, size: 10 });
  const { mutate: deleteCenter, isPending: isDeleting } = useDeleteCenter();

  const centers = data?.content ?? [];

  const handleDelete = () => {
    if (!deleteId) return;
    deleteCenter(
      { centerId: deleteId },
      {
        onSuccess: () => {
          toast({ title: 'Centro eliminado' });
          refetch();
          setDeleteId(null);
        },
        onError: () => {
          toast({ title: 'Error al eliminar', variant: 'destructive' });
          setDeleteId(null);
        },
      }
    );
  };

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
            <TableRow className="border-b border-zinc-100">
              <TableHead className="text-[11px] uppercase tracking-wide text-zinc-400">Nombre</TableHead>
              <TableHead className="text-[11px] uppercase tracking-wide text-zinc-400">Ciudad</TableHead>
              <TableHead className="text-[11px] uppercase tracking-wide text-zinc-400">Email</TableHead>
              <TableHead className="text-[11px] uppercase tracking-wide text-zinc-400">Teléfono</TableHead>
              <TableHead className="w-24 text-[11px] uppercase tracking-wide text-zinc-400">Acciones</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {centers.map((center: any) => (
              <TableRow key={center.centerId} className="border-b border-zinc-50 hover:bg-zinc-50/50">
                <TableCell className="font-medium">{center.name}</TableCell>
                <TableCell>
                  <span className="bg-zinc-100 text-zinc-600 rounded-full px-2 py-0.5 text-xs font-medium">
                    {center.city}
                  </span>
                </TableCell>
                <TableCell className="text-zinc-500">{center.email}</TableCell>
                <TableCell className="text-zinc-500">{center.phoneNumber}</TableCell>
                <TableCell>
                  <div className="flex items-center gap-1">
                    <Link href={`/admin/centers/${center.centerId}`}>
                      <Button size="icon" variant="ghost">
                        <Settings className="h-4 w-4" />
                      </Button>
                    </Link>
                    <Link href={`/centers/${center.centerId}`}>
                      <Button size="icon" variant="ghost">
                        <Eye className="h-4 w-4" />
                      </Button>
                    </Link>
                    <Button size="icon" variant="ghost"
                      onClick={() => setDeleteId(center.centerId)}
                      className="text-red-500 hover:text-red-600">
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </div>
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

      <AlertDialog open={!!deleteId} onOpenChange={() => setDeleteId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>¿Eliminar centro?</AlertDialogTitle>
            <AlertDialogDescription>
              Esta acción no se puede deshacer. El centro quedará desactivado.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancelar</AlertDialogCancel>
            <AlertDialogAction onClick={handleDelete}
              disabled={isDeleting}
              className="bg-red-600 hover:bg-red-700">
              {isDeleting ? 'Eliminando...' : 'Eliminar'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {showCreate && (
        <CreateCenterDialog
          onClose={() => setShowCreate(false)}
          onSuccess={() => { refetch(); setShowCreate(false); }}
        />
      )}
    </main>
  );
}
