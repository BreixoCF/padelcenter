'use client';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '@/lib/api/client';
import type { PageResponse, Tournament } from '@/types';

export function useTournaments(page = 0, size = 20) {
  return useQuery<PageResponse<Tournament>>({
    queryKey: ['tournaments', page, size],
    queryFn: () =>
      apiClient.get(`/api/v1/tournaments?page=${page}&size=${size}`).then((r) => r.data),
  });
}
