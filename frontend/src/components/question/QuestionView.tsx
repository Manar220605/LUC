'use client';

import Link from 'next/link';
import { useCallback, useState } from 'react';
import AnswerThread from '@/components/question/AnswerThread';
import type { QuestionResponseDTO } from '@/lib/types';

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080';

type Props = {
  question: QuestionResponseDTO;
  questionId: string;
};

export default function QuestionView({ question, questionId }: Props) {
  const [answerCount, setAnswerCount] = useState(question.answerCount);

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

      <h1 className="mt-3 text-3xl font-bold text-gray-900">{question.title}</h1>

      <div className="mt-2 flex flex-wrap gap-x-4 gap-y-1 text-sm text-gray-500">
        <span>{question.author.displayName}</span>
        <span>{new Date(question.createdAt).toLocaleString()}</span>
        <span>{question.viewCount} views</span>
        <span>{answerCount} answers</span>
        <span>{question.score} score</span>
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
