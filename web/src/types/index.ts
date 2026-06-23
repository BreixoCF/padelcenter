export interface Center {
  centerId: string;
  name: string;
  address: string;
  city: string;
  phone?: string;
  imageUrl?: string;
}

export interface Field {
  fieldId: string;
  centerId: string;
  name: string;
  type: 'INDOOR' | 'OUTDOOR';
  pricePerHour: number;
}

export interface Booking {
  bookingId: string;
  fieldId: string;
  userId: string;
  startTime: string;
  endTime: string;
  status: 'PENDING' | 'CONFIRMED' | 'CANCELLED';
  totalPrice: number;
}

export interface Tournament {
  tournamentId: string;
  centerId: string;
  name: string;
  startDate: string;
  endDate: string;
  maxPlayers: number;
  status: 'OPEN' | 'IN_PROGRESS' | 'FINISHED';
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
