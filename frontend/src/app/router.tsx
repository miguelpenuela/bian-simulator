import { createBrowserRouter, type RouteObject } from 'react-router';
import { NotImplementedPage } from '../components/NotImplementedPage';
import { LoginPage } from '../features/authentication/LoginPage';
import { RequireAuth } from '../features/authentication/RequireAuth';
import { CatalogExplorerPage } from '../features/bian-catalog/CatalogExplorerPage';
import { ElementDetailPage } from '../features/bian-catalog/ElementDetailPage';
import { DashboardPage } from '../features/dashboard/DashboardPage';
import { AppLayout } from './AppLayout';
import { navItems } from './navigation';

export const routes: RouteObject[] = [
  { path: '/login', element: <LoginPage /> },
  {
    path: '/',
    element: (
      <RequireAuth>
        <AppLayout />
      </RequireAuth>
    ),
    children: [
      { index: true, element: <DashboardPage /> },
      { path: 'catalog', element: <CatalogExplorerPage /> },
      { path: 'catalog/elements/:id', element: <ElementDetailPage /> },
      ...navItems
        .filter((item) => !item.available)
        .map((item) => ({
          path: item.to.slice(1),
          element: <NotImplementedPage title={item.label} summary={item.summary} />,
        })),
      {
        path: '*',
        element: <NotImplementedPage title="Página no encontrada" summary="La dirección no corresponde a ninguna sección." />,
      },
    ],
  },
];

export function createAppRouter() {
  return createBrowserRouter(routes);
}
