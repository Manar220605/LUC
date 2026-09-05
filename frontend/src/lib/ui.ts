export const pageShell = 'mx-auto w-full max-w-6xl px-4 py-8 sm:px-6 lg:px-8';
export const pageNarrow = 'mx-auto w-full max-w-3xl px-4 py-10 sm:px-6';
export const pageWide = 'mx-auto w-full max-w-6xl px-4 py-8 sm:px-6 lg:px-8';

export const heading = 'font-display text-3xl font-semibold tracking-tight text-lu-deep sm:text-4xl';
export const subheading = 'mt-2 max-w-2xl text-sm leading-6 text-muted sm:text-base';
export const sectionTitle = 'font-display text-xl font-semibold text-lu-deep';

export const card =
  'rounded-2xl border border-lu/10 bg-white shadow-[0_8px_30px_rgba(22,50,86,0.06)]';
export const cardPad = `${card} p-5 sm:p-6`;
export const listCard = `${card} divide-y divide-lu-soft overflow-hidden`;

export const label = 'block text-sm font-medium text-lu-deep';
export const input =
  'mt-1 w-full rounded-lg border border-lu/20 bg-white px-3 py-2 text-sm text-ink placeholder:text-muted/60 outline-none transition focus:border-lu focus:ring-2 focus:ring-lu/20';
export const select = input;

export const btnPrimary =
  'inline-flex items-center justify-center rounded-lg bg-lu px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-lu-dark disabled:cursor-not-allowed disabled:opacity-50';
export const btnSecondary =
  'inline-flex items-center justify-center rounded-lg border border-lu/20 bg-white px-4 py-2 text-sm font-semibold text-lu-dark transition hover:border-lu/40 hover:bg-lu-soft disabled:cursor-not-allowed disabled:opacity-50';
export const btnGhost =
  'inline-flex items-center justify-center rounded-lg px-3 py-2 text-sm font-medium text-lu-dark transition hover:bg-lu-soft';
export const btnTab = (active: boolean) =>
  active
    ? 'rounded-lg bg-lu px-3 py-1.5 text-sm font-semibold text-white'
    : 'rounded-lg px-3 py-1.5 text-sm font-medium text-lu-dark hover:bg-lu-soft';

export const link = 'font-medium text-lu hover:text-lu-dark';
export const muted = 'text-sm text-muted';
export const errorText = 'text-sm text-red-600';
export const alertError = 'rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700';
export const alertWarn = 'rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-800';
export const alertOk = 'rounded-lg border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-800';
