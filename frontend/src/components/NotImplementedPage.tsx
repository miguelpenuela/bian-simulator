import { Link } from 'react-router';

/** Página explícita para módulos aún no implementados (no se simula funcionalidad). */
export function NotImplementedPage({ title, summary }: { title: string; summary: string }) {
  return (
    <div className="max-w-2xl">
      <h1 className="text-2xl font-semibold tracking-tight">{title}</h1>
      <p className="mt-3 text-ink-soft">{summary}</p>
      <div className="mt-6 rounded-lg border border-dashed border-line-strong bg-surface p-5 text-sm">
        <p className="font-semibold">Este módulo aún no está disponible.</p>
        <p className="mt-1 text-ink-soft">
          Está planificado en el backlog del proyecto. Mientras tanto puede explorar el{' '}
          <Link to="/catalog" className="font-medium text-ledger underline underline-offset-2">
            catálogo
          </Link>
          .
        </p>
      </div>
    </div>
  );
}
