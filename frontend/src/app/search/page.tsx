import { Suspense } from 'react';
import { apiPublicGet } from '@/lib/apiPublic';
import type { PageResponseDTO, QuestionSummaryDTO } from '@/lib/types';
import FeedPanel from '@/components/question/FeedPanel';
import QuestionSearchBox from '@/components/question/QuestionSearchBox';
import { buildFeedQuery, parseFeedSort } from '@/lib/feed';

type Props = {
  searchParams: Promise<{ q?: string; sort?: string }>;
};

export default async function SearchPage({ searchParams }: Props) {
  const query = await searchParams;
  const search = query.q?.trim() ?? '';
  const sort = parseFeedSort(query.sort);
  const hasQuery = search.length > 0;

  const feed = hasQuery
    ? await apiPublicGet<PageResponseDTO<QuestionSummaryDTO>>(
        `/api/feed${buildFeedQuery({ sort, search })}`
      )
    : { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 };

  return (
    <main className="mx-auto max-w-4xl px-4 py-8">
      <h1 className="text-3xl font-bold text-gray-900">Search questions</h1>
      <p className="mt-2 text-sm text-gray-600">
        Full-text search across question titles and bodies.
      </p>

      <div className="mt-6">
        <QuestionSearchBox initialQuery={search} action="global" className="max-w-xl" />
      </div>

      {hasQuery ? (
        <section className="mt-8">
          <p className="text-sm text-gray-600">
            {feed.totalElements === 0
              ? `No results for “${search}”.`
              : `${feed.totalElements} result${feed.totalElements === 1 ? '' : 's'} for “${search}”.`}
          </p>
          <div className="mt-4">
            <Suspense fallback={<p className="text-sm text-gray-500">Loading results…</p>}>
              <FeedPanel
                items={feed.content}
                sort={sort}
                search={search}
                showSearch={false}
                emptyMessage={`No questions match “${search}”.`}
              />
            </Suspense>
          </div>
        </section>
      ) : (
        <p className="mt-8 text-sm text-gray-500">Enter a search term to find questions.</p>
      )}
    </main>
  );
}
