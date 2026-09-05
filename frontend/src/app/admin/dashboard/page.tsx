'use client';

import { useCallback, useEffect, useState } from 'react';
import { useSession } from 'next-auth/react';
import { useRouter } from 'next/navigation';
import { clientApiRequest } from '@/lib/clientApi';
import { getErrorMessage, isApiError } from '@/lib/apiError';
import { DASHBOARD_METRICS, type DashboardMetricsResponseDTO } from '@/lib/dashboard';

export default function AdminDashboardPage() {
  const { status } = useSession();
  const router = useRouter();
  const [metrics, setMetrics] = useState<DashboardMetricsResponseDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [forbidden, setForbidden] = useState(false);

  const loadDashboard = useCallback(async () => {
    setLoading(true);
    setError(null);
    setForbidden(false);

    try {
      const data = await clientApiRequest<DashboardMetricsResponseDTO>('/api/admin/dashboard');
      setMetrics(data);
    } catch (err) {
      if (isApiError(err, 401)) {
        router.push('/auth/signin');
        return;
      }
      if (isApiError(err, 403)) {
        setForbidden(true);
        setMetrics(null);
        return;
      }
      setMetrics(null);
      setError(getErrorMessage(err, 'Failed to load dashboard'));
    } finally {
      setLoading(false);
    }
  }, [router]);

  useEffect(() => {
    if (status === 'unauthenticated') {
      router.push('/auth/signin');
      return;
    }
    if (status === 'authenticated') {
      loadDashboard();
    }
  }, [status, router, loadDashboard]);

  if (status === 'loading' || loading) {
    return (
      <main className="mx-auto max-w-5xl px-4 py-8">
        <p className="text-muted">Loading dashboard…</p>
      </main>
    );
  }

  if (forbidden) {
    return (
      <main className="mx-auto max-w-5xl px-4 py-8">
        <h1 className="text-2xl font-semibold text-lu-deep">Admin dashboard</h1>
        <p className="mt-4 rounded border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-800">
          You do not have permission to view this page.
        </p>
      </main>
    );
  }

  return (
    <main className="mx-auto max-w-5xl px-4 py-8">
      <h1 className="text-2xl font-semibold text-lu-deep">Admin dashboard</h1>
      <p className="mt-1 text-sm text-muted">Platform totals and moderation queue sizes.</p>

      {error && (
        <p className="mt-4 rounded border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
          {error}
        </p>
      )}

      {metrics && (
        <section className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {DASHBOARD_METRICS.map((metric) => (
            <article
              key={metric.key}
              className="rounded-lg border border-lu/10 bg-white p-5 shadow-sm"
            >
              <p className="text-sm font-medium text-muted">{metric.label}</p>
              <p className="mt-2 text-3xl font-semibold text-lu-deep">
                {metrics[metric.key].toLocaleString()}
              </p>
              <p className="mt-2 text-sm text-muted">{metric.description}</p>
            </article>
          ))}
        </section>
      )}
    </main>
  );
}
