'use client';

import { useCallback, useEffect, useState } from 'react';
import { getSession, useSession } from 'next-auth/react';
import { useRouter } from 'next/navigation';
import { DASHBOARD_METRICS, type DashboardMetricsResponseDTO } from '@/lib/dashboard';
import type { UserResponseDTO } from '@/lib/types';

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080';

async function fetchWithAuth<T>(path: string): Promise<{ status: number; data?: T }> {
  const session = await getSession();
  if (session?.error === 'RefreshAccessTokenError') {
    throw new Error('Session expired; please sign in again');
  }
  const token = session?.accessToken;
  const res = await fetch(`${API_URL}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(token && { Authorization: `Bearer ${token}` }),
    },
    cache: 'no-store',
  });

  if (res.status === 204) {
    return { status: res.status };
  }

  const data = (await res.json()) as T;
  return { status: res.status, data };
}

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
      const profileResult = await fetchWithAuth<UserResponseDTO>('/api/me');
      if (profileResult.status === 401) {
        router.push('/auth/signin');
        return;
      }
      if (profileResult.status !== 200 || !profileResult.data) {
        throw new Error(`API ${profileResult.status}: Failed to load profile`);
      }
      if (profileResult.data.role !== 'ADMIN') {
        setForbidden(true);
        setMetrics(null);
        return;
      }

      const metricsResult = await fetchWithAuth<DashboardMetricsResponseDTO>('/api/admin/dashboard');
      if (metricsResult.status === 403) {
        setForbidden(true);
        setMetrics(null);
        return;
      }
      if (metricsResult.status !== 200 || !metricsResult.data) {
        throw new Error(`API ${metricsResult.status}: Failed to load dashboard metrics`);
      }

      setMetrics(metricsResult.data);
    } catch (err) {
      setMetrics(null);
      setError(err instanceof Error ? err.message : 'Failed to load dashboard');
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
        <p className="text-gray-600">Loading dashboard…</p>
      </main>
    );
  }

  if (forbidden) {
    return (
      <main className="mx-auto max-w-5xl px-4 py-8">
        <h1 className="text-2xl font-semibold text-gray-900">Admin dashboard</h1>
        <p className="mt-4 rounded border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-800">
          You do not have permission to view this page.
        </p>
      </main>
    );
  }

  return (
    <main className="mx-auto max-w-5xl px-4 py-8">
      <h1 className="text-2xl font-semibold text-gray-900">Admin dashboard</h1>
      <p className="mt-1 text-sm text-gray-600">Platform totals and moderation queue sizes.</p>

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
              className="rounded-lg border border-gray-200 bg-white p-5 shadow-sm"
            >
              <p className="text-sm font-medium text-gray-500">{metric.label}</p>
              <p className="mt-2 text-3xl font-semibold text-gray-900">
                {metrics[metric.key].toLocaleString()}
              </p>
              <p className="mt-2 text-sm text-gray-600">{metric.description}</p>
            </article>
          ))}
        </section>
      )}
    </main>
  );
}
