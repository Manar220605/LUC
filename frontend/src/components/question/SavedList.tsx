'use client';

import { useCallback, useEffect, useState } from 'react';
import Link from 'next/link';
import { useSession } from 'next-auth/react';
import { useRouter } from 'next/navigation';
import { clientApiRequest } from '@/lib/clientApi';
import { getErrorMessage } from '@/lib/apiError';
import AuthorBadge from '@/components/user/AuthorBadge';
import AuthorName from '@/components/user/AuthorName';
import VoteControls from '@/components/vote/VoteControls';
import type { QuestionSaveResponseDTO, QuestionSummaryDTO } from '@/lib/types';

export default function SavedList() {
  const { status } = useSession();
  const router = useRouter();
  const [items, setItems] = useState<QuestionSaveResponseDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [busyId, setBusyId] = useState<number | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await clientApiRequest<QuestionSaveResponseDTO[]>('/api/saves');
      setItems(result);
    } catch (err) {
      setError(getErrorMessage(err, 'Could not load saved questions'));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (status === 'unauthenticated') {
      router.replace('/auth/signin?callbackUrl=/saved');
      return;
    }
    if (status === 'authenticated') {
      load().catch(() => undefined);
    }
  }, [status, router, load]);

  async function unsave(questionId: number) {
    setBusyId(questionId);
    setError(null);
    try {
      await clientApiRequest(`/api/saves?questionId=${questionId}`, { method: 'DELETE' });
      setItems((current) => current.filter((item) => item.question.id !== questionId));
    } catch (err) {
      setError(getErrorMessage(err, 'Could not unsave'));
    } finally {
      setBusyId(null);
    }
  }

  function updateQuestion(questionId: number, patch: Partial<QuestionSummaryDTO>) {
    setItems((current) =>
      current.map((item) =>
        item.question.id === questionId
          ? { ...item, question: { ...item.question, ...patch } }
          : item
      )
    );
  }

  if (status === 'loading' || status === 'unauthenticated') {
    return (
      <main className="mx-auto max-w-4xl px-4 py-8">
        <p className="text-sm text-gray-500">Loading…</p>
      </main>
    );
  }

  return (
    <main className="mx-auto max-w-4xl px-4 py-8">
      <h1 className="text-3xl font-bold text-gray-900">Saved</h1>
      <p className="mt-2 text-sm text-gray-600">
        Questions you saved for later. Only you can see this list.
      </p>

      {error && <p className="mt-4 text-sm text-red-600">{error}</p>}

      {loading ? (
        <p className="mt-6 text-sm text-gray-500">Loading saved questions…</p>
      ) : items.length === 0 ? (
        <p className="mt-6 text-sm text-gray-500">
          You have not saved any questions yet. Open a question and click Save.
        </p>
      ) : (
        <ul className="mt-6 divide-y divide-gray-200 rounded-lg border border-gray-200 bg-white">
          {items.map((item) => {
            const question = item.question;
            return (
              <li key={question.id} className="px-4 py-4">
                <div className="flex items-start gap-4">
                  <VoteControls
                    targetType="QUESTION"
                    targetId={question.id}
                    score={question.score}
                    viewerVote={question.viewerVote}
                    disabled={question.ownedByCurrentUser === true}
                    onVoteChange={(score, viewerVote) => {
                      updateQuestion(question.id, { score, viewerVote });
                    }}
                  />
                  <div className="min-w-0 flex-1">
                    <Link
                      href={`/questions/${question.id}`}
                      className="text-base font-semibold text-gray-900 hover:text-blue-700"
                    >
                      {question.title}
                    </Link>
                    <p className="mt-1 flex flex-wrap items-center gap-2 text-sm text-gray-600">
                      <AuthorName author={question.author} />
                      <AuthorBadge author={question.author} />
                      <span>
                        in{' '}
                        <Link href={`/c/${question.communityPath}`} className="hover:text-blue-700">
                          {question.communityName}
                        </Link>
                      </span>
                    </p>
                    <p className="mt-1 text-xs text-gray-500">
                      {new Date(question.createdAt).toLocaleString()} · {question.answerCount}{' '}
                      answers · {question.viewCount} views
                    </p>
                  </div>
                  <button
                    type="button"
                    disabled={busyId === question.id}
                    onClick={() => unsave(question.id)}
                    className="shrink-0 rounded-md border border-gray-300 px-3 py-1.5 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50"
                  >
                    {busyId === question.id ? 'Saving…' : 'Unsave'}
                  </button>
                </div>
              </li>
            );
          })}
        </ul>
      )}
    </main>
  );
}
