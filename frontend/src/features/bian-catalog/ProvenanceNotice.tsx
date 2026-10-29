import type { CatalogVersion } from '../../types/api';

/**
 * Aviso permanente de procedencia. Un catálogo que no proviene de una importación
 * oficial se marca con el sombreado de "no verificado" en toda la interfaz.
 */
export function ProvenanceNotice({ version }: { version: CatalogVersion }) {
  if (version.official) {
    return (
      <div className="rounded-lg border border-ledger/30 bg-ledger-wash px-4 py-3 text-sm">
        <span className="font-semibold">Catálogo importado de fuente oficial</span>
        {version.standardReleaseLabel && <> — versión {version.standardReleaseLabel}</>}.
      </div>
    );
  }
  return (
    <div role="note" className="hatch-caution rounded-lg border border-caution/40 px-4 py-3 text-sm text-ink">
      <p className="font-semibold text-caution">Modo demostración: contenido educativo, no oficial</p>
      <p className="mt-1 max-w-3xl text-ink-soft">{version.provenanceNote}</p>
    </div>
  );
}
