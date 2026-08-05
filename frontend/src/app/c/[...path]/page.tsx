import { Suspense } from 'react';
import { apiPublicGet } from '@/lib/apiPublic';
import type {
  CommunityResponseDTO,
  CommunityTreeNodeDTO,
  PageResponseDTO,
  QuestionSummaryDTO,
} from '@/lib/types';
import CommunityBreadcrumb from '@/components/community/CommunityBreadcrumb';
import CommunityTree from '@/components/community/CommunityTree';
import FeedPanel from '@/components/question/FeedPanel';
import { buildFeedQuery, parseFeedSort } from '@/lib/feed';

type Props = {
  params: Promise<{ path: string[] }>;
  searchParams: Promise<{ sort?: string; includeDescendants?: string }>;
};

export default async function CommunityPage({ params, searchParams }: Props) {
  const { path: segments } = await params;
  const query = await searchParams;
  const path = segments.join('/');
  const sort = parseFeedSort(query.sort);
  const includeDescendants = query.includeDescendants !== 'false';
  const feedQuery = buildFeedQuery({ sort, communityPath: path, includeDescendants });

  const [community, tree, feed] = await Promise.all([
    apiPublicGet<CommunityResponseDTO>(`/api/communities/by-path?path=${encodeURIComponent(path)}`),
    apiPublicGet<CommunityTreeNodeDTO[]>('/api/communities'),
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
        <h2 className="text-lg font-semibold text-gray-900">Browse communities</h2>
        <div className="mt-3 rounded-lg border border-gray-200 bg-white p-4">
          <CommunityTree nodes={tree} currentPath={path} />
        </div>
      </section>

      <section className="mt-8">
        <h2 className="text-lg font-semibold text-gray-900">Questions</h2>
        <div className="mt-3">
          <Suspense fallback={<p className="text-sm text-gray-500">Loading feed…</p>}>
            <FeedPanel
              items={feed.content}
              sort={sort}
              communityPath={path}
              includeDescendants={includeDescendants}
              emptyMessage="No questions in this community yet."
            />
          </Suspense>
        </div>
      </section>
    </main>
  );
}
