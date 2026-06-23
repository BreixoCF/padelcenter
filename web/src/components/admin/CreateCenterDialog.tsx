'use client';
import { useState } from 'react';
import { useCreateCenter } from '@/lib/api/generated/centers/centers';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useToast } from '@/components/ui/use-toast';

export default function CreateCenterDialog({
  onClose, onSuccess,
}: {
  onClose: () => void; onSuccess: () => void;
}) {
  const { toast } = useToast();
  const [form, setForm] = useState({
    name: '', address: '', city: '',
    phoneNumber: '', email: '',
  });
  const { mutate, isPending } = useCreateCenter();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm(prev => ({ ...prev, [e.target.name]: e.target.value }));

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    mutate(
      { data: form },
      {
        onSuccess: () => {
          toast({ title: 'Centro creado correctamente' });
          onSuccess();
        },
        onError: (err: any) => {
          toast({
            title: 'Error al crear el centro',
            description: err.response?.data?.detail,
            variant: 'destructive',
          });
        },
      }
    );
  };

  return (
    <Dialog open onOpenChange={onClose}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Nuevo centro</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          {[
            { name: 'name', label: 'Nombre', required: true },
            { name: 'address', label: 'Dirección', required: true },
            { name: 'city', label: 'Ciudad', required: true },
            { name: 'email', label: 'Email', type: 'email', required: true },
            { name: 'phoneNumber', label: 'Teléfono' },
          ].map(field => (
            <div key={field.name} className="space-y-2">
              <Label htmlFor={field.name}>{field.label}</Label>
              <Input id={field.name} name={field.name}
                type={field.type ?? 'text'}
                value={(form as any)[field.name]}
                onChange={handleChange}
                required={field.required} />
            </div>
          ))}
          <DialogFooter>
            <Button type="button" variant="outline" onClick={onClose}>
              Cancelar
            </Button>
            <Button type="submit" disabled={isPending}>
              {isPending ? 'Creando...' : 'Crear centro'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
