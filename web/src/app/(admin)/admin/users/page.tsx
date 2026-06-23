'use client';
import { useState } from 'react';
import {
  useListUsers,
  useDeleteUser,
  useUpdateUserRoles,
} from '@/lib/api/generated/users/users';
import { useListCenters } from '@/lib/api/generated/centers/centers';
import { CenterRoleRequestRole } from '@/lib/api/generated/models/centerRoleRequestRole';
import PageHeader from '@/components/shared/PageHeader';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
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
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter,
} from '@/components/ui/dialog';
import {
  Select, SelectContent, SelectItem,
  SelectTrigger, SelectValue,
} from '@/components/ui/select';
import { useToast } from '@/components/ui/use-toast';
import { useQueryClient } from '@tanstack/react-query';
import { Trash2, ShieldCheck, Plus, X } from 'lucide-react';

type RoleEntry = { centerId: string; role: CenterRoleRequestRole };

export default function AdminUsersPage() {
  const { toast } = useToast();
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [deleteId, setDeleteId] = useState<string | null>(null);
  const [rolesUser, setRolesUser] = useState<{ id: string; name: string } | null>(null);
  const [roleEntries, setRoleEntries] = useState<RoleEntry[]>([]);

  const { data } = useListUsers({ page, size: 20 });
  const { data: centersData } = useListCenters({ page: 0, size: 50 });
  const { mutate: deleteUser, isPending: isDeleting } = useDeleteUser();
  const { mutate: updateRoles, isPending: isUpdatingRoles } = useUpdateUserRoles();

  const users = data?.content ?? [];
  const centers = centersData?.content ?? [];

  const queryKey = ['/api/v1/users'];

  const handleDelete = () => {
    if (!deleteId) return;
    deleteUser(
      { id: deleteId },
      {
        onSuccess: () => {
          toast({ title: 'Usuario eliminado' });
          queryClient.invalidateQueries({ queryKey });
          setDeleteId(null);
        },
        onError: (err: any) => {
          toast({ title: 'Error al eliminar', description: err.response?.data?.detail, variant: 'destructive' });
          setDeleteId(null);
        },
      }
    );
  };

  const openRolesDialog = (user: any) => {
    setRolesUser({ id: user.userId, name: `${user.firstName} ${user.lastName}` });
    setRoleEntries(
      (user.centerRoles ?? []).map((r: any) => ({ centerId: r.centerId, role: r.role as CenterRoleRequestRole }))
    );
  };

  const addRoleEntry = () => {
    setRoleEntries(prev => [...prev, { centerId: '', role: CenterRoleRequestRole.USER }]);
  };

  const removeRoleEntry = (index: number) => {
    setRoleEntries(prev => prev.filter((_, i) => i !== index));
  };

  const updateRoleEntry = (index: number, patch: Partial<RoleEntry>) => {
    setRoleEntries(prev => prev.map((e, i) => i === index ? { ...e, ...patch } : e));
  };

  const handleSaveRoles = () => {
    if (!rolesUser) return;
    const valid = roleEntries.filter(e => e.centerId);
    updateRoles(
      { id: rolesUser.id, data: valid },
      {
        onSuccess: () => {
          toast({ title: 'Roles actualizados' });
          queryClient.invalidateQueries({ queryKey });
          setRolesUser(null);
        },
        onError: (err: any) => {
          toast({ title: 'Error al actualizar roles', description: err.response?.data?.detail, variant: 'destructive' });
        },
      }
    );
  };

  return (
    <main className="container mx-auto px-4 py-8 max-w-5xl">
      <PageHeader
        title="Usuarios"
        description={`${data?.totalElements ?? 0} usuarios registrados`}
      />

      <div className="border rounded-lg overflow-hidden">
        <Table>
          <TableHeader>
            <TableRow className="border-b border-zinc-100">
              <TableHead className="text-[11px] uppercase tracking-wide text-zinc-400">Nombre</TableHead>
              <TableHead className="text-[11px] uppercase tracking-wide text-zinc-400">Email</TableHead>
              <TableHead className="text-[11px] uppercase tracking-wide text-zinc-400">Teléfono</TableHead>
              <TableHead className="text-[11px] uppercase tracking-wide text-zinc-400">Roles</TableHead>
              <TableHead className="text-[11px] uppercase tracking-wide text-zinc-400">Acciones</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {users.map((u: any) => (
              <TableRow key={u.userId} className="border-b border-zinc-50 hover:bg-zinc-50/50">
                <TableCell className="font-medium">{u.firstName} {u.lastName}</TableCell>
                <TableCell className="text-zinc-500 text-sm">{u.email}</TableCell>
                <TableCell className="text-zinc-500 text-sm">{u.phoneNumber ?? '—'}</TableCell>
                <TableCell className="text-zinc-500 text-sm">
                  {(u.centerRoles ?? []).length > 0
                    ? (u.centerRoles as any[]).map((r: any) => r.role).join(', ')
                    : '—'}
                </TableCell>
                <TableCell>
                  <div className="flex items-center gap-1">
                    <Button size="icon" variant="ghost"
                      onClick={() => openRolesDialog(u)}
                      className="text-blue-500 hover:text-blue-600">
                      <ShieldCheck className="h-4 w-4" />
                    </Button>
                    <Button size="icon" variant="ghost"
                      onClick={() => setDeleteId(u.userId)}
                      className="text-red-500 hover:text-red-600">
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </div>
                </TableCell>
              </TableRow>
            ))}
            {users.length === 0 && (
              <TableRow>
                <TableCell colSpan={5} className="text-center text-slate-400 py-8">
                  No hay usuarios
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>

      {data && data.totalPages > 1 && (
        <div className="flex justify-center gap-2 mt-4">
          <Button variant="outline" size="sm" disabled={page === 0} onClick={() => setPage(p => p - 1)}>
            Anterior
          </Button>
          <span className="text-sm text-slate-500 flex items-center px-2">
            {page + 1} / {data.totalPages}
          </span>
          <Button variant="outline" size="sm" disabled={page >= data.totalPages - 1} onClick={() => setPage(p => p + 1)}>
            Siguiente
          </Button>
        </div>
      )}

      <AlertDialog open={!!deleteId} onOpenChange={() => setDeleteId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>¿Eliminar usuario?</AlertDialogTitle>
            <AlertDialogDescription>Esta acción no se puede deshacer.</AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancelar</AlertDialogCancel>
            <AlertDialogAction onClick={handleDelete} disabled={isDeleting} className="bg-red-600 hover:bg-red-700">
              {isDeleting ? 'Eliminando...' : 'Eliminar'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {rolesUser && (
        <Dialog open onOpenChange={() => setRolesUser(null)}>
          <DialogContent className="max-w-md">
            <DialogHeader>
              <DialogTitle>Roles de {rolesUser.name}</DialogTitle>
            </DialogHeader>
            <div className="space-y-3">
              {roleEntries.map((entry, i) => (
                <div key={i} className="flex gap-2 items-end">
                  <div className="flex-1 space-y-1">
                    <Label className="text-xs">Centro</Label>
                    <Select value={entry.centerId} onValueChange={v => updateRoleEntry(i, { centerId: v })}>
                      <SelectTrigger>
                        <SelectValue placeholder="Selecciona centro" />
                      </SelectTrigger>
                      <SelectContent>
                        {centers.map((c: any) => (
                          <SelectItem key={c.centerId} value={c.centerId}>{c.name}</SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                  <div className="w-32 space-y-1">
                    <Label className="text-xs">Rol</Label>
                    <Select value={entry.role} onValueChange={v => updateRoleEntry(i, { role: v as CenterRoleRequestRole })}>
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {Object.values(CenterRoleRequestRole).map(r => (
                          <SelectItem key={r} value={r}>{r}</SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                  <Button type="button" size="icon" variant="ghost"
                    onClick={() => removeRoleEntry(i)}
                    className="text-red-500 hover:text-red-600 mb-0.5">
                    <X className="h-4 w-4" />
                  </Button>
                </div>
              ))}
              <Button type="button" variant="outline" size="sm" onClick={addRoleEntry} className="gap-1">
                <Plus className="h-3.5 w-3.5" />
                Añadir rol
              </Button>
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setRolesUser(null)}>Cancelar</Button>
              <Button onClick={handleSaveRoles} disabled={isUpdatingRoles}>
                {isUpdatingRoles ? 'Guardando...' : 'Guardar roles'}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      )}
    </main>
  );
}
