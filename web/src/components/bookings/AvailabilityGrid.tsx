'use client';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import { useFieldAvailability } from '@/hooks/useCenters';
import { Skeleton } from '@/components/ui/skeleton';
import { cn } from '@/lib/utils';

export interface Slot {
  start: string;
  end: string;
  available: boolean;
}

interface AvailabilityGridProps {
  fieldId: string;
  date: Date;
  selectedSlot: Slot | null;
  onSelectSlot: (slot: Slot) => void;
}

export default function AvailabilityGrid({
  fieldId,
  date,
  selectedSlot,
  onSelectSlot,
}: AvailabilityGridProps) {
  const dateStr = format(date, 'yyyy-MM-dd');
  const { data, isLoading } = useFieldAvailability(fieldId, { date: dateStr });

  if (isLoading)
    return (
      <div className="grid grid-cols-4 gap-2">
        {Array.from({ length: 13 }).map((_, i) => (
          <Skeleton key={i} className="h-10 rounded-md" />
        ))}
      </div>
    );

  const slots: Slot[] = data?.slots ?? [];

  return (
    <div>
      <p className="text-sm text-slate-500 mb-3">
        {format(date, "EEEE d 'de' MMMM", { locale: es })}
      </p>
      {slots.length === 0 ? (
        <p className="text-sm text-slate-400 py-4 text-center">
          No hay franjas disponibles para este día
        </p>
      ) : (
        <div className="grid grid-cols-4 gap-2">
          {slots.map((slot) => {
            const startHour = format(new Date(slot.start), 'HH:mm');
            const isSelected = selectedSlot?.start === slot.start;
            return (
              <button
                key={slot.start}
                disabled={!slot.available}
                onClick={() => slot.available && onSelectSlot(slot)}
                className={cn(
                  'h-10 rounded-md text-sm font-medium border transition-colors duration-150',
                  slot.available
                    ? isSelected
                      ? 'bg-slate-900 text-white border-slate-900'
                      : 'bg-white hover:bg-slate-50 border-slate-200 text-slate-900'
                    : 'bg-slate-50 text-slate-300 border-slate-100 cursor-not-allowed'
                )}
              >
                {startHour}
              </button>
            );
          })}
        </div>
      )}
      <div className="flex items-center gap-4 mt-3 text-xs text-slate-500">
        <div className="flex items-center gap-1.5">
          <div className="w-3 h-3 rounded bg-white border border-slate-200" />
          <span>Disponible</span>
        </div>
        <div className="flex items-center gap-1.5">
          <div className="w-3 h-3 rounded bg-slate-900" />
          <span>Seleccionada</span>
        </div>
        <div className="flex items-center gap-1.5">
          <div className="w-3 h-3 rounded bg-slate-50 border border-slate-100" />
          <span>Ocupada</span>
        </div>
      </div>
    </div>
  );
}
