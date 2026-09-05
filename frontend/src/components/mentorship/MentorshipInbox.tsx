'use client';

import { useCallback, useEffect, useState } from 'react';
import Link from 'next/link';
import { useSession } from 'next-auth/react';
import { useRouter } from 'next/navigation';
import Avatar from '@/components/user/Avatar';
import { clientApiRequest } from '@/lib/clientApi';
import { getErrorMessage } from '@/lib/apiError';
import { isSafeHttpUrl } from '@/lib/url';
import type { MentorshipInboxDTO, MentorshipRequestResponseDTO, MentorshipRequestStatus } from '@/lib/types';

function statusLabel(status: MentorshipRequestStatus): string {
  switch (status) {
    case 'PENDING':
      return 'Waiting';
    case 'ACCEPTED':
      return 'Accepted';
    case 'DECLINED':
      return 'Declined';
    default:
      return status;
  }
}

function RequestCard({
  request,
  side,
  onRespond,
  busyId,
}: {
  request: MentorshipRequestResponseDTO;
  side: 'incoming' | 'outgoing';
  onRespond: (id: number, action: 'accept' | 'decline') => void;
  busyId: number | null;
}) {
  const otherName = side === 'incoming' ? request.studentDisplayName : request.alumniDisplayName;
  const otherId = side === 'incoming' ? request.studentId : request.alumniId;
  const otherAvatar = side === 'incoming' ? request.studentAvatarUrl : request.alumniAvatarUrl;
  const pending = request.status === 'PENDING';
  const busy = busyId === request.id;

  return (
    <li className="px-4 py-4">
      <div className="flex items-start gap-3">
        <Avatar name={otherName} avatarUrl={otherAvatar} size="md" />
        <div className="min-w-0 flex-1">
          <div className="flex flex-wrap items-center gap-2">
            <Link
              href={`/profile/${otherId}`}
              className="font-semibold text-lu-deep hover:text-lu"
            >
              {otherName}
            </Link>
            <span className="rounded-full bg-lu-soft px-2 py-0.5 text-xs font-medium text-ink">
              {statusLabel(request.status)}
            </span>
          </div>
          <p className="mt-2 whitespace-pre-wrap text-sm text-ink">{request.message}</p>
          <p className="mt-1 text-xs text-muted">
            {new Date(request.createdAt).toLocaleString()}
          </p>
          {side === 'outgoing' &&
            request.status === 'ACCEPTED' &&
            isSafeHttpUrl(request.alumniLinkedinUrl) && (
              <p className="mt-2">
                <a
                  href={request.alumniLinkedinUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-sm font-medium text-lu hover:text-lu-dark"
                >
                  LinkedIn
                </a>
              </p>
            )}
          {side === 'incoming' && pending && (
            <div className="mt-3 flex gap-2">
              <button
                type="button"
                disabled={busy}
                onClick={() => onRespond(request.id, 'accept')}
                className="rounded-md bg-lu px-3 py-1.5 text-sm font-medium text-white hover:bg-lu-dark disabled:opacity-50"
              >
                Accept
              </button>
              <button
                type="button"
                disabled={busy}
                onClick={() => onRespond(request.id, 'decline')}
                className="rounded-md border border-lu/20 px-3 py-1.5 text-sm font-medium text-ink hover:bg-lu-soft disabled:opacity-50"
              >
                Decline
              </button>
            </div>
          )}
        </div>
      </div>
    </li>
  );
}

export default function MentorshipInbox() {
  const { status } = useSession();
  const router = useRouter();
  const [inbox, setInbox] = useState<MentorshipInboxDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [busyId, setBusyId] = useState<number | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await clientApiRequest<MentorshipInboxDTO>('/api/mentorship/requests');
      setInbox(result);
    } catch (err) {
      setError(getErrorMessage(err, 'Could not load mentorship requests'));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (status === 'unauthenticated') {
      router.replace('/auth/signin?callbackUrl=/mentorship');
      return;
    }
    if (status === 'authenticated') {
      load().catch(() => undefined);
    }
  }, [status, router, load]);

  async function handleRespond(id: number, action: 'accept' | 'decline') {
    setBusyId(id);
    setError(null);
    try {
      await clientApiRequest(`/api/mentorship/requests/${id}/${action}`, { method: 'POST' });
      await load();
    } catch (err) {
      setError(getErrorMessage(err, `Could not ${action} this request`));
    } finally {
      setBusyId(null);
    }
  }

  if (status === 'loading' || status === 'unauthenticated') {
    return (
      <main className="mx-auto max-w-3xl px-4 py-8">
        <p className="text-sm text-muted">Loading…</p>
      </main>
    );
  }

  const incoming = inbox?.incoming ?? [];
  const outgoing = inbox?.outgoing ?? [];
  const empty = incoming.length === 0 && outgoing.length === 0;

  return (
    <main className="mx-auto max-w-3xl px-4 py-8">
      <h1 className="text-3xl font-bold text-lu-deep">Mentorship</h1>
      <p className="mt-2 text-sm text-muted">
        Students can ask alumni for mentorship. Alumni accept or decline. There is no private chat —
        if you accept, the student can reach you on LinkedIn when you shared it.
      </p>

      {error && <p className="mt-4 text-sm text-red-600">{error}</p>}

      {loading ? (
        <p className="mt-6 text-sm text-muted">Loading requests…</p>
      ) : empty ? (
        <p className="mt-6 text-sm text-muted">
          No mentorship requests yet.{' '}
          <Link href="/alumni" className="font-medium text-lu hover:text-lu-dark">
            Browse alumni
          </Link>
          .
        </p>
      ) : (
        <>
          {incoming.length > 0 && (
            <section className="mt-8">
              <h2 className="text-lg font-semibold text-lu-deep">Requests you received</h2>
              <ul className="mt-3 divide-y divide-lu-soft rounded-lg border border-lu/10 bg-white">
                {incoming.map((request) => (
                  <RequestCard
                    key={request.id}
                    request={request}
                    side="incoming"
                    onRespond={handleRespond}
                    busyId={busyId}
                  />
                ))}
              </ul>
            </section>
          )}
          {outgoing.length > 0 && (
            <section className="mt-8">
              <h2 className="text-lg font-semibold text-lu-deep">Requests you sent</h2>
              <ul className="mt-3 divide-y divide-lu-soft rounded-lg border border-lu/10 bg-white">
                {outgoing.map((request) => (
                  <RequestCard
                    key={request.id}
                    request={request}
                    side="outgoing"
                    onRespond={handleRespond}
                    busyId={busyId}
                  />
                ))}
              </ul>
            </section>
          )}
        </>
      )}
    </main>
  );
}
