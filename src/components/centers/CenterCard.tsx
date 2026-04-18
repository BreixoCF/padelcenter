import Link from 'next/link';
import { MapPin } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import type { Center } from '@/types';

interface CenterCardProps {
  center: Center;
}

export default function CenterCard({ center }: CenterCardProps) {
  return (
    <Link href={`/centers/${center.centerId}`}>
      <Card className="hover:shadow-md transition-shadow cursor-pointer h-full">
        <CardHeader>
          <CardTitle className="text-lg">{center.name}</CardTitle>
        </CardHeader>
        <CardContent className="space-y-2">
          <div className="flex items-center gap-1.5 text-sm text-muted-foreground">
            <MapPin className="h-4 w-4 shrink-0" />
            <span>{center.address}</span>
          </div>
          <Badge variant="secondary">{center.city}</Badge>
        </CardContent>
      </Card>
    </Link>
  );
}
