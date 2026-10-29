export interface NavItem {
  to: string;
  label: string;
  available: boolean;
  summary: string;
}

/** Módulos de la plataforma. `available: false` = planificado, se muestra como tal. */
export const navItems: NavItem[] = [
  { to: '/', label: 'Inicio', available: true, summary: '' },
  { to: '/catalog', label: 'Catálogo BIAN', available: true, summary: '' },
  {
    to: '/workbench',
    label: 'Workbench',
    available: false,
    summary: 'Editor visual para componer soluciones con elementos del catálogo, canales y sistemas externos.',
  },
  {
    to: '/simulations',
    label: 'Simulaciones',
    available: false,
    summary: 'Ejecución paso a paso de flujos sobre una arquitectura, con fallos simulados y métricas.',
  },
  {
    to: '/scenarios',
    label: 'Laboratorio de escenarios',
    available: false,
    summary: 'Problemas bancarios guiados con requisitos, criterios de aceptación y solución de referencia.',
  },
  {
    to: '/assessments',
    label: 'Evaluaciones',
    available: false,
    summary: 'Cuestionarios de práctica y exámenes simulados con preguntas originales.',
  },
  {
    to: '/reports',
    label: 'Informes',
    available: false,
    summary: 'Informes de revisión arquitectónica en Markdown, HTML, JSON y PDF.',
  },
];
