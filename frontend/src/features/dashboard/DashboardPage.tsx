import { Link } from 'react-router';
import { navItems } from '../../app/navigation';
import { useCurrentUser } from '../authentication/session';

export function DashboardPage() {
  const { data: user } = useCurrentUser();
  const planned = navItems.filter((item) => !item.available);

  return (
    <div className="max-w-5xl">
      <h1 className="text-2xl font-semibold tracking-tight">Hola, {user?.displayName}</h1>
      <p className="mt-2 max-w-2xl text-ink-soft">
        Empiece por el catálogo: busque un Service Domain, revise su responsabilidad y siga sus relaciones con otros
        dominios.
      </p>

      <Link
        to="/catalog"
        className="mt-8 block max-w-2xl rounded-lg border border-line bg-surface p-6 hover:border-ledger"
      >
        <span className="text-lg font-semibold">Explorar el catálogo</span>
        <span className="mt-1 block text-sm text-ink-soft">
          Agrupaciones, Service Domains, operaciones y Business Objects, con su procedencia indicada en cada ficha.
        </span>
      </Link>

      <section className="mt-12" aria-labelledby="activity-title">
        <h2 id="activity-title" className="text-lg font-semibold">
          Su actividad
        </h2>
        <p className="mt-2 text-sm text-ink-soft">
          Todavía no hay proyectos, simulaciones ni resultados de evaluación. Aparecerán aquí cuando esos módulos estén
          disponibles y los use.
        </p>
      </section>

      <section className="mt-12" aria-labelledby="roadmap-title">
        <h2 id="roadmap-title" className="text-lg font-semibold">
          Próximos módulos
        </h2>
        <ul className="mt-4 divide-y divide-line border-y border-line">
          {planned.map((item) => (
            <li key={item.to} className="grid gap-1 py-3 sm:grid-cols-[14rem_minmax(0,1fr)] sm:gap-6">
              <span className="font-medium">{item.label}</span>
              <span className="text-sm text-ink-soft">{item.summary}</span>
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
}
