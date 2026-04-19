'use client';
import { useState } from 'react';
import { useListCenters, useGetTournamentsByCenter } from '@/lib/api/generated/centers/centers';
import { useCreateTournament, useDeleteTournament } from '@/lib/api/generated/tournaments/tournaments';
import { TournamentRequestFormat } from '@/lib/api/generated/models/tournamentRequestFormat';
import PageHeader from '@/components/shared/PageHeader';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
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
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Select, SelectContent, SelectItem,
  SelectTrigger, SelectValue,
} from '@/components/ui/select';
import { Plus, Trash2 } from 'lucide-react';
import { useToast } from '@/components/ui/use-toast';
import { format } from 'date-fns';

const FORMAT_LABELS: Record<string, string> = {
  ROUND_ROBIN: 'Round Robin',
  ELIMINATION: 'Eliminatoria',
  GROUPS_AND_ELIMINATION: 'Grupos + Eliminatoria',
};

const STATUS_VARIANTS: Record<string, any> = {
  DRAFT: 'secondary',
  REGISTRATION_OPEN: 'default',
  REGISTRATION_CLOSED: 'outline',
  IN_PROGRESS: 'default',
  COMPLETED: 'secondary',
  CANCELLED: 'destructive',
};

const STATUS_LABELS: Record<string, string> = {
  DRAFT: 'Borrador',
  REGISTRATION_OPEN: 'Inscripción abierta',
  REGISTRATION_CLOSED: 'Inscripción cerrada',
  IN_PROGRESS: 'En curso',
  COMPLETED: 'Finalizado',
  CANCELLED: 'Cancelado',
};

export default function AdminTournamentsPage() {
  const { toast } = useToast();
  const [selectedCenter, setSelectedCenter] = useState<string>('');
  const [showCreate, setShowCreate] = useState(false);
  const [deleteId, setDeleteId] = useState<string | null>(null);
  const [form, setForm] = useState<{
    name: string;
    format: TournamentRequestFormat;
    maxPairs: number;
    startDate: string;
    endDate: string;
  }>({
    name: '',
    format: TournamentRequestFormat.ROUND_ROBIN,
    maxPairs: 8,
    startDate: '',
    endDate: '',
  });

  const { data: centersData } = useListCenters({ page: 0, size: 50 });
  const { data: tournamentsData, refetch } = useGetTournamentsByCenter(
    selectedCenter,
    { page: 0, size: 20 },
    { query: { enabled: !!selectedCenter } }
  );
  const { mutate: createTournament, isPending } = useCreateTournament();
  const { mutate: deleteTournament, isPending: isDeleting } = useDeleteTournament();

  const centers = centersData?.content ?? [];
  const tournaments = tournamentsData?.content ?? [];

  const handleCreate = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedCenter) {
      toast({ title: 'Selecciona un centro', variant: 'destructive' });
      return;
    }
    createTournament(
      {
        data: {
          centerId: selectedCenter,
          name: form.name,
          format: form.format,
          maxPairs: form.maxPairs,
          startDate: form.startDate,
          endDate: form.endDate,
        },
      },
      {
        onSuccess: () => {
          toast({ title: 'Torneo creado' });
          refetch();
          setShowCreate(false);
          setForm({ name: '', format: TournamentRequestFormat.ROUND_ROBIN, maxPairs: 8, startDate: '', endDate: '' });
        },
        onError: (err: any) => {
          toast({
            title: 'Error al crear el torneo',
            description: err.response?.data?.detail,
            variant: 'destructive',
          });
        },
      }
    );
  };

  const handleDelete = () => {
    if (!deleteId) return;
    deleteTournament(
      { tournamentId: deleteId },
      {
        onSuccess: () => {
          toast({ title: 'Torneo eliminado' });
          refetch();
          setDeleteId(null);
        },
        onError: (err: any) => {
          toast({
            title: 'Error al eliminar el torneo',
            description: err.response?.data?.detail,
            variant: 'destructive',
          });
          setDeleteId(null);
        },
      }
    );
  };

  return (
    <main className="container mx-auto px-4 py-8 max-w-6xl">
      <PageHeader
        title="Torneos"
        description="Gestiona los torneos de los centros"
        action={
          <Button size="sm" onClick={() => setShowCreate(true)}>
            <Plus className="h-4 w-4 mr-1" />
            Nuevo torneo
          </Button>
        }
      />

      <div className="mb-6 max-w-xs">
        <Select onValueChange={setSelectedCenter}>
          <SelectTrigger>
            <SelectValue placeholder="Filtrar por centro" />
          </SelectTrigger>
          <SelectContent>
            {centers.map((c: any) => (
              <SelectItem key={c.centerId} value={c.centerId}>
                {c.name}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      {selectedCenter && (
        <div className="border rounded-lg overflow-hidden">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Nombre</TableHead>
                <TableHead>Formato</TableHead>
                <TableHead>Estado</TableHead>
                <TableHead>Parejas</TableHead>
                <TableHead>Fechas</TableHead>
                <TableHead className="w-16">Acciones</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {tournaments.map((t: any) => (
                <TableRow key={t.tournamentId}>
                  <TableCell className="font-medium">{t.name}</TableCell>
                  <TableCell className="text-slate-500">
                    {FORMAT_LABELS[t.format] ?? t.format}
                  </TableCell>
                  <TableCell>
                    <Badge variant={STATUS_VARIANTS[t.status] ?? 'outline'}>
                      {STATUS_LABELS[t.status] ?? t.status}
                    </Badge>
                  </TableCell>
                  <TableCell>{t.maxPairs}</TableCell>
                  <TableCell className="text-slate-500 text-sm">
                    {format(new Date(t.startDate), 'dd/MM/yy')} — {format(new Date(t.endDate), 'dd/MM/yy')}
                  </TableCell>
                  <TableCell>
                    <Button size="icon" variant="ghost"
                      onClick={() => setDeleteId(t.tournamentId)}
                      className="text-red-500 hover:text-red-600">
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
              {tournaments.length === 0 && (
                <TableRow>
                  <TableCell colSpan={6} className="text-center text-slate-400 py-8">
                    No hay torneos para este centro
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </div>
      )}

      <AlertDialog open={!!deleteId} onOpenChange={() => setDeleteId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>¿Eliminar torneo?</AlertDialogTitle>
            <AlertDialogDescription>
              Esta acción no se puede deshacer.
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
        <Dialog open onOpenChange={() => setShowCreate(false)}>
          <DialogContent className="max-w-md">
            <DialogHeader>
              <DialogTitle>Nuevo torneo</DialogTitle>
            </DialogHeader>
            <form onSubmit={handleCreate} className="space-y-4">
              <div className="space-y-2">
                <Label>Centro</Label>
                <Select value={selectedCenter} onValueChange={setSelectedCenter}>
                  <SelectTrigger>
                    <SelectValue placeholder="Selecciona un centro" />
                  </SelectTrigger>
                  <SelectContent>
                    {centers.map((c: any) => (
                      <SelectItem key={c.centerId} value={c.centerId}>
                        {c.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label>Nombre</Label>
                <Input value={form.name}
                  onChange={e => setForm(p => ({ ...p, name: e.target.value }))}
                  required />
              </div>
              <div className="space-y-2">
                <Label>Formato</Label>
                <Select
                  value={form.format}
                  onValueChange={v => setForm(p => ({ ...p, format: v as TournamentRequestFormat }))}>
                  <SelectTrigger><SelectValue /></SelectTrigger>
                  <SelectContent>
                    {Object.entries(FORMAT_LABELS).map(([value, label]) => (
                      <SelectItem key={value} value={value}>{label}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label>Máximo de parejas</Label>
                <Input type="number" min={4} value={form.maxPairs}
                  onChange={e => setForm(p => ({ ...p, maxPairs: Number(e.target.value) }))}
                  required />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label>Fecha inicio</Label>
                  <Input type="date" value={form.startDate}
                    onChange={e => setForm(p => ({ ...p, startDate: e.target.value }))}
                    required />
                </div>
                <div className="space-y-2">
                  <Label>Fecha fin</Label>
                  <Input type="date" value={form.endDate}
                    onChange={e => setForm(p => ({ ...p, endDate: e.target.value }))}
                    required />
                </div>
              </div>
              <DialogFooter>
                <Button type="button" variant="outline" onClick={() => setShowCreate(false)}>
                  Cancelar
                </Button>
                <Button type="submit" disabled={isPending}>
                  {isPending ? 'Creando...' : 'Crear torneo'}
                </Button>
              </DialogFooter>
            </form>
          </DialogContent>
        </Dialog>
      )}
    </main>
  );
}
