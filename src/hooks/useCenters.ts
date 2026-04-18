'use client';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '@/lib/api/client';
import type { PageResponse, Center } from '@/types';

export function useCenters(page = 0, size = 20) {
  return useQuery<PageResponse<Center>>({
    queryKey: ['centers', page, size],
    queryFn: () =>
      apiClient.get(`/api/v1/centers?page=${page}&size=${size}`).then((r) => r.data),
  });
}

export function useCenter(centerId: string) {
  return useQuery<Center>({
    queryKey: ['centers', centerId],
    queryFn: () => apiClient.get(`/api/v1/centers/${centerId}`).then((r) => r.data),
    enabled: !!centerId,
  });
}
