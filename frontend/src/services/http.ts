import type { ProblemDetail } from '../types/api';

const CSRF_COOKIE = 'XSRF-TOKEN';
const CSRF_HEADER = 'X-XSRF-TOKEN';

/** Error de la API con el Problem Detail (RFC 9457) devuelto por el backend. */
export class ApiError extends Error {
  readonly status: number;
  readonly problem: ProblemDetail;

  constructor(status: number, problem: ProblemDetail) {
    super(problem.detail ?? problem.title ?? `Error HTTP ${status}`);
    this.name = 'ApiError';
    this.status = status;
    this.problem = problem;
  }
}

export function readCookie(name: string): string | undefined {
  return document.cookie
    .split('; ')
    .find((part) => part.startsWith(`${name}=`))
    ?.slice(name.length + 1);
}

/** Solicita el token CSRF; el backend lo emite en la cookie XSRF-TOKEN. */
export async function ensureCsrfToken(): Promise<string> {
  const existing = readCookie(CSRF_COOKIE);
  if (existing) return decodeURIComponent(existing);
  const response = await fetch('/api/v1/auth/csrf', { credentials: 'same-origin' });
  if (!response.ok) throw new ApiError(response.status, { title: 'No se pudo obtener el token CSRF' });
  const body = (await response.json()) as { token: string };
  return body.token;
}

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';
  body?: unknown;
  signal?: AbortSignal;
}

export async function apiFetch<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const method = options.method ?? 'GET';
  const headers: Record<string, string> = { Accept: 'application/json, application/problem+json' };
  if (options.body !== undefined) headers['Content-Type'] = 'application/json';
  if (method !== 'GET') headers[CSRF_HEADER] = await ensureCsrfToken();

  const response = await fetch(path, {
    method,
    headers,
    credentials: 'same-origin',
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
    signal: options.signal,
  });

  if (response.status === 204) return undefined as T;

  const contentType = response.headers.get('Content-Type') ?? '';
  const isJson = contentType.includes('json');
  if (!response.ok) {
    const problem: ProblemDetail = isJson
      ? ((await response.json()) as ProblemDetail)
      : { status: response.status, title: response.statusText };
    throw new ApiError(response.status, problem);
  }
  return (isJson ? await response.json() : undefined) as T;
}

/** Construye una query string omitiendo valores vacíos; los arrays se repiten (type=A&type=B). */
export function toQuery(params: Record<string, string | number | string[] | undefined | null>): string {
  const search = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value === undefined || value === null || value === '') continue;
    if (Array.isArray(value)) value.forEach((v) => search.append(key, v));
    else search.append(key, String(value));
  }
  const text = search.toString();
  return text ? `?${text}` : '';
}
