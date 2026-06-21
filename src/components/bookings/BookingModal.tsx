'use client';
import { useState } from 'react';
import { format, addDays } from 'date-fns';
import { es } from 'date-fns/locale';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/lib/auth/store';
import { useCreateBooking } from '@/hooks/useCenters';
import { useUpdateBooking } from '@/lib/api/generated/bookings/bookings';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Calendar } from '@/components/ui/calendar';
import { useToast } from '@/components/ui/use-toast';
import AvailabilityGrid, { type Slot } from './AvailabilityGrid';

interface BookingModalProps {
  fieldId: string;
  pricePerHour?: number;
  onClose: () => void;
  existingBooking?: { bookingId: number };
}

function buttonLabel(isPending: boolean, isEditMode: boolean, isAuthenticated: boolean): string {
  if (isPending) return isEditMode ? 'Guardando...' : 'Reservando...';
  if (!isAuthenticated) return 'Iniciar sesión para reservar';
  return isEditMode ? 'Confirmar cambio' : 'Confirmar reserva';
}

export default function BookingModal({
  fieldId,
  pricePerHour = 0,
  onClose,
  existingBooking,
}: BookingModalProps) {
  const router = useRouter();
  const { isAuthenticated } = useAuthStore();
  const { toast } = useToast();
  const [date, setDate] = useState<Date>(new Date());
  const [selectedSlot, setSelectedSlot] = useState<Slot | null>(null);

  const isEditMode = !!existingBooking;

  const { mutate: createBooking, isPending: isCreating } = useCreateBooking();
  const { mutate: updateBooking, isPending: isUpdating } = useUpdateBooking();
  const isPending = isCreating || isUpdating;

  const handleBook = () => {
    if (!isAuthenticated) {
      router.push('/login');
      return;
    }
    if (!selectedSlot) return;

    if (isEditMode) {
      updateBooking(
        {
          bookingId: existingBooking.bookingId,
          data: {
            startTime: selectedSlot.start,
            endTime: selectedSlot.end,
            totalPrice: pricePerHour,
          },
        },
        {
          onSuccess: () => {
            toast({
              title: 'Reserva modificada',
              description: `Nueva hora: ${format(new Date(selectedSlot.start), 'HH:mm')} — ${format(new Date(selectedSlot.end), 'HH:mm')}`,
            });
            onClose();
          },
          onError: (error: any) => {
            toast({
              title: 'Error al modificar',
              description: error.response?.data?.detail ?? 'Inténtalo de nuevo',
              variant: 'destructive',
            });
          },
        }
      );
      return;
    }

    createBooking(
      {
        data: {
          fieldId,
          startTime: selectedSlot.start,
          endTime: selectedSlot.end,
          totalPrice: pricePerHour,
        },
      },
      {
        onSuccess: () => {
          toast({
            title: 'Reserva confirmada',
            description: `Pista reservada para el ${format(date, "d 'de' MMMM", { locale: es })} a las ${format(new Date(selectedSlot.start), 'HH:mm')}`,
          });
          onClose();
        },
        onError: (error: any) => {
          const detail = error.response?.data?.detail;
          toast({
            title: 'Error al reservar',
            description: detail ?? 'Inténtalo de nuevo',
            variant: 'destructive',
          });
        },
      }
    );
  };

  return (
    <Dialog open onOpenChange={onClose}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle>{isEditMode ? 'Modificar reserva' : 'Selecciona fecha y hora'}</DialogTitle>
        </DialogHeader>

        <div className="space-y-4">
          <Calendar
            mode="single"
            selected={date}
            onSelect={(d) => {
              if (d) {
                setDate(d);
                setSelectedSlot(null);
              }
            }}
            disabled={(d) => d < new Date() || d > addDays(new Date(), 30)}
            locale={es}
            className="rounded-md border mx-auto"
          />
          <AvailabilityGrid
            fieldId={fieldId}
            date={date}
            selectedSlot={selectedSlot}
            onSelectSlot={setSelectedSlot}
          />
        </div>

        <DialogFooter className="flex items-center justify-between gap-2">
          {selectedSlot && (
            <p className="text-sm text-slate-600">
              {format(new Date(selectedSlot.start), 'HH:mm')} —{' '}
              {format(new Date(selectedSlot.end), 'HH:mm')} · {pricePerHour}€
            </p>
          )}
          <div className="flex gap-2 ml-auto">
            <Button variant="outline" onClick={onClose}>
              Cancelar
            </Button>
            <Button onClick={handleBook} disabled={!selectedSlot || isPending}>
              {buttonLabel(isPending, isEditMode, isAuthenticated)}
            </Button>
          </div>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
