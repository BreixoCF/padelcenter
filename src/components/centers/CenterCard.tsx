import Link from 'next/link';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
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
      <Card className="hover:border-zinc-300 border border-zinc-100 transition-colors cursor-pointer h-full">
        <CardHeader className="pb-2">
          <div className="flex items-start justify-between gap-2">
            <CardTitle className="text-base font-medium">{center.name}</CardTitle>
            <span className="bg-zinc-100 text-zinc-600 rounded-full px-2 py-0.5 text-xs font-medium whitespace-nowrap">
              {center.city}
            </span>
          </div>
        </CardHeader>
        <CardContent className="space-y-1.5 text-sm text-zinc-600">
          <div className="flex items-center gap-2">
            <MapPin className="h-3.5 w-3.5 flex-shrink-0 text-zinc-400" />
            <span>{center.address}</span>
          </div>
          {center.phoneNumber && (
            <div className="flex items-center gap-2">
              <Phone className="h-3.5 w-3.5 flex-shrink-0 text-zinc-400" />
              <span>{center.phoneNumber}</span>
            </div>
          )}
          {center.email && (
            <div className="flex items-center gap-2">
              <Mail className="h-3.5 w-3.5 flex-shrink-0 text-zinc-400" />
              <span>{center.email}</span>
            </div>
          )}
        </CardContent>
      </Card>
    </Link>
  );
}
