import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';
import { demoUser, json, mockApi, renderRoute } from '../../tests/test-utils';

const unauthorized = () => json({ status: 401, title: 'No autenticado' }, 401);
const csrf = () => {
  document.cookie = 'XSRF-TOKEN=token-123; path=/';
  return json({ headerName: 'X-XSRF-TOKEN', token: 'token-123' });
};

describe('LoginPage', () => {
  it('redirige a /login cuando no hay sesión', async () => {
    mockApi({ 'GET /api/v1/auth/me': unauthorized });
    const { router } = renderRoute('/catalog');
    expect(await screen.findByRole('heading', { name: 'Iniciar sesión' })).toBeInTheDocument();
    expect(router.state.location.pathname).toBe('/login');
  });

  it('valida los campos obligatorios sin llamar al servidor', async () => {
    const fetchMock = mockApi({ 'GET /api/v1/auth/me': unauthorized });
    renderRoute('/login');
    await userEvent.click(await screen.findByRole('button', { name: 'Iniciar sesión' }));
    expect(await screen.findByText('Introduzca su usuario')).toBeInTheDocument();
    expect(screen.getByText('Introduzca su contraseña')).toBeInTheDocument();
    expect(fetchMock.mock.calls.some(([url]) => String(url).includes('/login'))).toBe(false);
  });

  it('envía las credenciales con el token CSRF y vuelve a la ruta pedida', async () => {
    let loginRequest: RequestInit | undefined;
    mockApi({
      'GET /api/v1/auth/me': unauthorized,
      'GET /api/v1/auth/csrf': csrf,
      'POST /api/v1/auth/login': (_url, init) => {
        loginRequest = init;
        return json(demoUser);
      },
    });
    const { router } = renderRoute('/');
    await userEvent.type(await screen.findByLabelText('Usuario'), 'demo');
    await userEvent.type(screen.getByLabelText('Contraseña'), 'secreto');
    await userEvent.click(screen.getByRole('button', { name: 'Iniciar sesión' }));

    await waitFor(() => expect(router.state.location.pathname).toBe('/'));
    expect(await screen.findByRole('heading', { name: /Hola, Usuario de demostración/ })).toBeInTheDocument();
    expect(JSON.parse(String(loginRequest?.body))).toEqual({ username: 'demo', password: 'secreto' });
    expect((loginRequest?.headers as Record<string, string>)['X-XSRF-TOKEN']).toBe('token-123');
  });

  it('muestra un mensaje claro con credenciales incorrectas', async () => {
    mockApi({
      'GET /api/v1/auth/me': unauthorized,
      'GET /api/v1/auth/csrf': csrf,
      'POST /api/v1/auth/login': () => json({ status: 401, title: 'Credenciales inválidas' }, 401),
    });
    renderRoute('/login');
    await userEvent.type(await screen.findByLabelText('Usuario'), 'demo');
    await userEvent.type(screen.getByLabelText('Contraseña'), 'mal');
    await userEvent.click(screen.getByRole('button', { name: 'Iniciar sesión' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('Usuario o contraseña incorrectos.');
  });

  it('informa del bloqueo temporal tras demasiados intentos', async () => {
    mockApi({
      'GET /api/v1/auth/me': unauthorized,
      'GET /api/v1/auth/csrf': csrf,
      'POST /api/v1/auth/login': () => json({ status: 429 }, 429),
    });
    renderRoute('/login');
    await userEvent.type(await screen.findByLabelText('Usuario'), 'demo');
    await userEvent.type(screen.getByLabelText('Contraseña'), 'x');
    await userEvent.click(screen.getByRole('button', { name: 'Iniciar sesión' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('Demasiados intentos fallidos');
  });
});
