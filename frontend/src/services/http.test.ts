import { describe, expect, it } from 'vitest';
import { json, mockApi } from '../tests/test-utils';
import { ApiError, apiFetch, toQuery } from './http';

describe('toQuery', () => {
  it('omite valores vacíos y repite los arrays', () => {
    expect(toQuery({ q: '', type: ['A', 'B'], page: 0, category: undefined })).toBe('?type=A&type=B&page=0');
    expect(toQuery({})).toBe('');
  });
});

describe('apiFetch', () => {
  it('convierte un Problem Detail en ApiError', async () => {
    mockApi({ 'GET /x': () => json({ status: 404, detail: 'No existe' }, 404) });
    const error = await apiFetch('/x').catch((e: unknown) => e);
    expect(error).toBeInstanceOf(ApiError);
    expect((error as ApiError).status).toBe(404);
    expect((error as ApiError).message).toBe('No existe');
  });

  it('obtiene el token CSRF antes de una petición que modifica estado', async () => {
    const fetchMock = mockApi({
      'GET /api/v1/auth/csrf': () => json({ headerName: 'X-XSRF-TOKEN', token: 'abc' }),
      'POST /y': () => new Response(null, { status: 204 }),
    });
    await apiFetch('/y', { method: 'POST' });
    const [, init] = fetchMock.mock.calls.at(-1)!;
    expect((init?.headers as Record<string, string>)['X-XSRF-TOKEN']).toBe('abc');
  });
});
