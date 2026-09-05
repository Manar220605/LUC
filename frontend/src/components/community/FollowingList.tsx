'use client';

import { useCallback, useEffect, useState } from 'react';
import Link from 'next/link';
import { useSession } from 'next-auth/react';
import { useRouter } from 'next/navigation';
import { clientApiRequest } from '@/lib/clientApi';
import { getErrorMessage } from '@/lib/apiError';
import type { CommunityFollowResponseDTO } from '@/lib/types';

export default function FollowingList() {
  const { status } = useSession();
  const router = useRouter();
  const [items, setItems] = useState<CommunityFollowResponseDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [busyPath, setBusyPath] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await clientApiRequest<CommunityFollowResponseDTO[]>('/api/follows');
      setItems(result);
    } catch (err) {
      setError(getErrorMessage(err, 'Could not load followed communities'));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (status === 'unauthenticated') {
      router.replace('/auth/signin?callbackUrl=/following');
      return;
    }
    if (status === 'authenticated') {
      load().catch(() => undefined);
    }
  }, [status, router, load]);

  async function unfollow(communityPath: string) {
    setBusyPath(communityPath);
    setError(null);
    try {
      await clientApiRequest(
        `/api/follows?communityPath=${encodeURIComponent(communityPath)}`,
        { method: 'DELETE' }
      );
      setItems((current) => current.filter((item) => item.community.path !== communityPath));
    } catch (err) {
      setError(getErrorMessage(err, 'Could not unfollow'));
    } finally {
      setBusyPath(null);
    }
  }

  if (status === 'loading' || status === 'unauthenticated') {
    return (
      <main className="mx-auto max-w-4xl px-4 py-8">
        <p className="text-sm text-muted">Loading…</p>
      </main>
    );
  }

  return (
    <main className="mx-auto max-w-4xl px-4 py-8">
      <h1 className="text-3xl font-bold text-lu-deep">Following</h1>
      <p className="mt-2 text-sm text-muted">
        Communities you follow. You get a notification when someone posts a new question there.
      </p>

      {error && <p className="mt-4 text-sm text-red-600">{error}</p>}

      {loading ? (
        <p className="mt-6 text-sm text-muted">Loading communities…</p>
      ) : items.length === 0 ? (
        <p className="mt-6 text-sm text-muted">
          You do not follow any communities yet. Open a community and click Follow.
        </p>
      ) : (
        <ul className="mt-6 divide-y divide-lu-soft rounded-lg border border-lu/10 bg-white">
          {items.map((item) => (
            <li key={item.community.id} className="flex flex-wrap items-center justify-between gap-3 px-4 py-4">
              <div className="min-w-0">
                <Link
                  href={`/c/${item.community.path}`}
                  className="font-semibold text-lu-deep hover:text-lu"
                >
                  {item.community.name}
                </Link>
                <p className="text-xs text-muted">c/{item.community.path}</p>
                <p className="mt-1 text-sm text-muted">
                  {item.community.questionCount}{' '}
                  {item.community.questionCount === 1 ? 'question' : 'questions'}
                </p>
              </div>
              <button
                type="button"
                disabled={busyPath === item.community.path}
                onClick={() => unfollow(item.community.path)}
                className="rounded-md border border-lu/20 px-3 py-1.5 text-sm font-medium text-ink hover:bg-lu-soft disabled:opacity-50"
              >
                {busyPath === item.community.path ? 'Saving…' : 'Unfollow'}
              </button>
            </li>
          ))}
        </ul>
      )}
    </main>
  );
}
