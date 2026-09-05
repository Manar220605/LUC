'use client';

import { useCallback, useEffect, useState } from 'react';
import Link from 'next/link';
import { useSession } from 'next-auth/react';
import { clientApiRequest } from '@/lib/clientApi';
import { getErrorMessage } from '@/lib/apiError';
import type { FollowStatusDTO } from '@/lib/types';

type Props = {
  communityPath: string;
};

export default function CommunityFollowButton({ communityPath }: Props) {
  const { status: sessionStatus } = useSession();
  const [status, setStatus] = useState<FollowStatusDTO | null>(null);
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
      const result = await clientApiRequest<FollowStatusDTO>(
        `/api/follows/status?communityPath=${encodeURIComponent(communityPath)}`
      );
      setStatus(result);
    } catch (err) {
      setError(getErrorMessage(err, 'Could not load follow status'));
    } finally {
      setLoading(false);
    }
  }, [communityPath, sessionStatus]);

  useEffect(() => {
    loadStatus().catch(() => undefined);
  }, [loadStatus]);

  async function toggleFollow() {
    if (!status) {
      return;
    }
    setBusy(true);
    setError(null);
    try {
      const result = status.following
        ? await clientApiRequest<FollowStatusDTO>(
            `/api/follows?communityPath=${encodeURIComponent(communityPath)}`,
            { method: 'DELETE' }
          )
        : await clientApiRequest<FollowStatusDTO>('/api/follows', {
            method: 'POST',
            body: JSON.stringify({ communityPath }),
          });
      setStatus(result);
    } catch (err) {
      setError(getErrorMessage(err, 'Could not update follow'));
    } finally {
      setBusy(false);
    }
  }

  if (sessionStatus === 'loading' || loading) {
    return <p className="text-sm text-muted">Loading…</p>;
  }

  if (sessionStatus !== 'authenticated') {
    return (
      <Link
        href={`/auth/signin?callbackUrl=/c/${communityPath}`}
        className="rounded-md border border-lu/20 px-4 py-2 text-sm font-medium text-ink hover:bg-lu-soft"
      >
        Sign in to follow
      </Link>
    );
  }

  return (
    <div>
      <button
        type="button"
        onClick={toggleFollow}
        disabled={busy}
        className={
          status?.following
            ? 'rounded-md border border-lu/20 px-4 py-2 text-sm font-medium text-ink hover:bg-lu-soft disabled:opacity-50'
            : 'rounded-md border border-lu px-4 py-2 text-sm font-medium text-lu hover:bg-lu-soft disabled:opacity-50'
        }
      >
        {busy ? 'Saving…' : status?.following ? 'Following' : 'Follow'}
      </button>
      {status != null && (
        <p className="mt-1 text-center text-xs text-muted">
          {status.followerCount} {status.followerCount === 1 ? 'follower' : 'followers'}
        </p>
      )}
      {error && <p className="mt-1 text-xs text-red-600">{error}</p>}
    </div>
  );
}
