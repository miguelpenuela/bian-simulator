import type { ReactNode } from 'react';

export function LoadingState({ label = 'Cargando' }: { label?: string }) {
  return (
    <div role="status" aria-live="polite" className="flex items-center gap-3 px-1 py-10 text-sm text-ink-soft">
      <span className="size-4 animate-spin rounded-full border-2 border-line border-t-ledger" aria-hidden />
      {label}…
    </div>
  );
}

export function ErrorState({ title, detail, onRetry }: { title: string; detail?: string; onRetry?: () => void }) {
  return (
    <div role="alert" className="rounded-lg border border-danger/30 bg-danger-wash p-5">
      <p className="font-semibold text-danger">{title}</p>
      {detail && <p className="mt-1 text-sm text-ink-soft">{detail}</p>}
      {onRetry && (
        <button
          type="button"
          onClick={onRetry}
          className="mt-3 rounded-md border border-line-strong bg-surface px-3 py-1.5 text-sm font-medium hover:border-ink"
        >
          Reintentar
        </button>
      )}
    </div>
  );
}

export function EmptyState({ title, children }: { title: string; children?: ReactNode }) {
  return (
    <div className="rounded-lg border border-dashed border-line-strong px-6 py-10 text-center">
      <p className="font-semibold">{title}</p>
      {children && <div className="mt-2 text-sm text-ink-soft">{children}</div>}
    </div>
  );
}
