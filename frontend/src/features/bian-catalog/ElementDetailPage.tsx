import { Link, useParams } from 'react-router';
import { ApiError } from '../../services/http';
import { EmptyState, ErrorState, LoadingState } from '../../components/States';
import type { RelatedElement, RelationshipType } from '../../types/api';
import { useCatalogElement } from './api';
import { elementTypeLabel, relationshipHeading } from './labels';
import { ProvenanceNotice } from './ProvenanceNotice';
import { TypeGlyph } from './TypeGlyph';

const relationshipOrder: RelationshipType[] = [
  'CONTAINS',
  'HAS_OPERATION',
  'HAS_CONTROL_RECORD',
  'REFERENCES_BUSINESS_OBJECT',
  'COLLABORATES_WITH',
];

const dateFormat = new Intl.DateTimeFormat('es', { dateStyle: 'medium' });

interface RelationGroup {
  key: string;
  heading: string;
  items: RelatedElement[];
}

function groupRelationships(relationships: RelatedElement[]): RelationGroup[] {
  const groups: RelationGroup[] = [];
  // Primero las relaciones entrantes de agrupación (contexto), luego las salientes.
  for (const direction of ['INCOMING', 'OUTGOING'] as const) {
    for (const type of relationshipOrder) {
      const items = relationships.filter((r) => r.relationshipType === type && r.direction === direction);
      if (items.length > 0) {
        const heading = relationshipHeading[type][direction === 'OUTGOING' ? 'outgoing' : 'incoming'];
        groups.push({ key: `${direction}-${type}`, heading, items });
      }
    }
  }
  return groups;
}

export function ElementDetailPage() {
  const { id } = useParams<{ id: string }>();
  const { data: element, isPending, isError, error, refetch } = useCatalogElement(id);

  if (isPending) return <LoadingState label="Cargando la ficha" />;
  if (isError) {
    if (error instanceof ApiError && error.status === 404) {
      return (
        <EmptyState title="Este elemento no existe en el catálogo">
          <Link to="/catalog" className="font-medium text-ledger underline underline-offset-2">
            Volver al catálogo
          </Link>
        </EmptyState>
      );
    }
    return <ErrorState title="No se pudo cargar la ficha" detail={error.message} onRetry={() => void refetch()} />;
  }

  const groups = groupRelationships(element.relationships);
  const unverified = element.validationStatus !== 'VERIFIED';

  return (
    <article className="max-w-5xl">
      <nav aria-label="Ruta" className="text-sm">
        <Link to="/catalog" className="text-ledger underline-offset-2 hover:underline">
          Catálogo
        </Link>
        <span className="px-2 text-ink-faint" aria-hidden>
          /
        </span>
        <span className="text-ink-soft">{elementTypeLabel[element.type]}</span>
      </nav>

      <header className="mt-4 flex items-start gap-4">
        <TypeGlyph type={element.type} size="lg" />
        <div className="min-w-0">
          <h1 className="text-3xl font-semibold tracking-tight text-balance">{element.name}</h1>
          <p className="mt-1 flex flex-wrap gap-x-4 gap-y-1 text-sm text-ink-soft">
            <span>{elementTypeLabel[element.type]}</span>
            <span className="font-mono text-xs leading-5">{element.externalRef}</span>
            {element.category && <span>{element.category}</span>}
          </p>
        </div>
      </header>

      <div className="mt-6 grid gap-8 lg:grid-cols-[minmax(0,1fr)_18rem]">
        <div className="space-y-8">
          {element.functionalResponsibility && (
            <section aria-labelledby="responsibility">
              <h2 id="responsibility" className="text-sm font-semibold text-ink-soft">
                Responsabilidad funcional
              </h2>
              <p className="mt-2 border-l-4 border-ledger pl-4 text-lg leading-relaxed text-pretty">
                {element.functionalResponsibility}
              </p>
            </section>
          )}
          {element.description && (
            <section aria-labelledby="description">
              <h2 id="description" className="text-sm font-semibold text-ink-soft">
                Descripción
              </h2>
              <p className="mt-2 max-w-prose leading-relaxed">{element.description}</p>
            </section>
          )}

          <section aria-labelledby="relationships">
            <h2 id="relationships" className="text-lg font-semibold">
              Relaciones
            </h2>
            {groups.length === 0 ? (
              <p className="mt-2 text-sm text-ink-soft">Este elemento no tiene relaciones registradas en esta versión.</p>
            ) : (
              <div className="mt-4 space-y-6">
                {groups.map((group) => (
                  <div key={group.key}>
                    <h3 className="text-sm font-semibold text-ink-soft">
                      {group.heading} <span className="font-normal text-ink-faint">({group.items.length})</span>
                    </h3>
                    <ul className="mt-2 divide-y divide-line rounded-lg border border-line bg-surface">
                      {group.items.map((rel) => (
                        <li key={rel.relationshipId} className="flex gap-3 px-4 py-2.5">
                          <TypeGlyph type={rel.element.type} />
                          <div className="min-w-0 flex-1">
                            <Link
                              to={`/catalog/elements/${rel.element.id}`}
                              className="font-medium text-ink underline-offset-2 hover:text-ledger hover:underline"
                            >
                              {rel.element.name}
                            </Link>
                            {rel.description && <p className="text-sm text-ink-soft">{rel.description}</p>}
                          </div>
                          {rel.provenance === 'EDUCATIONAL_EXAMPLE' ? (
                            <span className="hatch-caution self-start rounded px-1.5 py-0.5 text-xs text-caution">
                              Ejemplo educativo
                            </span>
                          ) : (
                            <span className="self-start rounded bg-ledger-wash px-1.5 py-0.5 text-xs text-ledger">
                              Documentada en la fuente
                            </span>
                          )}
                        </li>
                      ))}
                    </ul>
                  </div>
                ))}
              </div>
            )}
          </section>
        </div>

        <aside aria-label="Procedencia" className="space-y-4 text-sm">
          <div className={`rounded-lg border p-4 ${unverified ? 'hatch-caution border-caution/40' : 'border-ledger/30 bg-ledger-wash'}`}>
            <p className="font-semibold">{unverified ? 'Sin verificar' : 'Verificado'}</p>
            <p className="mt-1 text-ink-soft">
              {unverified
                ? 'Este contenido no ha sido contrastado con una fuente oficial de BIAN.'
                : 'Contrastado con la fuente indicada.'}
            </p>
          </div>
          <dl className="space-y-3 rounded-lg border border-line bg-surface p-4">
            <div>
              <dt className="text-ink-soft">Versión del catálogo</dt>
              <dd className="font-medium">
                {element.version.name} <span className="font-mono text-xs">({element.version.code})</span>
              </dd>
            </div>
            <div>
              <dt className="text-ink-soft">Fecha de carga</dt>
              <dd>{dateFormat.format(new Date(element.version.importedAt))}</dd>
            </div>
            {element.sourceReference && (
              <div>
                <dt className="text-ink-soft">Referencia de la fuente</dt>
                <dd>{element.sourceReference}</dd>
              </div>
            )}
          </dl>
          {!element.version.official && <ProvenanceNotice version={element.version} />}
        </aside>
      </div>
    </article>
  );
}
