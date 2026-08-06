'use client';

import { FormEvent, useCallback, useEffect, useState } from 'react';
import { getSession, useSession } from 'next-auth/react';
import { useRouter } from 'next/navigation';
import { degreeLabel, facultyLabel } from '@/lib/alumni';
import type {
  AdminVerificationResponseDTO,
  PageResponseDTO,
  VerificationStatus,
} from '@/lib/types';

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080';

const STATUS_OPTIONS: { value: '' | VerificationStatus; label: string }[] = [
  { value: '', label: 'All statuses' },
  { value: 'PENDING', label: 'Pending' },
  { value: 'APPROVED', label: 'Approved' },
  { value: 'REJECTED', label: 'Rejected' },
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

export default function AdminVerificationsPage() {
  const { status } = useSession();
  const router = useRouter();
  const [items, setItems] = useState<AdminVerificationResponseDTO[]>([]);
  const [statusFilter, setStatusFilter] = useState<'' | VerificationStatus>('PENDING');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionId, setActionId] = useState<number | null>(null);
  const [rejectId, setRejectId] = useState<number | null>(null);
  const [rejectionReason, setRejectionReason] = useState('');

  const loadQueue = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = new URLSearchParams({ page: '0', size: '50' });
      if (statusFilter) {
        params.set('status', statusFilter);
      }
      const page = await apiRequest<PageResponseDTO<AdminVerificationResponseDTO>>(
        `/api/admin/verifications?${params.toString()}`
      );
      setItems(page.content);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load verification queue');
    } finally {
      setLoading(false);
    }
  }, [statusFilter]);

  useEffect(() => {
    if (status === 'unauthenticated') {
      router.push('/auth/signin');
      return;
    }
    if (status === 'authenticated') {
      loadQueue();
    }
  }, [status, router, loadQueue]);

  async function handleApprove(id: number) {
    setActionId(id);
    setError(null);
    try {
      await apiRequest(`/api/admin/verifications/${id}/approve`, { method: 'POST' });
      setRejectId(null);
      await loadQueue();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Approve failed');
    } finally {
      setActionId(null);
    }
  }

  async function handleReject(e: FormEvent) {
    e.preventDefault();
    if (rejectId == null) {
      return;
    }
    setActionId(rejectId);
    setError(null);
    try {
      await apiRequest(`/api/admin/verifications/${rejectId}/reject`, {
        method: 'POST',
        body: JSON.stringify({ rejectionReason: rejectionReason.trim() }),
      });
      setRejectId(null);
      setRejectionReason('');
      await loadQueue();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Reject failed');
    } finally {
      setActionId(null);
    }
  }

  if (status === 'loading' || loading) {
    return (
      <main className="mx-auto max-w-4xl px-4 py-8">
        <p className="text-gray-600">Loading verification queue…</p>
      </main>
    );
  }

  return (
    <main className="mx-auto max-w-4xl px-4 py-8">
      <h1 className="text-2xl font-semibold text-gray-900">Alumni verification queue</h1>
      <p className="mt-1 text-sm text-gray-600">Review pending requests and assign the Alumni role.</p>

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
            onChange={(e) => setStatusFilter(e.target.value as '' | VerificationStatus)}
          >
            {STATUS_OPTIONS.map((option) => (
              <option key={option.label} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </label>
      </div>

      {rejectId != null && (
        <section className="mt-6 rounded-lg border border-red-200 bg-red-50 p-4">
          <h2 className="text-lg font-medium text-gray-900">Reject request #{rejectId}</h2>
          <form onSubmit={handleReject} className="mt-3 grid gap-3">
            <label className="text-sm">
              Rejection reason
              <textarea
                className="mt-1 w-full rounded border border-gray-300 px-2 py-1"
                rows={3}
                value={rejectionReason}
                onChange={(e) => setRejectionReason(e.target.value)}
                required
              />
            </label>
            <div className="flex gap-2">
              <button
                type="submit"
                disabled={actionId === rejectId}
                className="rounded bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700 disabled:opacity-50"
              >
                Confirm reject
              </button>
              <button
                type="button"
                onClick={() => {
                  setRejectId(null);
                  setRejectionReason('');
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
          <p className="text-sm text-gray-500">No verification requests found.</p>
        ) : (
          <ul className="divide-y divide-gray-200 rounded-lg border border-gray-200 bg-white">
            {items.map((item) => (
              <li key={item.id} className="px-4 py-4">
                <div className="flex flex-wrap items-start justify-between gap-4">
                  <div className="min-w-0">
                    <p className="font-medium text-gray-900">
                      {item.userDisplayName}{' '}
                      <span className="font-normal text-gray-500">({item.userEmail})</span>
                    </p>
                    <p className="mt-1 text-sm text-gray-600">
                      {item.claimedGradYear} · {facultyLabel(item.claimedFaculty)} ·{' '}
                      {degreeLabel(item.claimedDegree)} · {item.claimedMajor}
                    </p>
                    {(item.claimedPosition || item.claimedCompany) && (
                      <p className="mt-1 text-sm text-gray-600">
                        {[item.claimedPosition, item.claimedCompany].filter(Boolean).join(' at ')}
                      </p>
                    )}
                    <p className="mt-2 text-sm">
                      <a
                        href={item.linkedinUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="font-medium text-blue-600 hover:text-blue-800"
                      >
                        Open LinkedIn profile
                      </a>
                    </p>
                    <p className="mt-1 text-xs text-gray-500">
                      Submitted {new Date(item.submittedAt).toLocaleString()} · Status:{' '}
                      {item.status}
                    </p>
                    {item.rejectionReason && (
                      <p className="mt-1 text-sm text-red-700">Reason: {item.rejectionReason}</p>
                    )}
                  </div>
                  {item.status === 'PENDING' && (
                    <div className="flex shrink-0 gap-2">
                      <button
                        type="button"
                        onClick={() => handleApprove(item.id)}
                        disabled={actionId === item.id}
                        className="rounded bg-green-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-green-700 disabled:opacity-50"
                      >
                        Approve
                      </button>
                      <button
                        type="button"
                        onClick={() => setRejectId(item.id)}
                        disabled={actionId === item.id}
                        className="rounded border border-red-300 px-3 py-1.5 text-sm font-medium text-red-700 hover:bg-red-50 disabled:opacity-50"
                      >
                        Reject
                      </button>
                    </div>
                  )}
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>
    </main>
  );
}
