'use client';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/lib/api/client';

export const useCenters = (page = 0, size = 20) =>
  useQuery({
    queryKey: ['centers', page, size],
    queryFn: () =>
      apiClient.get('/api/v1/centers', { params: { page, size } }).then((r) => r.data),
  });

export const useCenter = (centerId: string) =>
  useQuery({
    queryKey: ['centers', centerId],
    queryFn: () => apiClient.get(`/api/v1/centers/${centerId}`).then((r) => r.data),
    enabled: !!centerId,
  });

export const useCenterFields = (
  centerId: string,
  params?: { type?: string; available?: boolean }
) =>
  useQuery({
    queryKey: ['centers', centerId, 'fields', params],
    queryFn: () =>
      apiClient
        .get(`/api/v1/centers/${centerId}/fields`, { params })
        .then((r) => r.data),
    enabled: !!centerId,
  });

export const useFieldAvailability = (fieldId: string, date: string) =>
  useQuery({
    queryKey: ['fields', fieldId, 'availability', date],
    queryFn: () =>
      apiClient
        .get(`/api/v1/fields/${fieldId}/availability`, { params: { date } })
        .then((r) => r.data),
    enabled: !!fieldId && !!date,
  });

export const useCreateBooking = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: {
      fieldId: string;
      startTime: string;
      endTime: string;
      totalPrice: number;
    }) => apiClient.post('/api/v1/bookings', data).then((r) => r.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['bookings'] });
      queryClient.invalidateQueries({ queryKey: ['fields'] });
    },
  });
};

export const useUserBookings = (userId: string, page = 0, size = 20) =>
  useQuery({
    queryKey: ['bookings', 'user', userId, page],
    queryFn: () =>
      apiClient
        .get(`/api/v1/users/${userId}/bookings`, { params: { page, size } })
        .then((r) => r.data),
    enabled: !!userId,
  });

export const useCurrentUser = () =>
  useQuery({
    queryKey: ['me'],
    queryFn: () => apiClient.get('/api/v1/me').then((r) => r.data),
  });
