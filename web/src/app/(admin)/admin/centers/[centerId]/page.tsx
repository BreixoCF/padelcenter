'use client';
import { useState } from 'react';
import { useParams } from 'next/navigation';
import { useGetCenterById, useListCenterFields, useCreateField } from '@/lib/api/generated/centers/centers';
import { useDeleteField, useUpdateFieldAvailability } from '@/lib/api/generated/fields/fields';
import PageHeader from '@/components/shared/PageHeader';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Switch } from '@/components/ui/switch';
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
import { Pencil, Trash2, Plus, ArrowLeft } from 'lucide-react';
import { useToast } from '@/components/ui/use-toast';
import FieldFormDialog from '@/components/admin/FieldFormDialog';
import Link from 'next/link';

export default function AdminCenterDetailPage() {
  const params = useParams();
  const centerId = params.centerId as string;
  const { toast } = useToast();

  const [deleteId, setDeleteId] = useState<string | null>(null);
  const [editField, setEditField] = useState<any | null>(null);
  const [showCreate, setShowCreate] = useState(false);

  const { data: center } = useGetCenterById(centerId);
  const { data: fieldsData, isLoading, refetch } = useListCenterFields(centerId, { page: 0, size: 50 });
  const { mutate: deleteField, isPending: isDeleting } = useDeleteField();
  const { mutate: updateAvailability } = useUpdateFieldAvailability();

  const fields = fieldsData?.content ?? [];

  const handleDelete = () => {
    if (!deleteId) return;
    deleteField(
      { id: deleteId },
      {
        onSuccess: () => {
          toast({ title: 'Pista eliminada' });
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

  const handleToggleAvailability = (fieldId: string, current: boolean) => {
    updateAvailability(
      { fieldId, data: { available: !current } },
      {
        onSuccess: () => {
          refetch();
          toast({ title: !current ? 'Pista habilitada' : 'Pista deshabilitada' });
        },
      }
    );
  };

  return (
    <main className="container mx-auto px-4 py-8 max-w-6xl">
      <div className="mb-2">
        <Link href="/admin/centers"
          className="text-sm text-slate-500 hover:text-slate-900 flex items-center gap-1">
          <ArrowLeft className="h-3.5 w-3.5" />
          Volver a centros
        </Link>
      </div>

      <PageHeader
        title={center?.name ?? 'Centro'}
        description={`${fields.length} pistas · ${center?.city ?? ''}`}
        action={
          <Button size="sm" onClick={() => setShowCreate(true)}>
            <Plus className="h-4 w-4 mr-1" />
            Nueva pista
          </Button>
        }
      />

      <div className="border rounded-lg overflow-hidden">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Nombre</TableHead>
              <TableHead>Tipo</TableHead>
              <TableHead>Precio/hora</TableHead>
              <TableHead>Disponible</TableHead>
              <TableHead className="w-24">Acciones</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              <TableRow>
                <TableCell colSpan={5} className="text-center py-8 text-slate-400">
                  Cargando...
                </TableCell>
              </TableRow>
            ) : fields.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} className="text-center py-8 text-slate-400">
                  No hay pistas. Crea la primera.
                </TableCell>
              </TableRow>
            ) : (
              fields.map((field: any) => (
                <TableRow key={field.fieldId}>
                  <TableCell className="font-medium">{field.name}</TableCell>
                  <TableCell>
                    <Badge variant="outline">
                      {field.type === 'INDOOR' ? 'Interior' : 'Exterior'}
                    </Badge>
                  </TableCell>
                  <TableCell>{field.pricePerHour}€</TableCell>
                  <TableCell>
                    <Switch
                      checked={field.isAvailable}
                      onCheckedChange={() => handleToggleAvailability(field.fieldId, field.isAvailable)}
                    />
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center gap-1">
                      <Button size="icon" variant="ghost" onClick={() => setEditField(field)}>
                        <Pencil className="h-4 w-4" />
                      </Button>
                      <Button size="icon" variant="ghost"
                        onClick={() => setDeleteId(field.fieldId)}
                        className="text-red-500 hover:text-red-600">
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      <AlertDialog open={!!deleteId} onOpenChange={() => setDeleteId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>¿Eliminar pista?</AlertDialogTitle>
            <AlertDialogDescription>
              Esta acción desactivará la pista permanentemente.
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

      {(showCreate || editField) && (
        <FieldFormDialog
          centerId={centerId}
          field={editField}
          onClose={() => { setShowCreate(false); setEditField(null); }}
          onSuccess={() => { refetch(); setShowCreate(false); setEditField(null); }}
        />
      )}
    </main>
  );
}
