'use client';
import { useState } from 'react';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import { Button } from '@/components/ui/button';
import StatusBadge from '@/components/shared/StatusBadge';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';

interface BookingCardProps {
  readonly booking: any;
  readonly onCancel?: () => void;
  readonly onEdit?: () => void;
}

export default function BookingCard({ booking, onCancel, onEdit }: BookingCardProps) {
  const [open, setOpen] = useState(false);
  const start = new Date(booking.startTime);
  const end = new Date(booking.endTime);

  return (
    <>
      <div className="border-b border-zinc-100 py-3 px-0">
        <button
          type="button"
          className="w-full text-left"
          onClick={() => setOpen(true)}
        >
          <div className="flex items-start justify-between">
            <div>
              <p className="font-medium text-sm">
                {booking.field?.center?.name
                  ? `${booking.field.center.name} · ${booking.field.name}`
                  : (booking.field?.name ?? 'Pista')}
              </p>
              <div className="flex items-center gap-3 text-sm text-zinc-500 mt-0.5">
                <span>{format(start, 'd MMM yyyy', { locale: es })}</span>
                <span>{format(start, 'HH:mm')} — {format(end, 'HH:mm')}</span>
              </div>
            </div>
            <div className="flex flex-col items-end gap-1.5">
              <span className="font-medium text-sm">{booking.totalPrice}€</span>
              <StatusBadge status={booking.status} />
            </div>
          </div>
        </button>
        {(onCancel || onEdit) && (
          <div className="mt-2 flex gap-2">
            {onEdit && (
              <Button variant="outline" size="sm" onClick={onEdit}>
                Modificar reserva
              </Button>
            )}
            {onCancel && (
              <Button
                variant="outline"
                size="sm"
                onClick={onCancel}
                className="text-red-600 hover:text-red-700 border-red-200 hover:border-red-300 hover:bg-red-50">
                Cancelar reserva
              </Button>
            )}
          </div>
        )}
      </div>

      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>Detalle de reserva</DialogTitle>
          </DialogHeader>
          <div className="space-y-3 text-sm">
            <div className="flex justify-between">
              <span className="text-zinc-500">Pista</span>
              <span className="font-medium text-right">
                {booking.field?.center?.name
                  ? `${booking.field.center.name} · ${booking.field.name}`
                  : (booking.field?.name ?? 'Pista')}
              </span>
            </div>
            {booking.field?.type && (
              <div className="flex justify-between">
                <span className="text-zinc-500">Tipo</span>
                <span>{booking.field.type}</span>
              </div>
            )}
            <div className="flex justify-between">
              <span className="text-zinc-500">Fecha</span>
              <span>{format(start, "d 'de' MMMM yyyy", { locale: es })}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-zinc-500">Horario</span>
              <span>{format(start, 'HH:mm')} — {format(end, 'HH:mm')}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-zinc-500">Precio total</span>
              <span className="font-medium">{booking.totalPrice}€</span>
            </div>
            <div className="flex justify-between">
              <span className="text-zinc-500">Estado</span>
              <StatusBadge status={booking.status} />
            </div>
            {booking.bookedAt && (
              <div className="flex justify-between">
                <span className="text-zinc-500">Reservado el</span>
                <span className="text-zinc-400">
                  {format(new Date(booking.bookedAt), "d MMM yyyy 'a las' HH:mm", { locale: es })}
                </span>
              </div>
            )}
          </div>
        </DialogContent>
      </Dialog>
    </>
  );
}
