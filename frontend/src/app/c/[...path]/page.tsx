import { Suspense } from 'react';
import { apiPublicGet } from '@/lib/apiPublic';
import type {
  CommunityResponseDTO,
  PageResponseDTO,
  QuestionSummaryDTO,
} from '@/lib/types';
import CommunityBreadcrumb from '@/components/community/CommunityBreadcrumb';
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
      <h1 className="text-3xl font-bold text-gray-900">{community.name}</h1>
      {community.description && (
        <p className="mt-2 text-gray-600">{community.description}</p>
      )}
      <p className="mt-1 text-sm text-gray-500">Path: {community.path}</p>

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
