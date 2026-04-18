import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Euro } from 'lucide-react';

interface FieldCardProps {
  field: {
    fieldId: string;
    name: string;
    type: string;
    pricePerHour: number;
    isAvailable?: boolean;
  };
  onBook: (fieldId: string, pricePerHour: number) => void;
}

export default function FieldCard({ field, onBook }: FieldCardProps) {
  const available = field.isAvailable !== false;
  return (
    <Card>
      <CardHeader className="pb-2">
        <div className="flex items-center justify-between">
          <CardTitle className="text-base font-medium">{field.name}</CardTitle>
          <Badge variant={available ? 'default' : 'secondary'}>
            {available ? 'Disponible' : 'Cerrada'}
          </Badge>
        </div>
      </CardHeader>
      <CardContent>
        <div className="flex items-center justify-between">
          <div className="space-y-1">
            <p className="text-sm text-slate-500">
              {field.type === 'INDOOR' ? 'Interior' : 'Exterior'}
            </p>
            <div className="flex items-center gap-1 text-sm font-medium">
              <Euro className="h-3.5 w-3.5" />
              <span>{field.pricePerHour}/hora</span>
            </div>
          </div>
          <Button
            size="sm"
            disabled={!available}
            onClick={() => onBook(field.fieldId, field.pricePerHour)}
          >
            Reservar
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}
