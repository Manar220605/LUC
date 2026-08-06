'use client';

import Link from 'next/link';
import { FormEvent, useCallback, useEffect, useState } from 'react';
import { getSession, useSession } from 'next-auth/react';
import { useRouter } from 'next/navigation';
import {
  REPORT_REASON_LABELS,
  REPORT_TARGET_LABELS,
  RESOLUTION_ACTIONS,
  type AdminReportResponseDTO,
  type ReportStatus,
  type ReportTargetType,
  type ResolutionAction,
} from '@/lib/report';
import type { PageResponseDTO } from '@/lib/types';

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080';

const STATUS_OPTIONS: { value: '' | ReportStatus; label: string }[] = [
  { value: '', label: 'All statuses' },
  { value: 'PENDING', label: 'Pending' },
  { value: 'RESOLVED', label: 'Resolved' },
];

const TARGET_OPTIONS: { value: '' | ReportTargetType; label: string }[] = [
  { value: '', label: 'All targets' },
  { value: 'QUESTION', label: 'Questions' },
  { value: 'ANSWER', label: 'Answers' },
  { value: 'USER', label: 'Users' },
];

async function apiRequest<T>(path: string, init: RequestInit = {}): Promise<T> {
  const session = await getSession();
  if (session?.error === 'RefreshAccessTokenError') {
    throw new Error('Session expired; please sign in again');
  }
  const token = session?.accessToken;
  const res = await fetch(`${API_URL}${path}`, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...(token && { Authorization: `Bearer ${token}` }),
      ...init.headers,
    },
  });
  if (!res.ok) {
    throw new Error(`API ${res.status}: ${await res.text()}`);
  }
  if (res.status === 204) {
    return undefined as T;
  }
  return res.json();
}

export default function AdminReportsPage() {
  const { status } = useSession();
  const router = useRouter();
  const [items, setItems] = useState<AdminReportResponseDTO[]>([]);
  const [statusFilter, setStatusFilter] = useState<'' | ReportStatus>('PENDING');
  const [targetFilter, setTargetFilter] = useState<'' | ReportTargetType>('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionId, setActionId] = useState<number | null>(null);
  const [resolveId, setResolveId] = useState<number | null>(null);
  const [resolutionNote, setResolutionNote] = useState('');
  const [resolutionAction, setResolutionAction] = useState<ResolutionAction>('NONE');

  const loadQueue = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = new URLSearchParams({ page: String(page), size: '20' });
      if (statusFilter) {
        params.set('status', statusFilter);
      }
      if (targetFilter) {
        params.set('targetType', targetFilter);
      }
      const response = await apiRequest<PageResponseDTO<AdminReportResponseDTO>>(
        `/api/admin/reports?${params.toString()}`
      );
      setItems(response.content);
      setTotalPages(response.totalPages);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load report queue');
    } finally {
      setLoading(false);
    }
  }, [page, statusFilter, targetFilter]);

  useEffect(() => {
    if (status === 'unauthenticated') {
      router.push('/auth/signin');
      return;
    }
    if (status === 'authenticated') {
      loadQueue();
    }
  }, [status, router, loadQueue]);

  async function handleResolve(event: FormEvent) {
    event.preventDefault();
    if (resolveId == null) {
      return;
    }
    setActionId(resolveId);
    setError(null);
    try {
      await apiRequest(`/api/admin/reports/${resolveId}/resolve`, {
        method: 'POST',
        body: JSON.stringify({
          action: resolutionAction,
          resolutionNote: resolutionNote.trim() || undefined,
        }),
      });
      setResolveId(null);
      setResolutionNote('');
      setResolutionAction('NONE');
      await loadQueue();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Resolve failed');
    } finally {
      setActionId(null);
    }
  }

  if (status === 'loading' || loading) {
    return (
      <main className="mx-auto max-w-5xl px-4 py-8">
        <p className="text-gray-600">Loading report queue…</p>
      </main>
    );
  }

  return (
    <main className="mx-auto max-w-5xl px-4 py-8">
      <h1 className="text-2xl font-semibold text-gray-900">Moderation reports</h1>
      <p className="mt-1 text-sm text-gray-600">
        Review user reports and resolve them with optional moderation actions.
      </p>

      {error && (
        <p className="mt-4 rounded border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
          {error}
        </p>
      )}

      <div className="mt-6 flex flex-wrap items-center gap-3">
        <label className="text-sm text-gray-700">
          Status
          <select
            className="ml-2 rounded border border-gray-300 px-2 py-1"
            value={statusFilter}
            onChange={(event) => {
              setPage(0);
              setStatusFilter(event.target.value as '' | ReportStatus);
            }}
          >
            {STATUS_OPTIONS.map((option) => (
              <option key={option.label} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </label>
        <label className="text-sm text-gray-700">
          Target
          <select
            className="ml-2 rounded border border-gray-300 px-2 py-1"
            value={targetFilter}
            onChange={(event) => {
              setPage(0);
              setTargetFilter(event.target.value as '' | ReportTargetType);
            }}
          >
            {TARGET_OPTIONS.map((option) => (
              <option key={option.label} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </label>
      </div>

      {resolveId != null && (
        <section className="mt-6 rounded-lg border border-blue-200 bg-blue-50 p-4">
          <h2 className="text-lg font-medium text-gray-900">Resolve report #{resolveId}</h2>
          <form onSubmit={handleResolve} className="mt-3 grid gap-3">
            <fieldset className="space-y-2">
              <legend className="text-sm font-medium text-gray-700">Action</legend>
              {RESOLUTION_ACTIONS.map((option) => (
                <label key={option.value} className="flex items-start gap-2 text-sm text-gray-700">
                  <input
                    type="radio"
                    name="resolutionAction"
                    value={option.value}
                    checked={resolutionAction === option.value}
                    onChange={() => setResolutionAction(option.value)}
                  />
                  <span>
                    <span className="font-medium">{option.label}</span>
                    <span className="block text-gray-500">{option.hint}</span>
                  </span>
                </label>
              ))}
            </fieldset>
            <label className="text-sm">
              Resolution note (optional)
              <textarea
                className="mt-1 w-full rounded border border-gray-300 px-2 py-1"
                rows={3}
                value={resolutionNote}
                onChange={(event) => setResolutionNote(event.target.value)}
              />
            </label>
            <div className="flex gap-2">
              <button
                type="submit"
                disabled={actionId === resolveId}
                className="rounded bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
              >
                Confirm resolve
              </button>
              <button
                type="button"
                onClick={() => {
                  setResolveId(null);
                  setResolutionNote('');
                  setResolutionAction('NONE');
                }}
                className="rounded border border-gray-300 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
              >
                Cancel
              </button>
            </div>
          </form>
        </section>
      )}

      <section className="mt-8">
        {items.length === 0 ? (
          <p className="text-sm text-gray-500">No reports found.</p>
        ) : (
          <ul className="divide-y divide-gray-200 rounded-lg border border-gray-200 bg-white">
            {items.map((item) => (
              <li key={item.id} className="px-4 py-4">
                <div className="flex flex-wrap items-start justify-between gap-4">
                  <div className="min-w-0 flex-1">
                    <p className="font-medium text-gray-900">
                      #{item.id} · {REPORT_TARGET_LABELS[item.targetType]} #{item.targetId} ·{' '}
                      {REPORT_REASON_LABELS[item.reason]}
                    </p>
                    <p className="mt-1 text-sm text-gray-600">
                      Reported by {item.reporterDisplayName} ({item.reporterEmail})
                    </p>
                    {item.details && (
                      <p className="mt-2 text-sm text-gray-700">Details: {item.details}</p>
                    )}
                    <pre className="mt-3 whitespace-pre-wrap rounded border border-gray-100 bg-gray-50 p-3 text-sm text-gray-800">
                      {item.targetPreview}
                    </pre>
                    {item.targetAuthor && (
                      <p className="mt-2 text-sm text-gray-700">
                        Author: {item.targetAuthor.displayName} ({item.targetAuthor.email})
                        {item.targetAuthor.postedAnonymously && (
                          <span className="ml-2 rounded bg-amber-100 px-2 py-0.5 text-xs font-medium text-amber-800">
                            Posted anonymously
                          </span>
                        )}
                      </p>
                    )}
                    <p className="mt-2 text-xs text-gray-500">
                      Submitted {new Date(item.createdAt).toLocaleString()} · Status: {item.status}
                      {item.resolvedAt && (
                        <>
                          {' '}
                          · Resolved {new Date(item.resolvedAt).toLocaleString()}
                          {item.resolverDisplayName && ` by ${item.resolverDisplayName}`}
                        </>
                      )}
                    </p>
                    {item.resolutionNote && (
                      <p className="mt-1 text-sm text-gray-600">Note: {item.resolutionNote}</p>
                    )}
                    <div className="mt-2 flex flex-wrap gap-3 text-sm">
                      {item.targetQuestionId != null && (
                        <Link
                          href={`/questions/${item.targetQuestionId}`}
                          className="font-medium text-blue-600 hover:text-blue-800"
                        >
                          Open question
                        </Link>
                      )}
                    </div>
                  </div>
                  {item.status === 'PENDING' && (
                    <button
                      type="button"
                      onClick={() => {
                        setResolveId(item.id);
                        setResolutionNote('');
                        setResolutionAction('NONE');
                      }}
                      disabled={actionId === item.id}
                      className="shrink-0 rounded bg-blue-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
                    >
                      Resolve
                    </button>
                  )}
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>

      {totalPages > 1 && (
        <div className="mt-6 flex items-center justify-between text-sm text-gray-600">
          <button
            type="button"
            disabled={page === 0}
            onClick={() => setPage((current) => Math.max(0, current - 1))}
            className="rounded border border-gray-300 px-3 py-1.5 disabled:opacity-50"
          >
            Previous
          </button>
          <span>
            Page {page + 1} of {totalPages}
          </span>
          <button
            type="button"
            disabled={page + 1 >= totalPages}
            onClick={() => setPage((current) => current + 1)}
            className="rounded border border-gray-300 px-3 py-1.5 disabled:opacity-50"
          >
            Next
          </button>
        </div>
      )}
    </main>
  );
}
