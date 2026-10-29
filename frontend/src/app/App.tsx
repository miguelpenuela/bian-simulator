import { QueryCache, QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useState } from 'react';
import { RouterProvider } from 'react-router';
import { sessionKey } from '../features/authentication/session';
import { ApiError } from '../services/http';
import { createAppRouter } from './router';

export function createQueryClient() {
  const client: QueryClient = new QueryClient({
    // Si la sesión caduca durante el uso, cualquier 401 devuelve al login.
    queryCache: new QueryCache({
      onError: (error) => {
        if (error instanceof ApiError && error.status === 401) client.setQueryData(sessionKey, null);
      },
    }),
    defaultOptions: {
      queries: {
        retry: (count, error) => !(error instanceof ApiError && error.status < 500) && count < 2,
        refetchOnWindowFocus: false,
      },
    },
  });
  return client;
}

export function App() {
  const [queryClient] = useState(createQueryClient);
  const [router] = useState(createAppRouter);
  return (
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>
  );
}
