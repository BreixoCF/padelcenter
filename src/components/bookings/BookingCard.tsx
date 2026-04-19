import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Calendar, Clock, Euro } from 'lucide-react';

const STATUS_LABELS: Record<string, string> = {
  CONFIRMED: 'Confirmada',
  CANCELLED: 'Cancelada',
  COMPLETED: 'Completada',
  PENDING: 'Pendiente',
};

const STATUS_VARIANTS: Record<string, 'default' | 'secondary' | 'destructive' | 'outline'> = {
  CONFIRMED: 'default',
  CANCELLED: 'destructive',
  COMPLETED: 'secondary',
  PENDING: 'outline',
};

interface BookingCardProps {
  booking: any;
  onCancel?: () => void;
}

export default function BookingCard({ booking, onCancel }: BookingCardProps) {
  const start = new Date(booking.startTime);
  const end = new Date(booking.endTime);

  return (
    <Card>
      <CardContent className="p-4">
        <div className="flex items-start justify-between">
          <div className="space-y-1.5">
            <p className="font-medium text-sm">{booking.field?.name ?? 'Pista'}</p>
            <div className="flex items-center gap-3 text-sm text-slate-600">
              <div className="flex items-center gap-1">
                <Calendar className="h-3.5 w-3.5" />
                <span>{format(start, 'd MMM yyyy', { locale: es })}</span>
              </div>
              <div className="flex items-center gap-1">
                <Clock className="h-3.5 w-3.5" />
                <span>{format(start, 'HH:mm')} — {format(end, 'HH:mm')}</span>
              </div>
              <div className="flex items-center gap-1">
                <Euro className="h-3.5 w-3.5" />
                <span>{booking.totalPrice}</span>
              </div>
            </div>
          </div>
          <Badge variant={STATUS_VARIANTS[booking.status] ?? 'outline'}>
            {STATUS_LABELS[booking.status] ?? booking.status}
          </Badge>
        </div>
        {onCancel && (
          <div className="mt-3 pt-3 border-t">
            <Button
              variant="outline"
              size="sm"
              onClick={onCancel}
              className="text-red-600 hover:text-red-700 border-red-200 hover:border-red-300">
              Cancelar reserva
            </Button>
          </div>
        )}
      </CardContent>
    </Card>
  );
}
