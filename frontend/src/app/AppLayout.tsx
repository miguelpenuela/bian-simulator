import { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router';
import { useCurrentUser, useLogout } from '../features/authentication/session';
import { navItems } from './navigation';

export function AppLayout() {
  const { data: user } = useCurrentUser();
  const logout = useLogout();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);

  const signOut = () => logout.mutate(undefined, { onSettled: () => navigate('/login', { replace: true }) });

  return (
    <div className="min-h-dvh lg:grid lg:grid-cols-[15rem_minmax(0,1fr)]">
      <a
        href="#main"
        className="sr-only focus:not-sr-only focus:absolute focus:top-2 focus:left-2 focus:z-50 focus:rounded focus:bg-surface focus:px-3 focus:py-2"
      >
        Saltar al contenido
      </a>
      <header className="flex flex-col bg-ink text-paper lg:sticky lg:top-0 lg:h-dvh">
        <div className="flex items-center justify-between px-5 py-4">
          <span className="font-semibold tracking-tight">BIAN Simulator</span>
          <button
            type="button"
            className="rounded px-2 py-1 text-sm text-[#cdd6d4] lg:hidden"
            aria-expanded={menuOpen}
            aria-controls="main-nav"
            onClick={() => setMenuOpen((open) => !open)}
          >
            {menuOpen ? 'Cerrar menú' : 'Menú'}
          </button>
        </div>
        <nav
          id="main-nav"
          aria-label="Principal"
          className={`${menuOpen ? 'block' : 'hidden'} flex-1 px-3 pb-4 lg:block`}
        >
          <ul className="space-y-0.5">
            {navItems.map((item) => (
              <li key={item.to}>
                <NavLink
                  to={item.to}
                  end={item.to === '/'}
                  onClick={() => setMenuOpen(false)}
                  className={({ isActive }) =>
                    `flex items-center justify-between rounded-md px-3 py-2 text-sm ${
                      isActive ? 'bg-white/10 font-semibold text-white' : 'text-[#b9c5c2] hover:bg-white/5 hover:text-white'
                    }`
                  }
                >
                  {item.label}
                  {!item.available && <span className="text-xs text-[#7f8e8b]">pronto</span>}
                </NavLink>
              </li>
            ))}
          </ul>
        </nav>
        {user && (
          <div className={`${menuOpen ? 'block' : 'hidden'} border-t border-white/10 px-5 py-4 text-sm lg:block`}>
            <p className="font-medium">{user.displayName}</p>
            <p className="text-xs text-[#93a29f]">{user.roles.join(', ').toLowerCase()}</p>
            <button
              type="button"
              onClick={signOut}
              disabled={logout.isPending}
              className="mt-3 text-sm text-[#cdd6d4] underline underline-offset-2 hover:text-white"
            >
              Cerrar sesión
            </button>
          </div>
        )}
      </header>
      <main id="main" className="px-4 py-6 sm:px-8 lg:px-10 lg:py-8">
        <Outlet />
      </main>
    </div>
  );
}
