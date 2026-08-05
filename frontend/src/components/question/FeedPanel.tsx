'use client';

import { usePathname, useRouter, useSearchParams } from 'next/navigation';
import FeedList from '@/components/question/FeedList';
import type { QuestionSummaryDTO } from '@/lib/types';
import {
  FEED_SORTS,
  type FeedSort,
  buildFeedQuery,
  feedSortLabel,
  parseFeedSort,
} from '@/lib/feed';

type Props = {
  items: QuestionSummaryDTO[];
  sort: FeedSort;
  communityPath?: string;
  includeDescendants?: boolean;
  emptyMessage?: string;
};

function tabClassName(active: boolean): string {
  return active
    ? 'rounded-md bg-blue-600 px-3 py-1.5 text-sm font-medium text-white'
    : 'rounded-md px-3 py-1.5 text-sm font-medium text-gray-700 hover:bg-gray-100';
}

export default function FeedPanel({
  items,
  sort,
  communityPath,
  includeDescendants = true,
  emptyMessage,
}: Props) {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();

  const feedQuery = buildFeedQuery({
    sort,
    communityPath,
    includeDescendants: communityPath ? includeDescendants : undefined,
  });

  function navigate(next: { sort?: FeedSort; includeDescendants?: boolean }) {
    const params = new URLSearchParams(searchParams.toString());
    params.set('sort', next.sort ?? sort);
    if (communityPath) {
      params.set(
        'includeDescendants',
        String(next.includeDescendants ?? includeDescendants)
      );
    }
    router.push(`${pathname}?${params.toString()}`);
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex flex-wrap gap-2">
          {FEED_SORTS.map((option) => (
            <button
              key={option}
              type="button"
              onClick={() => navigate({ sort: option })}
              className={tabClassName(sort === option)}
            >
              {feedSortLabel(option)}
            </button>
          ))}
        </div>

        {communityPath && (
          <div className="flex flex-wrap gap-2 text-sm">
            <button
              type="button"
              onClick={() => navigate({ includeDescendants: true })}
              className={tabClassName(includeDescendants)}
            >
              Include subcommunities
            </button>
            <button
              type="button"
              onClick={() => navigate({ includeDescendants: false })}
              className={tabClassName(!includeDescendants)}
            >
              Exact community only
            </button>
          </div>
        )}
      </div>

      <FeedList items={items} feedQuery={feedQuery} emptyMessage={emptyMessage} />
    </div>
  );
}
