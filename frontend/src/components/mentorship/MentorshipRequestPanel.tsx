'use client';

import { FormEvent, useCallback, useEffect, useState } from 'react';
import Link from 'next/link';
import { useSession } from 'next-auth/react';
import { clientApiRequest } from '@/lib/clientApi';
import { getErrorMessage, isApiError } from '@/lib/apiError';
import { isSafeHttpUrl } from '@/lib/url';
import type { MentorshipStatusDTO } from '@/lib/types';

type Props = {
  alumniUserId: number;
  alumniName: string;
};

export default function MentorshipRequestPanel({ alumniUserId, alumniName }: Props) {
  const { status: sessionStatus } = useSession();
  const [status, setStatus] = useState<MentorshipStatusDTO | null>(null);
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [sent, setSent] = useState(false);

  const loadStatus = useCallback(async () => {
    if (sessionStatus !== 'authenticated') {
      setStatus(null);
      setLoading(false);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const result = await clientApiRequest<MentorshipStatusDTO>(
        `/api/mentorship/with/${alumniUserId}`
      );
      setStatus(result);
    } catch (err) {
      if (isApiError(err, 401)) {
        setStatus(null);
        return;
      }
      setError(getErrorMessage(err, 'Could not load mentorship status'));
    } finally {
      setLoading(false);
    }
  }, [alumniUserId, sessionStatus]);

  useEffect(() => {
    loadStatus().catch(() => undefined);
  }, [loadStatus]);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    const text = message.trim();
    if (text.length < 10) {
      setError('Write at least 10 characters so the alumnus knows why you are asking.');
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      await clientApiRequest('/api/mentorship/requests', {
        method: 'POST',
        body: JSON.stringify({ alumniUserId, message: text }),
      });
      setSent(true);
      setMessage('');
      await loadStatus();
    } catch (err) {
      setError(getErrorMessage(err, 'Could not send the request'));
    } finally {
      setSubmitting(false);
    }
  }

  if (sessionStatus === 'loading' || loading) {
    return (
      <section id="mentorship" className="mt-6 rounded-lg border border-gray-200 bg-white p-4">
        <p className="text-sm text-gray-500">Loading mentorship…</p>
      </section>
    );
  }

  if (sessionStatus !== 'authenticated') {
    return (
      <section id="mentorship" className="mt-6 rounded-lg border border-gray-200 bg-white p-4">
        <h2 className="text-sm font-semibold text-gray-900">Mentorship</h2>
        <p className="mt-1 text-sm text-gray-600">
          Students can ask {alumniName} to be a mentor.{' '}
          <Link
            href={`/auth/signin?callbackUrl=/profile/${alumniUserId}`}
            className="font-medium text-blue-700 hover:text-blue-800"
          >
            Sign in
          </Link>{' '}
          to send a request.
        </p>
      </section>
    );
  }

  if (
    status?.cannotRequestReason === 'SELF' ||
    status?.cannotRequestReason === 'STUDENT_ONLY' ||
    status?.cannotRequestReason === 'NOT_ALUMNI'
  ) {
    const reasonText =
      status.cannotRequestReason === 'SELF'
        ? 'This is your own profile.'
        : status.cannotRequestReason === 'STUDENT_ONLY'
          ? 'Only students can ask an alumnus for mentorship. Sign in with a student account to send a request.'
          : 'This person is not available for mentorship.';
    return (
      <section id="mentorship" className="mt-6 rounded-lg border border-gray-200 bg-white p-4">
        <h2 className="text-sm font-semibold text-gray-900">Mentorship</h2>
        <p className="mt-1 text-sm text-gray-600">{reasonText}</p>
      </section>
    );
  }

  const existing = status?.existing;
  const linkedin = existing?.alumniLinkedinUrl;

  return (
    <section id="mentorship" className="mt-6 rounded-lg border border-gray-200 bg-white p-4">
      <h2 className="text-sm font-semibold text-gray-900">Mentorship</h2>

      {error && <p className="mt-2 text-sm text-red-600">{error}</p>}
      {sent && status?.cannotRequestReason === 'PENDING' && (
        <p className="mt-2 text-sm text-green-700">Request sent. They will get a notification.</p>
      )}

      {status?.cannotRequestReason === 'PENDING' && (
        <p className="mt-2 text-sm text-gray-600">
          You already sent a request. Waiting for {alumniName} to reply.
        </p>
      )}

      {status?.cannotRequestReason === 'ACCEPTED' && (
        <div className="mt-2 text-sm text-gray-600">
          <p>{alumniName} accepted your mentorship request.</p>
          {isSafeHttpUrl(linkedin) ? (
            <p className="mt-1">
              You can reach them on{' '}
              <a
                href={linkedin}
                target="_blank"
                rel="noopener noreferrer"
                className="font-medium text-blue-700 hover:text-blue-800"
              >
                LinkedIn
              </a>
              .
            </p>
          ) : (
            <p className="mt-1">There is no private chat yet. Use LinkedIn if they shared it.</p>
          )}
        </div>
      )}

      {status?.cannotRequestReason === 'DECLINED' && (
        <p className="mt-2 text-sm text-gray-600">
          They declined last time. You can send a new request.
        </p>
      )}

      {status?.canRequest && (
        <form onSubmit={handleSubmit} className="mt-3">
          <label className="block text-sm">
            <span className="font-medium text-gray-700">Short message</span>
            <textarea
              value={message}
              onChange={(event) => setMessage(event.target.value)}
              rows={4}
              maxLength={500}
              placeholder="Say who you are and what you would like help with."
              className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
            />
          </label>
          <p className="mt-1 text-xs text-gray-500">{message.length}/500</p>
          <button
            type="submit"
            disabled={submitting}
            className="mt-3 rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
          >
            {submitting ? 'Sending…' : 'Ask for mentorship'}
          </button>
        </form>
      )}
    </section>
  );
}
