import Link from 'next/link';
import { Suspense } from 'react';
import { apiPublicGet } from '@/lib/apiPublic';
import FeedPanel from '@/components/question/FeedPanel';
import { buildFeedQuery, parseFeedSort } from '@/lib/feed';
import type { PageResponseDTO, QuestionSummaryDTO } from '@/lib/types';

type Props = {
  searchParams: Promise<{ sort?: string }>;
};

export default async function HomePage({ searchParams }: Props) {
  const params = await searchParams;
  const sort = parseFeedSort(params.sort);
  const feedQuery = buildFeedQuery({ sort });
  const feed = await apiPublicGet<PageResponseDTO<QuestionSummaryDTO>>(`/api/feed${feedQuery}`);

  return (
    <main className="mx-auto max-w-4xl px-4 py-8">
      <div className="flex items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Latest questions</h1>
          <p className="mt-2 text-sm text-gray-600">
            Browse questions from across LUC communities.
          </p>
        </div>
        <Link
          href="/questions/new"
          className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
        >
          Ask question
        </Link>
      </div>

      <section className="mt-8">
        <Suspense fallback={<p className="text-sm text-gray-500">Loading feed…</p>}>
          <FeedPanel items={feed.content} sort={sort} />
        </Suspense>
      </section>
    </main>
  );
}
