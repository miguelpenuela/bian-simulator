import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';
import type { Page, ElementSummary } from '../../types/api';
import { demoUser, demoVersion, element, json, mockApi, renderRoute } from '../../tests/test-utils';

function page(content: ElementSummary[], extra: Partial<Page<ElementSummary>> = {}): Page<ElementSummary> {
  return { content, page: 0, size: 25, totalElements: content.length, totalPages: 1, ...extra };
}

const base = {
  'GET /api/v1/auth/me': () => json(demoUser),
  'GET /api/v1/catalog/versions': () => json([demoVersion]),
  'GET /api/v1/catalog/versions/v-1/facets': () =>
    json({
      versionId: 'v-1',
      types: [{ value: 'SERVICE_DOMAIN', count: 18 }],
      categories: [{ value: 'Pagos', count: 12 }],
    }),
};

describe('CatalogExplorerPage', () => {
  it('muestra el aviso de modo demostración y los resultados', async () => {
    mockApi({ ...base, 'GET /api/v1/catalog/elements': () => json(page([element()])) });
    renderRoute('/catalog');
    expect(await screen.findByText('Modo demostración: contenido educativo, no oficial')).toBeInTheDocument();
    const results = await screen.findByRole('link', { name: /Payment Order/ });
    expect(results).toHaveAttribute('href', '/catalog/elements/e-1');
    expect(within(results).getByLabelText('Service Domain')).toHaveTextContent('SD');
  });

  it('aplica filtros de tipo y texto como parámetros de la API', async () => {
    const urls: URL[] = [];
    mockApi({
      ...base,
      'GET /api/v1/catalog/elements': (url) => {
        urls.push(url);
        return json(page([element()]));
      },
    });
    const { router } = renderRoute('/catalog');
    await userEvent.click(await screen.findByRole('checkbox', { name: /Service Domain/ }));
    await userEvent.type(screen.getByLabelText('Buscar en el catálogo'), 'pago');

    await waitFor(() => {
      const last = urls.at(-1)!;
      expect(last.searchParams.getAll('type')).toEqual(['SERVICE_DOMAIN']);
      expect(last.searchParams.get('q')).toBe('pago');
      expect(last.searchParams.get('versionId')).toBe('v-1');
    });
    expect(router.state.location.search).toContain('type=SERVICE_DOMAIN');
  });

  it('muestra un estado vacío con indicaciones cuando no hay coincidencias', async () => {
    mockApi({ ...base, 'GET /api/v1/catalog/elements': () => json(page([])) });
    renderRoute('/catalog?q=zzz');
    expect(await screen.findByText('Ningún elemento coincide con la búsqueda')).toBeInTheDocument();
  });

  it('muestra un error recuperable si la búsqueda falla', async () => {
    mockApi({ ...base, 'GET /api/v1/catalog/elements': () => json({ status: 500, detail: 'Fallo' }, 500) });
    renderRoute('/catalog');
    expect(await screen.findByText('La búsqueda falló')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Reintentar' })).toBeInTheDocument();
  });

  it('pagina los resultados', async () => {
    const urls: URL[] = [];
    mockApi({
      ...base,
      'GET /api/v1/catalog/elements': (url) => {
        urls.push(url);
        const current = Number(url.searchParams.get('page'));
        return json(page([element({ id: `e-${current}`, name: `Elemento ${current}` })], { page: current, totalPages: 3, totalElements: 60 }));
      },
    });
    renderRoute('/catalog');
    await userEvent.click(await screen.findByRole('button', { name: 'Siguiente' }));
    expect(await screen.findByText('Página 2 de 3')).toBeInTheDocument();
    expect(urls.at(-1)!.searchParams.get('page')).toBe('1');
  });
});
