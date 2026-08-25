'use client';

import { useCallback, useEffect, useState } from 'react';
import Link from 'next/link';
import { useSession } from 'next-auth/react';
import { clientApiRequest } from '@/lib/clientApi';
import { getErrorMessage } from '@/lib/apiError';
import type { SaveStatusDTO } from '@/lib/types';

type Props = {
  questionId: number;
};

export default function SaveQuestionButton({ questionId }: Props) {
  const { status: sessionStatus } = useSession();
  const [status, setStatus] = useState<SaveStatusDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadStatus = useCallback(async () => {
    if (sessionStatus !== 'authenticated') {
      setStatus(null);
      setLoading(false);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const result = await clientApiRequest<SaveStatusDTO>(
        `/api/saves/status?questionId=${questionId}`
      );
      setStatus(result);
    } catch (err) {
      setError(getErrorMessage(err, 'Could not load save status'));
    } finally {
      setLoading(false);
    }
  }, [questionId, sessionStatus]);

  useEffect(() => {
    loadStatus().catch(() => undefined);
  }, [loadStatus]);

  async function toggleSave() {
    if (!status) {
      return;
    }
    setBusy(true);
    setError(null);
    try {
      const result = status.saved
        ? await clientApiRequest<SaveStatusDTO>(
            `/api/saves?questionId=${questionId}`,
            { method: 'DELETE' }
          )
        : await clientApiRequest<SaveStatusDTO>('/api/saves', {
            method: 'POST',
            body: JSON.stringify({ questionId }),
          });
      setStatus(result);
    } catch (err) {
      setError(getErrorMessage(err, 'Could not update save'));
    } finally {
      setBusy(false);
    }
  }

  if (sessionStatus === 'loading' || loading) {
    return <span className="text-sm text-gray-500">Loading…</span>;
  }

  if (sessionStatus !== 'authenticated') {
    return (
      <Link
        href={`/auth/signin?callbackUrl=/questions/${questionId}`}
        className="text-sm font-medium text-gray-600 hover:text-blue-700"
      >
        Sign in to save
      </Link>
    );
  }

  return (
    <div>
      <button
        type="button"
        onClick={toggleSave}
        disabled={busy}
        className={
          status?.saved
            ? 'rounded-md border border-gray-300 px-3 py-1.5 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50'
            : 'rounded-md border border-blue-600 px-3 py-1.5 text-sm font-medium text-blue-700 hover:bg-blue-50 disabled:opacity-50'
        }
      >
        {busy ? 'Saving…' : status?.saved ? 'Saved' : 'Save'}
      </button>
      {error && <p className="mt-1 text-xs text-red-600">{error}</p>}
    </div>
  );
}
