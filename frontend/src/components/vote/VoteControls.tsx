'use client';

import { useEffect, useState } from 'react';
import { getSession, signIn } from 'next-auth/react';
import { ApiError, getErrorMessage, SIGN_IN_REQUIRED } from '@/lib/apiError';
import type { VoteResponseDTO, VoteTargetType } from '@/lib/vote';
import { nextVoteState } from '@/lib/vote';

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080';

type Props = {
  targetType: VoteTargetType;
  targetId: number;
  score: number;
  viewerVote?: number | null;
  disabled?: boolean;
  layout?: 'vertical' | 'horizontal';
  onVoteChange?: (score: number, viewerVote: number | null) => void;
};

async function castVote(
  targetType: VoteTargetType,
  targetId: number,
  value: -1 | 1
): Promise<VoteResponseDTO> {
  const session = await getSession();
  if (session?.error === 'RefreshAccessTokenError' || !session?.accessToken) {
    throw new Error(SIGN_IN_REQUIRED);
  }

  const res = await fetch(`${API_URL}/api/votes`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${session.accessToken}`,
    },
    body: JSON.stringify({ targetType, targetId, value }),
  });

  if (!res.ok) {
    throw await ApiError.fromResponse(res);
  }

  return res.json() as Promise<VoteResponseDTO>;
}

export default function VoteControls({
  targetType,
  targetId,
  score: initialScore,
  viewerVote: initialViewerVote = null,
  disabled = false,
  layout = 'vertical',
  onVoteChange,
}: Props) {
  const [score, setScore] = useState(initialScore);
  const [viewerVote, setViewerVote] = useState<number | null>(initialViewerVote ?? null);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    setScore(initialScore);
    setViewerVote(initialViewerVote ?? null);
  }, [initialScore, initialViewerVote, targetId]);

  async function handleVote(value: -1 | 1) {
    if (disabled || submitting) {
      return;
    }

    setError(null);
    const previousScore = score;
    const previousVote = viewerVote;
    const optimistic = nextVoteState(viewerVote, value);

    setScore(previousScore + optimistic.scoreDelta);
    setViewerVote(optimistic.viewerVote);
    setSubmitting(true);

    try {
      const session = await getSession();
      if (!session?.accessToken || session.error === 'RefreshAccessTokenError') {
        setScore(previousScore);
        setViewerVote(previousVote);
        signIn('keycloak', { callbackUrl: window.location.href });
        return;
      }

      const response = await castVote(targetType, targetId, value);
      setScore(response.score);
      setViewerVote(response.viewerVote);
      onVoteChange?.(response.score, response.viewerVote);
    } catch (err) {
      setScore(previousScore);
      setViewerVote(previousVote);
      if (err instanceof Error && err.message === SIGN_IN_REQUIRED) {
        signIn('keycloak', { callbackUrl: window.location.href });
        return;
      }
      setError(getErrorMessage(err, 'Vote failed'));
    } finally {
      setSubmitting(false);
    }
  }

  const containerClass =
    layout === 'vertical'
      ? 'flex flex-col items-center gap-1'
      : 'flex items-center gap-2';

  return (
    <div className={containerClass}>
      <button
        type="button"
        aria-label="Upvote"
        disabled={disabled || submitting}
        onClick={() => handleVote(1)}
        className={`rounded px-2 py-1 text-sm font-semibold ${
          viewerVote === 1
            ? 'bg-orange-100 text-orange-700'
            : 'text-gray-500 hover:bg-gray-100 hover:text-orange-600'
        } disabled:cursor-not-allowed disabled:opacity-50`}
      >
        ↑
      </button>
      <span className="text-sm font-semibold text-gray-800">{score}</span>
      <button
        type="button"
        aria-label="Downvote"
        disabled={disabled || submitting}
        onClick={() => handleVote(-1)}
        className={`rounded px-2 py-1 text-sm font-semibold ${
          viewerVote === -1
            ? 'bg-blue-100 text-blue-700'
            : 'text-gray-500 hover:bg-gray-100 hover:text-blue-600'
        } disabled:cursor-not-allowed disabled:opacity-50`}
      >
        ↓
      </button>
      {error && <p className="max-w-[8rem] text-xs text-red-600">{error}</p>}
    </div>
  );
}
