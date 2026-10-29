import type { ElementType } from '../../types/api';
import { elementTypeGlyph, elementTypeLabel } from './labels';

export function TypeGlyph({ type, size = 'md' }: { type: ElementType; size?: 'md' | 'lg' }) {
  const glyph = elementTypeGlyph[type];
  const dimension = size === 'lg' ? 'size-10 text-sm' : 'size-7 text-[11px]';
  return (
    <span
      title={elementTypeLabel[type]}
      aria-label={elementTypeLabel[type]}
      className={`inline-flex shrink-0 items-center justify-center rounded font-mono font-semibold text-white ${dimension}`}
      style={{ backgroundColor: glyph.color }}
    >
      {glyph.code}
    </span>
  );
}
