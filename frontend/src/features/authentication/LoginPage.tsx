import { zodResolver } from '@hookform/resolvers/zod';
import type { ReactNode } from 'react';
import { useForm } from 'react-hook-form';
import { Navigate, useLocation, useNavigate } from 'react-router';
import { z } from 'zod';
import { ApiError } from '../../services/http';
import { useCurrentUser, useLogin } from './session';

const loginSchema = z.object({
  username: z.string().trim().min(1, 'Introduzca su usuario').max(64, 'Máximo 64 caracteres'),
  password: z.string().min(1, 'Introduzca su contraseña').max(128, 'Máximo 128 caracteres'),
});

type LoginForm = z.infer<typeof loginSchema>;

function loginErrorMessage(error: unknown): string {
  if (error instanceof ApiError) {
    if (error.status === 401) return 'Usuario o contraseña incorrectos.';
    if (error.status === 429) return 'Demasiados intentos fallidos. Espere unos minutos e inténtelo de nuevo.';
    if (error.status === 403) return 'La sesión de seguridad caducó. Recargue la página e inténtelo de nuevo.';
  }
  return 'No se pudo conectar con el servidor. Compruebe su conexión e inténtelo de nuevo.';
}

export function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const from = (location.state as { from?: string } | null)?.from ?? '/';
  const { data: currentUser } = useCurrentUser();
  const login = useLogin();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginForm>({ resolver: zodResolver(loginSchema) });

  if (currentUser) return <Navigate to={from} replace />;

  const onSubmit = handleSubmit((values) =>
    login.mutate(values, { onSuccess: () => navigate(from, { replace: true }) }),
  );

  return (
    <main className="grid min-h-dvh lg:grid-cols-[minmax(0,1fr)_minmax(0,28rem)]">
      <section className="hidden flex-col justify-between bg-ink p-12 text-paper lg:flex">
        <p className="text-lg font-semibold tracking-tight">BIAN Architecture Simulator</p>
        <div className="max-w-xl">
          <h1 className="text-5xl leading-[1.05] font-semibold tracking-tight text-balance">
            Diseñe una solución bancaria, ejecútela y vea dónde falla.
          </h1>
          <p className="mt-6 max-w-md text-base leading-relaxed text-[#b9c5c2]">
            Explore Service Domains, componga arquitecturas, simule flujos y practique para evaluaciones de
            conocimiento BIAN.
          </p>
        </div>
        <p className="max-w-md text-sm text-[#8d9c99]">
          Plataforma educativa independiente. No está certificada por BIAN ni declara conformidad con el estándar.
        </p>
      </section>

      <section className="flex items-center justify-center bg-surface px-6 py-12">
        <form onSubmit={onSubmit} noValidate className="w-full max-w-sm" aria-labelledby="login-title">
          <h2 id="login-title" className="text-2xl font-semibold tracking-tight">
            Iniciar sesión
          </h2>
          <p className="mt-2 text-sm text-ink-soft">Use la cuenta que le asignó el administrador.</p>

          <div className="mt-8 space-y-5">
            <Field label="Usuario" id="username" error={errors.username?.message}>
              <input
                id="username"
                autoComplete="username"
                autoFocus
                aria-invalid={!!errors.username}
                aria-describedby={errors.username ? 'username-error' : undefined}
                className="input"
                {...register('username')}
              />
            </Field>
            <Field label="Contraseña" id="password" error={errors.password?.message}>
              <input
                id="password"
                type="password"
                autoComplete="current-password"
                aria-invalid={!!errors.password}
                aria-describedby={errors.password ? 'password-error' : undefined}
                className="input"
                {...register('password')}
              />
            </Field>
          </div>

          {login.isError && (
            <p role="alert" className="mt-6 rounded-md border border-danger/30 bg-danger-wash px-3 py-2 text-sm text-danger">
              {loginErrorMessage(login.error)}
            </p>
          )}

          <button
            type="submit"
            disabled={login.isPending}
            className="mt-8 w-full rounded-md bg-ledger px-4 py-2.5 text-sm font-semibold text-white hover:bg-ledger-strong disabled:opacity-60"
          >
            {login.isPending ? 'Iniciando sesión…' : 'Iniciar sesión'}
          </button>
        </form>
      </section>
    </main>
  );
}

function Field({
  label,
  id,
  error,
  children,
}: {
  label: string;
  id: string;
  error?: string;
  children: ReactNode;
}) {
  return (
    <div>
      <label htmlFor={id} className="block text-sm font-medium">
        {label}
      </label>
      <div className="mt-1.5">{children}</div>
      {error && (
        <p id={`${id}-error`} className="mt-1.5 text-sm text-danger">
          {error}
        </p>
      )}
    </div>
  );
}
