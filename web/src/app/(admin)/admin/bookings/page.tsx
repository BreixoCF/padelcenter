'use client';
import { useState } from 'react';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import { useListCenters, useGetCenterBookings } from '@/lib/api/generated/centers/centers';
import PageHeader from '@/components/shared/PageHeader';
import StatusBadge from '@/components/shared/StatusBadge';
import { Button } from '@/components/ui/button';
import { Calendar } from '@/components/ui/calendar';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import {
  Select, SelectContent, SelectItem,
  SelectTrigger, SelectValue,
} from '@/components/ui/select';
import {
  Table, TableBody, TableCell, TableHead,
  TableHeader, TableRow,
} from '@/components/ui/table';
import { CalendarIcon, ChevronLeft, ChevronRight, CalendarX } from 'lucide-react';
import { GridSkeleton } from '@/components/shared/LoadingState';
import EmptyState from '@/components/shared/EmptyState';


export default function AdminBookingsPage() {
  const [selectedCenter, setSelectedCenter] = useState('');
  const [selectedDate, setSelectedDate] = useState<Date>(new Date());
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [page, setPage] = useState(0);

  const { data: centersData } = useListCenters({ page: 0, size: 50 });
  const centers = centersData?.content ?? [];

  const dateStr = format(selectedDate, 'yyyy-MM-dd');

  const { data: bookingsData, isLoading } = useGetCenterBookings(
    selectedCenter,
    {
      date: dateStr,
      status: statusFilter === 'ALL' ? undefined : statusFilter as 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED',
      page,
      size: 20,
    },
    { query: { enabled: !!selectedCenter } }
  );

  const bookings = bookingsData?.content ?? [];

  const handlePrevDay = () => {
    const d = new Date(selectedDate);
    d.setDate(d.getDate() - 1);
    setSelectedDate(d);
    setPage(0);
  };

  const handleNextDay = () => {
    const d = new Date(selectedDate);
    d.setDate(d.getDate() + 1);
    setSelectedDate(d);
    setPage(0);
  };

  return (
    <main className="container mx-auto px-4 py-8 max-w-6xl">
      <PageHeader
        title="Reservas"
        description="Gestiona las reservas de tus centros"
      />

      <div className="flex flex-wrap gap-3 mb-6">
        <Select value={selectedCenter} onValueChange={v => { setSelectedCenter(v); setPage(0); }}>
          <SelectTrigger className="w-56">
            <SelectValue placeholder="Selecciona un centro" />
          </SelectTrigger>
          <SelectContent>
            {centers.map((c: any) => (
              <SelectItem key={c.centerId} value={c.centerId}>{c.name}</SelectItem>
            ))}
          </SelectContent>
        </Select>

        <div className="flex items-center gap-1">
          <Button variant="outline" size="icon" onClick={handlePrevDay}>
            <ChevronLeft className="h-4 w-4" />
          </Button>
          <Popover>
            <PopoverTrigger asChild>
              <Button variant="outline" className="w-40 justify-start gap-2">
                <CalendarIcon className="h-4 w-4" />
                {format(selectedDate, "d MMM yyyy", { locale: es })}
              </Button>
            </PopoverTrigger>
            <PopoverContent className="w-auto p-0">
              <Calendar
                mode="single"
                selected={selectedDate}
                onSelect={d => { if (d) { setSelectedDate(d); setPage(0); } }}
                locale={es}
              />
            </PopoverContent>
          </Popover>
          <Button variant="outline" size="icon" onClick={handleNextDay}>
            <ChevronRight className="h-4 w-4" />
          </Button>
        </div>

        <Select value={statusFilter} onValueChange={v => { setStatusFilter(v); setPage(0); }}>
          <SelectTrigger className="w-44">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">Todos los estados</SelectItem>
            <SelectItem value="CONFIRMED">Confirmadas</SelectItem>
            <SelectItem value="CANCELLED">Canceladas</SelectItem>
            <SelectItem value="COMPLETED">Completadas</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {!selectedCenter ? (
        <EmptyState
          icon={CalendarX}
          title="Selecciona un centro"
          description="Elige un centro para ver sus reservas"
        />
      ) : isLoading ? (
        <GridSkeleton count={4} />
      ) : bookings.length === 0 ? (
        <EmptyState
          icon={CalendarX}
          title="Sin reservas"
          description={`No hay reservas el ${format(selectedDate, "d 'de' MMMM", { locale: es })}`}
        />
      ) : (
        <>
          <div className="border rounded-lg overflow-hidden">
            <Table>
              <TableHeader>
                <TableRow className="border-b border-zinc-100">
                  <TableHead className="text-[11px] uppercase tracking-wide text-zinc-400">Pista</TableHead>
                  <TableHead className="text-[11px] uppercase tracking-wide text-zinc-400">Usuario</TableHead>
                  <TableHead className="text-[11px] uppercase tracking-wide text-zinc-400">Horario</TableHead>
                  <TableHead className="text-[11px] uppercase tracking-wide text-zinc-400">Precio</TableHead>
                  <TableHead className="text-[11px] uppercase tracking-wide text-zinc-400">Estado</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {bookings.map((b: any) => (
                  <TableRow key={b.bookingId} className="border-b border-zinc-50 hover:bg-zinc-50/50">
                    <TableCell className="font-medium">{b.fieldName ?? b.fieldId}</TableCell>
                    <TableCell className="text-zinc-500 text-sm">{b.userId}</TableCell>
                    <TableCell>
                      {format(new Date(b.startTime), 'HH:mm')}
                      {' — '}
                      {format(new Date(b.endTime), 'HH:mm')}
                    </TableCell>
                    <TableCell>{b.totalPrice}€</TableCell>
                    <TableCell>
                      <StatusBadge status={b.status} />
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>

          {bookingsData && bookingsData.totalPages > 1 && (
            <div className="flex justify-center gap-2 mt-4">
              <Button variant="outline" size="sm"
                disabled={page === 0}
                onClick={() => setPage(p => p - 1)}>
                Anterior
              </Button>
              <span className="text-sm text-slate-500 flex items-center px-2">
                {page + 1} / {bookingsData.totalPages}
              </span>
              <Button variant="outline" size="sm"
                disabled={page >= bookingsData.totalPages - 1}
                onClick={() => setPage(p => p + 1)}>
                Siguiente
              </Button>
            </div>
          )}
        </>
      )}
    </main>
  );
}
