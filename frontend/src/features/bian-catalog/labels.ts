import type { ElementType, RelationshipType } from '../../types/api';

export const elementTypeLabel: Record<ElementType, string> = {
  BUSINESS_AREA: 'Business Area',
  BUSINESS_DOMAIN: 'Business Domain',
  SERVICE_DOMAIN: 'Service Domain',
  SERVICE_OPERATION: 'Operación',
  CONTROL_RECORD: 'Control Record',
  BUSINESS_OBJECT: 'Business Object',
};

/** Sigla y color de cada tipo: identifican el tipo de un vistazo en listas densas. */
export const elementTypeGlyph: Record<ElementType, { code: string; color: string }> = {
  BUSINESS_AREA: { code: 'BA', color: 'var(--color-type-area)' },
  BUSINESS_DOMAIN: { code: 'BD', color: 'var(--color-type-domain)' },
  SERVICE_DOMAIN: { code: 'SD', color: 'var(--color-type-sd)' },
  SERVICE_OPERATION: { code: 'OP', color: 'var(--color-type-op)' },
  CONTROL_RECORD: { code: 'CR', color: 'var(--color-type-cr)' },
  BUSINESS_OBJECT: { code: 'BO', color: 'var(--color-type-bo)' },
};

export const elementTypeOrder: ElementType[] = [
  'BUSINESS_AREA',
  'BUSINESS_DOMAIN',
  'SERVICE_DOMAIN',
  'SERVICE_OPERATION',
  'CONTROL_RECORD',
  'BUSINESS_OBJECT',
];

/** Cómo leer la relación desde el elemento consultado, según la dirección. */
export const relationshipHeading: Record<RelationshipType, { outgoing: string; incoming: string }> = {
  CONTAINS: { outgoing: 'Contiene', incoming: 'Agrupado en' },
  HAS_OPERATION: { outgoing: 'Operaciones', incoming: 'Operación de' },
  HAS_CONTROL_RECORD: { outgoing: 'Control Records', incoming: 'Control Record de' },
  REFERENCES_BUSINESS_OBJECT: { outgoing: 'Business Objects', incoming: 'Referenciado por' },
  COLLABORATES_WITH: { outgoing: 'Colabora con', incoming: 'Recibe colaboración de' },
};
