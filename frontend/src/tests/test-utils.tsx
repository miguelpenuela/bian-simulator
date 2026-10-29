import { QueryClientProvider } from '@tanstack/react-query';
import { render } from '@testing-library/react';
import { createMemoryRouter, RouterProvider } from 'react-router';
import { vi } from 'vitest';
import { createQueryClient } from '../app/App';
import { routes } from '../app/router';
import type { CatalogVersion, CurrentUser, ElementSummary } from '../types/api';

type Handler = (url: URL, init?: RequestInit) => Response | Promise<Response>;

export function json(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': status >= 400 ? 'application/problem+json' : 'application/json' },
  });
}

/**
 * Sustituye fetch por un enrutador de pruebas: `"GET /api/v1/x"` → handler.
 * Las rutas no declaradas fallan con 599 para que el test lo detecte.
 */
export function mockApi(handlers: Record<string, Handler>) {
  const fetchMock = vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
    const url = new URL(String(input), 'http://localhost');
    const key = `${init?.method ?? 'GET'} ${url.pathname}`;
    const handler = handlers[key];
    if (!handler) return json({ title: `Ruta no simulada: ${key}` }, 599);
    return handler(url, init);
  });
  vi.stubGlobal('fetch', fetchMock);
  return fetchMock;
}

export function renderRoute(path: string) {
  const router = createMemoryRouter(routes, { initialEntries: [path] });
  const queryClient = createQueryClient();
  queryClient.setDefaultOptions({ queries: { retry: false, refetchOnWindowFocus: false } });
  return { router, ...render(<QueryClientProvider client={queryClient}><RouterProvider router={router} /></QueryClientProvider>) };
}

export const demoUser: CurrentUser = {
  id: 'u-1',
  username: 'demo',
  displayName: 'Usuario de demostración',
  roles: ['ARCHITECT', 'LEARNER'],
  permissions: ['CATALOG_READ'],
};

export const demoVersion: CatalogVersion = {
  id: 'v-1',
  code: 'DEMO-EDU-1',
  name: 'Catálogo educativo de demostración',
  sourceType: 'EDUCATIONAL_DEMO',
  official: false,
  provenanceNote: 'Conjunto educativo creado para la plataforma.',
  status: 'ACTIVE',
  importedAt: '2026-10-29T10:00:00Z',
};

export function element(overrides: Partial<ElementSummary> = {}): ElementSummary {
  return {
    id: 'e-1',
    externalRef: 'SD-PAYMENT-ORDER',
    type: 'SERVICE_DOMAIN',
    name: 'Payment Order',
    category: 'Pagos',
    description: 'Recibe y procesa una solicitud de pago.',
    validationStatus: 'UNVERIFIED',
    ...overrides,
  };
}
