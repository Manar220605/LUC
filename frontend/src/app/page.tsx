import Link from 'next/link';
import { apiPublicGet } from '@/lib/apiPublic';
import FeedList from '@/components/question/FeedList';
import type { PageResponseDTO, QuestionSummaryDTO } from '@/lib/types';

const feedQuery = '?sort=NEW&page=0&size=20';

export default async function HomePage() {
  const feed = await apiPublicGet<PageResponseDTO<QuestionSummaryDTO>>(`/api/feed${feedQuery}`);

  return (
    <main className="mx-auto max-w-4xl px-4 py-8">
      <div className="flex items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Latest questions</h1>
          <p className="mt-2 text-sm text-gray-600">
            Browse the newest questions from across LUC communities.
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
        <h2 className="text-lg font-semibold text-gray-900">New</h2>
        <div className="mt-3">
          <FeedList items={feed.content} feedQuery={feedQuery} />
        </div>
      </section>
    </main>
  );
}
