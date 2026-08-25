'use client';

import Link from 'next/link';
import { useEffect, useState } from 'react';
import { getSession } from 'next-auth/react';
import VoteControls from '@/components/vote/VoteControls';
import AuthorBadge from '@/components/user/AuthorBadge';
import AuthorName from '@/components/user/AuthorName';
import type { PageResponseDTO, QuestionSummaryDTO } from '@/lib/types';

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080';

type Props = {
  items: QuestionSummaryDTO[];
  feedQuery: string;
  emptyMessage?: string;
};

export default function FeedList({ items: initialItems, feedQuery, emptyMessage = 'No questions yet.' }: Props) {
  const [items, setItems] = useState(initialItems);

  useEffect(() => {
    setItems(initialItems);
  }, [initialItems]);

  useEffect(() => {
    let cancelled = false;

    async function enrichWithViewerVotes() {
      const session = await getSession();
      if (!session?.accessToken || session.error === 'RefreshAccessTokenError') {
        return;
      }

      const res = await fetch(`${API_URL}/api/feed${feedQuery}`, {
        headers: { Authorization: `Bearer ${session.accessToken}` },
        cache: 'no-store',
      });
      if (!res.ok || cancelled) {
        return;
      }

      const feed: PageResponseDTO<QuestionSummaryDTO> = await res.json();
      setItems(feed.content);
    }

    enrichWithViewerVotes().catch(() => {
      // keep the public feed if enrichment fails
    });

    return () => {
      cancelled = true;
    };
  }, [feedQuery]);

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
                {item.courseCode && (
                  <Link
                    href={`/courses/${encodeURIComponent(item.courseCode)}`}
                    className="text-xs font-medium text-blue-700 hover:text-blue-900"
                  >
                    {item.courseCode}
                  </Link>
                )}
              </p>
              <p className="mt-1 text-xs text-gray-500">
                {new Date(item.createdAt).toLocaleString()} · {item.answerCount} answers ·{' '}
                {item.viewCount} views
              </p>
            </div>
          </div>
        </li>
      ))}
    </ul>
  );
}
