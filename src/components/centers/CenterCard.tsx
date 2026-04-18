import Link from 'next/link';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { MapPin, Phone, Mail } from 'lucide-react';

interface CenterCardProps {
  center: {
    centerId: string;
    name: string;
    address: string;
    city: string;
    phoneNumber?: string;
    email?: string;
  };
}

export default function CenterCard({ center }: CenterCardProps) {
  return (
    <Link href={`/centers/${center.centerId}`}>
      <Card className="hover:shadow-md transition-shadow cursor-pointer h-full">
        <CardHeader className="pb-2">
          <div className="flex items-start justify-between">
            <CardTitle className="text-base font-medium">{center.name}</CardTitle>
            <Badge variant="secondary">{center.city}</Badge>
          </div>
        </CardHeader>
        <CardContent className="space-y-2 text-sm text-slate-600">
          <div className="flex items-center gap-2">
            <MapPin className="h-4 w-4 flex-shrink-0" />
            <span>{center.address}</span>
          </div>
          {center.phoneNumber && (
            <div className="flex items-center gap-2">
              <Phone className="h-4 w-4 flex-shrink-0" />
              <span>{center.phoneNumber}</span>
            </div>
          )}
          {center.email && (
            <div className="flex items-center gap-2">
              <Mail className="h-4 w-4 flex-shrink-0" />
              <span>{center.email}</span>
            </div>
          )}
        </CardContent>
      </Card>
    </Link>
  );
}
