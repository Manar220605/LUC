'use client';

import Link from 'next/link';
import { useEffect, useState } from 'react';
import { getSession } from 'next-auth/react';
import VoteControls from '@/components/vote/VoteControls';
import AuthorBadge from '@/components/user/AuthorBadge';
import AuthorName from '@/components/user/AuthorName';
import type { QuestionSummaryDTO } from '@/lib/types';

type Props = {
  items: QuestionSummaryDTO[];
  emptyMessage: string;
};

export default function CourseQuestionList({ items: initialItems, emptyMessage }: Props) {
  const [items, setItems] = useState(initialItems);

  useEffect(() => {
    setItems(initialItems);
  }, [initialItems]);

  useEffect(() => {
    getSession().catch(() => undefined);
  }, []);

  if (items.length === 0) {
    return <p className="text-sm text-gray-500">{emptyMessage}</p>;
  }

  return (
    <ul className="divide-y divide-gray-200 rounded-lg border border-gray-200 bg-white">
      {items.map((item) => (
        <li key={item.id} className="px-4 py-4">
          <div className="flex items-start gap-4">
            <VoteControls
              targetType="QUESTION"
              targetId={item.id}
              score={item.score}
              viewerVote={item.viewerVote}
              disabled={item.ownedByCurrentUser === true}
              onVoteChange={(score, viewerVote) => {
                setItems((current) =>
                  current.map((entry) =>
                    entry.id === item.id ? { ...entry, score, viewerVote } : entry
                  )
                );
              }}
            />
            <div className="min-w-0 flex-1">
              <Link
                href={`/questions/${item.id}`}
                className="text-base font-semibold text-gray-900 hover:text-blue-700"
              >
                {item.title}
              </Link>
              <p className="mt-1 flex flex-wrap items-center gap-2 text-sm text-gray-600">
                <AuthorName author={item.author} />
                <AuthorBadge author={item.author} />
                <span>
                  in{' '}
                  <Link href={`/c/${item.communityPath}`} className="hover:text-blue-700">
                    {item.communityName}
                  </Link>
                </span>
              </p>
              <p className="mt-1 text-xs text-gray-500">
                {new Date(item.createdAt).toLocaleString()} · {item.answerCount} answers
              </p>
            </div>
          </div>
        </li>
      ))}
    </ul>
  );
}
