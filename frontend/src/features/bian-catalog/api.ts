import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { apiFetch, toQuery } from '../../services/http';
import type { CatalogFacets, CatalogVersion, ElementDetail, ElementSummary, ElementType, Page } from '../../types/api';

export interface CatalogSearch {
  versionId?: string;
  q?: string;
  types: ElementType[];
  category?: string;
  page: number;
  size: number;
}

export function useCatalogVersions() {
  return useQuery({
    queryKey: ['catalog', 'versions'],
    queryFn: () => apiFetch<CatalogVersion[]>('/api/v1/catalog/versions'),
    staleTime: 5 * 60_000,
  });
}

export function useCatalogFacets(versionId: string | undefined) {
  return useQuery({
    queryKey: ['catalog', 'facets', versionId],
    queryFn: () => apiFetch<CatalogFacets>(`/api/v1/catalog/versions/${versionId}/facets`),
    enabled: !!versionId,
    staleTime: 5 * 60_000,
  });
}

export function useCatalogSearch(search: CatalogSearch, enabled: boolean) {
  return useQuery({
    queryKey: ['catalog', 'elements', search],
    queryFn: ({ signal }) =>
      apiFetch<Page<ElementSummary>>(
        `/api/v1/catalog/elements${toQuery({
          versionId: search.versionId,
          q: search.q,
          type: search.types,
          category: search.category,
          page: search.page,
          size: search.size,
        })}`,
        { signal },
      ),
    enabled,
    placeholderData: keepPreviousData,
  });
}

export function useCatalogElement(id: string | undefined) {
  return useQuery({
    queryKey: ['catalog', 'element', id],
    queryFn: () => apiFetch<ElementDetail>(`/api/v1/catalog/elements/${id}`),
    enabled: !!id,
  });
}
