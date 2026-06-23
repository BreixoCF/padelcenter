const STATUS_CONFIG: Record<string, { dot: string; text: string; label: string }> = {
  CONFIRMED: { dot: 'bg-emerald-500', text: 'text-emerald-700', label: 'Confirmada' },
  CANCELLED: { dot: 'bg-red-500', text: 'text-red-700', label: 'Cancelada' },
  COMPLETED: { dot: 'bg-zinc-400', text: 'text-zinc-500', label: 'Completada' },
  PENDING: { dot: 'bg-amber-400', text: 'text-amber-700', label: 'Pendiente' },
  DRAFT: { dot: 'bg-zinc-300', text: 'text-zinc-500', label: 'Borrador' },
  REGISTRATION_OPEN: { dot: 'bg-emerald-500', text: 'text-emerald-700', label: 'Inscripción abierta' },
  REGISTRATION_CLOSED: { dot: 'bg-orange-400', text: 'text-orange-700', label: 'Inscripción cerrada' },
  IN_PROGRESS: { dot: 'bg-blue-500', text: 'text-blue-700', label: 'En curso' },
};

export default function StatusBadge({ status }: { status: string }) {
  const config = STATUS_CONFIG[status];
  if (!config) {
    return <span className="text-xs text-zinc-400">{status}</span>;
  }
  return (
    <span className="inline-flex items-center gap-1.5">
      <span className={`h-2 w-2 rounded-full flex-shrink-0 ${config.dot}`} />
      <span className={`text-xs font-medium ${config.text}`}>{config.label}</span>
    </span>
  );
}
