import Link from 'next/link';
import { Suspense } from 'react';
import { apiPublicGet } from '@/lib/apiPublic';
import type {
  CommunityResponseDTO,
  PageResponseDTO,
  QuestionSummaryDTO,
} from '@/lib/types';
import CommunityBreadcrumb from '@/components/community/CommunityBreadcrumb';
import CommunityFollowButton from '@/components/community/CommunityFollowButton';
import FeedPanel from '@/components/question/FeedPanel';
import { buildFeedQuery, parseFeedSort } from '@/lib/feed';

type Props = {
  params: Promise<{ path: string[] }>;
  searchParams: Promise<{ sort?: string; includeDescendants?: string; search?: string }>;
};

export default async function CommunityPage({ params, searchParams }: Props) {
  const { path: segments } = await params;
  const query = await searchParams;
  const path = segments.join('/');
  const sort = parseFeedSort(query.sort);
  const includeDescendants = query.includeDescendants !== 'false';
  const search = query.search?.trim();
  const feedQuery = buildFeedQuery({ sort, communityPath: path, includeDescendants, search });

  const [community, feed] = await Promise.all([
    apiPublicGet<CommunityResponseDTO>(`/api/communities/by-path?path=${encodeURIComponent(path)}`),
    apiPublicGet<PageResponseDTO<QuestionSummaryDTO>>(`/api/feed${feedQuery}`),
  ]);

  return (
    <main className="mx-auto max-w-4xl px-4 py-8">
      <CommunityBreadcrumb path={path} />
      <div className="mt-2 flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">{community.name}</h1>
          {community.description && (
            <p className="mt-2 text-gray-600">{community.description}</p>
          )}
          <p className="mt-1 text-sm text-gray-500">Path: {community.path}</p>
        </div>
        <div className="flex shrink-0 flex-wrap items-start gap-2">
          <CommunityFollowButton communityPath={path} />
          <Link
            href={`/questions/new?community=${encodeURIComponent(path)}`}
            className="shrink-0 rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
          >
            Ask question
          </Link>
        </div>
      </div>

      <section className="mt-8">
        <h2 className="text-lg font-semibold text-gray-900">Questions</h2>
        <div className="mt-3">
          <Suspense fallback={<p className="text-sm text-gray-500">Loading feed…</p>}>
            <FeedPanel
              items={feed.content}
              sort={sort}
              communityPath={path}
              includeDescendants={includeDescendants}
              search={search}
              emptyMessage={
                search
                  ? `No questions match “${search}” in this community.`
                  : 'No questions in this community yet.'
              }
            />
          </Suspense>
        </div>
      </section>
    </main>
  );
}
