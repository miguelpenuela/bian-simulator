import type { ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router';
import { ErrorState, LoadingState } from '../../components/States';
import { useCurrentUser } from './session';

/** Guarda de ruta: sin sesión redirige a /login conservando el destino. */
export function RequireAuth({ children }: { children: ReactNode }) {
  const location = useLocation();
  const { data: user, isPending, isError, refetch } = useCurrentUser();

  if (isPending) return <LoadingState label="Comprobando la sesión" />;
  if (isError) {
    return (
      <ErrorState
        title="No se pudo comprobar la sesión"
        detail="El servidor no respondió. Verifique que el backend esté en marcha."
        onRetry={() => void refetch()}
      />
    );
  }
  if (!user) {
    return <Navigate to="/login" replace state={{ from: location.pathname + location.search }} />;
  }
  return children;
}
