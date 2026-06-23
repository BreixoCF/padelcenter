'use client';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '@/lib/api/client';
import type { PageResponse, Field } from '@/types';

export function useFields(centerId: string) {
  return useQuery<PageResponse<Field>>({
    queryKey: ['fields', centerId],
    queryFn: () =>
      apiClient.get(`/api/v1/centers/${centerId}/fields`).then((r) => r.data),
    enabled: !!centerId,
  });
}
