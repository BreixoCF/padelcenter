'use client';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/lib/api/client';
import type { PageResponse, Booking } from '@/types';

export function useMyBookings() {
  return useQuery<PageResponse<Booking>>({
    queryKey: ['bookings', 'mine'],
    queryFn: () => apiClient.get('/api/v1/bookings/my').then((r) => r.data),
  });
}

export function useCancelBooking() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (bookingId: string) =>
      apiClient.patch(`/api/v1/bookings/${bookingId}/cancel`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['bookings'] });
    },
  });
}
