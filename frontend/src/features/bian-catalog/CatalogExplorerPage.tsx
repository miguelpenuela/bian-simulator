import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router';
import { EmptyState, ErrorState, LoadingState } from '../../components/States';
import type { ElementType } from '../../types/api';
import { useDebouncedValue } from '../../utils/useDebouncedValue';
import { useCatalogFacets, useCatalogSearch, useCatalogVersions } from './api';
import { elementTypeLabel, elementTypeOrder } from './labels';
import { ProvenanceNotice } from './ProvenanceNotice';
import { TypeGlyph } from './TypeGlyph';

const PAGE_SIZE = 25;

function isElementType(value: string): value is ElementType {
  return (elementTypeOrder as string[]).includes(value);
}

export function CatalogExplorerPage() {
  const [params, setParams] = useSearchParams();
  const types = params.getAll('type').filter(isElementType);
  const category = params.get('category') ?? '';
  const page = Math.max(0, Number(params.get('page') ?? 0) || 0);

  // El texto se edita localmente y se sincroniza con la URL con un pequeño retardo.
  const [text, setText] = useState(params.get('q') ?? '');
  const debouncedText = useDebouncedValue(text.trim(), 300);

  const update = (changes: Record<string, string | string[] | null>, resetPage = true) => {
    setParams(
      (current) => {
        const next = new URLSearchParams(current);
        for (const [key, value] of Object.entries(changes)) {
          next.delete(key);
          if (Array.isArray(value)) value.forEach((v) => next.append(key, v));
          else if (value) next.set(key, value);
        }
        if (resetPage) next.delete('page');
        return next;
      },
      { replace: true },
    );
  };

  useEffect(() => {
    if ((params.get('q') ?? '') !== debouncedText) update({ q: debouncedText || null });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [debouncedText]);

  const versions = useCatalogVersions();
  const version = versions.data?.[0];
  const facets = useCatalogFacets(version?.id);
  const results = useCatalogSearch(
    { versionId: version?.id, q: params.get('q') ?? undefined, types, category: category || undefined, page, size: PAGE_SIZE },
    !!version,
  );

  if (versions.isPending) return <LoadingState label="Cargando el catálogo" />;
  if (versions.isError) {
    return <ErrorState title="No se pudo cargar el catálogo" detail={versions.error.message} onRetry={() => void versions.refetch()} />;
  }
  if (!version) {
    return (
      <EmptyState title="No hay ningún catálogo cargado">
        Un administrador debe importar una versión del catálogo para poder explorarlo.
      </EmptyState>
    );
  }

  const typeCount = (type: ElementType) => facets.data?.types.find((f) => f.value === type)?.count;
  const toggleType = (type: ElementType) =>
    update({ type: types.includes(type) ? types.filter((t) => t !== type) : [...types, type] });
  const hasFilters = types.length > 0 || !!category || !!params.get('q');

  return (
    <div className="max-w-6xl">
      <div className="flex flex-wrap items-baseline justify-between gap-2">
        <h1 className="text-2xl font-semibold tracking-tight">Catálogo BIAN</h1>
        <p className="text-sm text-ink-soft">
          {version.name} <span className="font-mono text-xs">({version.code})</span>
        </p>
      </div>
      <div className="mt-4">
        <ProvenanceNotice version={version} />
      </div>

      <div className="mt-6 grid gap-8 md:grid-cols-[13rem_minmax(0,1fr)]">
        <aside aria-label="Filtros" className="space-y-6">
          <fieldset>
            <legend className="text-sm font-semibold">Tipo de elemento</legend>
            <ul className="mt-2 space-y-1">
              {elementTypeOrder.map((type) => (
                <li key={type}>
                  <label className="flex cursor-pointer items-center gap-2 rounded px-1 py-1 text-sm hover:bg-surface">
                    <input
                      type="checkbox"
                      checked={types.includes(type)}
                      onChange={() => toggleType(type)}
                      className="accent-ledger"
                    />
                    <span className="flex-1">{elementTypeLabel[type]}</span>
                    <span className="text-xs text-ink-faint tabular-nums">{typeCount(type) ?? ''}</span>
                  </label>
                </li>
              ))}
            </ul>
          </fieldset>

          <div>
            <label htmlFor="category" className="text-sm font-semibold">
              Categoría
            </label>
            <select
              id="category"
              value={category}
              onChange={(e) => update({ category: e.target.value || null })}
              className="input mt-2"
            >
              <option value="">Todas</option>
              {facets.data?.categories.map((c) => (
                <option key={c.value} value={c.value}>
                  {c.value} ({c.count})
                </option>
              ))}
            </select>
          </div>

          {hasFilters && (
            <button
              type="button"
              onClick={() => {
                setText('');
                update({ type: null, category: null, q: null });
              }}
              className="text-sm font-medium text-ledger underline underline-offset-2"
            >
              Quitar filtros
            </button>
          )}
        </aside>

        <section aria-labelledby="results-title">
          <label htmlFor="catalog-search" className="sr-only">
            Buscar en el catálogo
          </label>
          <input
            id="catalog-search"
            type="search"
            value={text}
            onChange={(e) => setText(e.target.value)}
            placeholder="Buscar por nombre o descripción, p. ej. pago, cuenta, fraude"
            className="input py-2.5 text-base"
            maxLength={100}
          />

          <h2 id="results-title" className="sr-only">
            Resultados
          </h2>
          <div className="mt-4" aria-busy={results.isFetching}>
            {results.isPending ? (
              <LoadingState label="Buscando" />
            ) : results.isError ? (
              <ErrorState title="La búsqueda falló" detail={results.error.message} onRetry={() => void results.refetch()} />
            ) : results.data.totalElements === 0 ? (
              <EmptyState title="Ningún elemento coincide con la búsqueda">
                Pruebe con otro término o quite algún filtro.
              </EmptyState>
            ) : (
              <>
                <p className="mb-2 text-sm text-ink-soft" aria-live="polite">
                  {results.data.totalElements} {results.data.totalElements === 1 ? 'elemento' : 'elementos'}
                </p>
                <ul className="divide-y divide-line overflow-hidden rounded-lg border border-line bg-surface">
                  {results.data.content.map((element) => (
                    <li key={element.id}>
                      <Link
                        to={`/catalog/elements/${element.id}`}
                        className="flex gap-3 px-4 py-3 hover:bg-ledger-wash focus-visible:bg-ledger-wash"
                      >
                        <TypeGlyph type={element.type} />
                        <span className="min-w-0 flex-1">
                          <span className="flex flex-wrap items-baseline gap-x-3">
                            <span className="font-semibold">{element.name}</span>
                            <span className="font-mono text-xs text-ink-faint">{element.externalRef}</span>
                          </span>
                          {element.description && (
                            <span className="mt-0.5 line-clamp-2 block text-sm text-ink-soft">{element.description}</span>
                          )}
                        </span>
                        {element.category && (
                          <span className="hidden shrink-0 self-start text-xs text-ink-soft sm:block">{element.category}</span>
                        )}
                      </Link>
                    </li>
                  ))}
                </ul>
                <Pagination
                  page={results.data.page}
                  totalPages={results.data.totalPages}
                  onChange={(next) => update({ page: next > 0 ? String(next) : null }, false)}
                />
              </>
            )}
          </div>
        </section>
      </div>
    </div>
  );
}

function Pagination({ page, totalPages, onChange }: { page: number; totalPages: number; onChange: (page: number) => void }) {
  if (totalPages <= 1) return null;
  return (
    <nav aria-label="Paginación" className="mt-4 flex items-center justify-between text-sm">
      <button
        type="button"
        disabled={page === 0}
        onClick={() => onChange(page - 1)}
        className="rounded-md border border-line-strong bg-surface px-3 py-1.5 disabled:opacity-40"
      >
        Anterior
      </button>
      <span className="text-ink-soft">
        Página {page + 1} de {totalPages}
      </span>
      <button
        type="button"
        disabled={page + 1 >= totalPages}
        onClick={() => onChange(page + 1)}
        className="rounded-md border border-line-strong bg-surface px-3 py-1.5 disabled:opacity-40"
      >
        Siguiente
      </button>
    </nav>
  );
}
