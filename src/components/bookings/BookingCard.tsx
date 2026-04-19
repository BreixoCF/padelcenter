import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import { Button } from '@/components/ui/button';
import StatusBadge from '@/components/shared/StatusBadge';

interface BookingCardProps {
  booking: any;
  onCancel?: () => void;
}

export default function BookingCard({ booking, onCancel }: BookingCardProps) {
  const start = new Date(booking.startTime);
  const end = new Date(booking.endTime);

  return (
    <div className="border-b border-zinc-100 py-3 px-0">
      <div className="flex items-start justify-between">
        <div>
          <p className="font-medium text-sm">{booking.field?.name ?? 'Pista'}</p>
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
      {onCancel && (
        <div className="mt-2">
          <Button
            variant="outline"
            size="sm"
            onClick={onCancel}
            className="text-red-600 hover:text-red-700 border-red-200 hover:border-red-300 hover:bg-red-50">
            Cancelar reserva
          </Button>
        </div>
      )}
    </div>
  );
}
