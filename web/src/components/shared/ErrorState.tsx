import { AlertCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';

export default function ErrorState({ onRetry }: { onRetry?: () => void }) {
  return (
    <div className="text-center py-12">
      <AlertCircle className="mx-auto h-10 w-10 text-red-400 mb-3" />
      <p className="text-sm text-slate-600">Error al cargar los datos</p>
      {onRetry && (
        <Button variant="outline" size="sm" onClick={onRetry} className="mt-3">
          Reintentar
        </Button>
      )}
    </div>
  );
}
