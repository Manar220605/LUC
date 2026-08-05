'use client';

import Link from 'next/link';
import { useCallback, useEffect, useRef, useState } from 'react';
import { getSession, useSession } from 'next-auth/react';
import AnswerThread from '@/components/question/AnswerThread';
import VoteControls from '@/components/vote/VoteControls';
import type { QuestionResponseDTO } from '@/lib/types';

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080';

type Props = {
  question: QuestionResponseDTO;
  questionId: string;
};

export default function QuestionView({ question: initialQuestion, questionId }: Props) {
  const { status } = useSession();
  const [question, setQuestion] = useState(initialQuestion);
  const [answerCount, setAnswerCount] = useState(initialQuestion.answerCount);
  const enrichedWithAuthRef = useRef(false);

  useEffect(() => {
    setQuestion(initialQuestion);
    setAnswerCount(initialQuestion.answerCount);
    enrichedWithAuthRef.current = false;
  }, [initialQuestion, questionId]);

  useEffect(() => {
    if (status !== 'authenticated' || enrichedWithAuthRef.current) {
      return;
    }

    let cancelled = false;

    async function enrichQuestionWithAuth() {
      const session = await getSession();
      if (!session?.accessToken || session.error === 'RefreshAccessTokenError') {
        return;
      }

      const res = await fetch(`${API_URL}/api/questions/${questionId}`, {
        headers: { Authorization: `Bearer ${session.accessToken}` },
        cache: 'no-store',
      });
      if (!res.ok || cancelled) {
        return;
      }

      const updated: QuestionResponseDTO = await res.json();
      enrichedWithAuthRef.current = true;
      setQuestion((current) => ({
        ...current,
        score: updated.score,
        viewerVote: updated.viewerVote ?? null,
        ownedByCurrentUser: updated.ownedByCurrentUser === true,
      }));
      setAnswerCount(updated.answerCount);
    }

    enrichQuestionWithAuth().catch(() => {
      // keep the public question if enrichment fails
    });

    return () => {
      cancelled = true;
    };
  }, [status, questionId]);

  const refreshAnswerCount = useCallback(async () => {
    const res = await fetch(`${API_URL}/api/questions/${questionId}`, {
      cache: 'no-store',
    });
    if (!res.ok) {
      return;
    }
    const updated: QuestionResponseDTO = await res.json();
    setAnswerCount(updated.answerCount);
  }, [questionId]);

  return (
    <main className="mx-auto max-w-4xl px-4 py-8">
      <p className="text-sm text-gray-500">
        <Link href="/" className="hover:text-blue-700">
          Feed
        </Link>
        {' / '}
        <Link href={`/c/${question.community.path}`} className="hover:text-blue-700">
          {question.community.name}
        </Link>
      </p>

      <div className="mt-3 flex items-start gap-4">
        <VoteControls
          targetType="QUESTION"
          targetId={question.id}
          score={question.score}
          viewerVote={question.viewerVote}
          disabled={question.ownedByCurrentUser === true}
          onVoteChange={(score, viewerVote) => {
            setQuestion((current) => ({ ...current, score, viewerVote }));
          }}
        />
        <div className="min-w-0 flex-1">
          <h1 className="text-3xl font-bold text-gray-900">{question.title}</h1>

          <div className="mt-2 flex flex-wrap gap-x-4 gap-y-1 text-sm text-gray-500">
            <span>{question.author.displayName}</span>
            <span>{new Date(question.createdAt).toLocaleString()}</span>
            <span>{question.viewCount} views</span>
            <span>{answerCount} answers</span>
          </div>
        </div>
      </div>

      <article className="mt-6 whitespace-pre-wrap rounded-lg border border-gray-200 bg-white p-6 text-gray-800">
        {question.body}
      </article>

      <AnswerThread
        questionId={questionId}
        answerCount={answerCount}
        onAnswersChanged={refreshAnswerCount}
      />
    </main>
  );
}
