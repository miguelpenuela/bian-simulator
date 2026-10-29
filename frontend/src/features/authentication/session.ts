import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { ApiError, apiFetch } from '../../services/http';
import type { CurrentUser } from '../../types/api';

export const sessionKey = ['session', 'me'] as const;

/** Usuario de la sesión actual; `null` si no hay sesión (401). */
export async function fetchCurrentUser(): Promise<CurrentUser | null> {
  try {
    return await apiFetch<CurrentUser>('/api/v1/auth/me');
  } catch (error) {
    if (error instanceof ApiError && error.status === 401) return null;
    throw error;
  }
}

export function useCurrentUser() {
  return useQuery({ queryKey: sessionKey, queryFn: fetchCurrentUser, staleTime: 60_000, retry: false });
}

export interface Credentials {
  username: string;
  password: string;
}

export function useLogin() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (credentials: Credentials) =>
      apiFetch<CurrentUser>('/api/v1/auth/login', { method: 'POST', body: credentials }),
    onSuccess: (user) => queryClient.setQueryData(sessionKey, user),
  });
}

export function useLogout() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => apiFetch<void>('/api/v1/auth/logout', { method: 'POST' }),
    onSettled: () => {
      queryClient.clear();
      queryClient.setQueryData(sessionKey, null);
    },
  });
}

export function hasPermission(user: CurrentUser | null | undefined, permission: string): boolean {
  return !!user && user.permissions.includes(permission);
}
