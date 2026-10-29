import { screen, within } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import type { ElementDetail } from '../../types/api';
import { demoUser, demoVersion, element, json, mockApi, renderRoute } from '../../tests/test-utils';

const detail: ElementDetail = {
  ...element(),
  functionalResponsibility: 'Validar la orden y decidir su ejecución.',
  sourceReference: 'Contenido educativo del catálogo DEMO-EDU-1',
  version: demoVersion,
  relationships: [
    {
      relationshipId: 'r-1',
      relationshipType: 'CONTAINS',
      direction: 'INCOMING',
      provenance: 'EDUCATIONAL_EXAMPLE',
      element: element({ id: 'bd-1', type: 'BUSINESS_DOMAIN', name: 'Pagos (agrupación demo)' }),
    },
    {
      relationshipId: 'r-2',
      relationshipType: 'COLLABORATES_WITH',
      direction: 'OUTGOING',
      provenance: 'EDUCATIONAL_EXAMPLE',
      description: 'Ejemplo: entregar la orden aprobada para su ejecución.',
      element: element({ id: 'sd-2', name: 'Payment Execution' }),
    },
  ],
};

describe('ElementDetailPage', () => {
  it('muestra responsabilidad, procedencia no verificada y relaciones agrupadas', async () => {
    mockApi({ 'GET /api/v1/auth/me': () => json(demoUser), 'GET /api/v1/catalog/elements/e-1': () => json(detail) });
    renderRoute('/catalog/elements/e-1');

    expect(await screen.findByRole('heading', { level: 1, name: 'Payment Order' })).toBeInTheDocument();
    expect(screen.getByText('Validar la orden y decidir su ejecución.')).toBeInTheDocument();
    expect(screen.getByText('Sin verificar')).toBeInTheDocument();

    const grouping = screen.getByRole('heading', { name: /Agrupado en/ }).parentElement!;
    expect(within(grouping).getByRole('link', { name: 'Pagos (agrupación demo)' })).toHaveAttribute('href', '/catalog/elements/bd-1');

    const collaborations = screen.getByRole('heading', { name: /Colabora con/ }).parentElement!;
    expect(within(collaborations).getByText('Ejemplo educativo')).toBeInTheDocument();
    expect(within(collaborations).getByRole('link', { name: 'Payment Execution' })).toBeInTheDocument();
  });

  it('indica cuando el elemento no existe', async () => {
    mockApi({
      'GET /api/v1/auth/me': () => json(demoUser),
      'GET /api/v1/catalog/elements/nope': () => json({ status: 404, title: 'Recurso no encontrado' }, 404),
    });
    renderRoute('/catalog/elements/nope');
    expect(await screen.findByText('Este elemento no existe en el catálogo')).toBeInTheDocument();
  });
});
