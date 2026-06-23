'use client';
import { useState, useEffect } from 'react';
import { useCreateField } from '@/lib/api/generated/centers/centers';
import { useUpdateField } from '@/lib/api/generated/fields/fields';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Switch } from '@/components/ui/switch';
import {
  Select, SelectContent, SelectItem,
  SelectTrigger, SelectValue,
} from '@/components/ui/select';
import { useToast } from '@/components/ui/use-toast';

interface FieldFormDialogProps {
  centerId: string;
  field?: any;
  onClose: () => void;
  onSuccess: () => void;
}

export default function FieldFormDialog({ centerId, field, onClose, onSuccess }: FieldFormDialogProps) {
  const { toast } = useToast();
  const isEdit = !!field;

  const [form, setForm] = useState({
    name: '',
    type: 'INDOOR',
    pricePerHour: 0,
    isAvailable: true,
  });

  useEffect(() => {
    if (field) {
      setForm({
        name: field.name,
        type: field.type ?? 'INDOOR',
        pricePerHour: field.pricePerHour,
        isAvailable: field.isAvailable,
      });
    }
  }, [field]);

  const { mutate: createField, isPending: isCreating } = useCreateField();
  const { mutate: updateField, isPending: isUpdating } = useUpdateField();

  const isPending = isCreating || isUpdating;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (isEdit) {
      updateField(
        { id: field.fieldId, data: form },
        {
          onSuccess: () => {
            toast({ title: 'Pista actualizada' });
            onSuccess();
          },
          onError: (err: any) => {
            toast({
              title: 'Error al actualizar',
              description: err.response?.data?.detail,
              variant: 'destructive',
            });
          },
        }
      );
    } else {
      createField(
        { centerId, data: form },
        {
          onSuccess: () => {
            toast({ title: 'Pista creada' });
            onSuccess();
          },
          onError: (err: any) => {
            toast({
              title: 'Error al crear',
              description: err.response?.data?.detail,
              variant: 'destructive',
            });
          },
        }
      );
    }
  };

  return (
    <Dialog open onOpenChange={onClose}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>{isEdit ? 'Editar pista' : 'Nueva pista'}</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label>Nombre</Label>
            <Input value={form.name} required
              onChange={e => setForm(p => ({ ...p, name: e.target.value }))} />
          </div>
          <div className="space-y-2">
            <Label>Tipo</Label>
            <Select value={form.type} onValueChange={v => setForm(p => ({ ...p, type: v }))}>
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="INDOOR">Interior</SelectItem>
                <SelectItem value="OUTDOOR">Exterior</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-2">
            <Label>Precio por hora (€)</Label>
            <Input type="number" min={0} step={0.5}
              value={form.pricePerHour}
              onChange={e => setForm(p => ({ ...p, pricePerHour: parseFloat(e.target.value) }))} />
          </div>
          <div className="flex items-center justify-between">
            <Label>Disponible</Label>
            <Switch
              checked={form.isAvailable}
              onCheckedChange={v => setForm(p => ({ ...p, isAvailable: v }))}
            />
          </div>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={onClose}>
              Cancelar
            </Button>
            <Button type="submit" disabled={isPending}>
              {isPending
                ? (isEdit ? 'Guardando...' : 'Creando...')
                : (isEdit ? 'Guardar cambios' : 'Crear pista')}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
