'use client';

import { usePathname, useRouter, useSearchParams } from 'next/navigation';
import FeedList from '@/components/question/FeedList';
import QuestionSearchBox from '@/components/question/QuestionSearchBox';
import type { QuestionSummaryDTO } from '@/lib/types';
import { btnTab } from '@/lib/ui';
import {
  FEED_SORTS,
  type FeedSort,
  buildFeedQuery,
  feedSortLabel,
} from '@/lib/feed';

type Props = {
  items: QuestionSummaryDTO[];
  sort: FeedSort;
  communityPath?: string;
  includeDescendants?: boolean;
  search?: string;
  emptyMessage?: string;
  showSearch?: boolean;
};

function tabClassName(active: boolean): string {
  return btnTab(active);
}

export default function FeedPanel({
  items,
  sort,
  communityPath,
  includeDescendants = true,
  search,
  emptyMessage,
  showSearch = true,
}: Props) {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();

  const feedQuery = buildFeedQuery({
    sort,
    communityPath,
    includeDescendants: communityPath ? includeDescendants : undefined,
    search,
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
      {showSearch && (
        <QuestionSearchBox
          initialQuery={search ?? ''}
          action={communityPath ? 'community' : 'global'}
          placeholder={
            communityPath ? 'Search in this community…' : 'Refine search…'
          }
        />
      )}

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
