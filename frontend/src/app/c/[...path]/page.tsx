import { apiPublicGet } from '@/lib/apiPublic';
import type { CommunityResponseDTO, CommunityTreeNodeDTO } from '@/lib/types';
import CommunityBreadcrumb from '@/components/community/CommunityBreadcrumb';
import CommunityTree from '@/components/community/CommunityTree';

type Props = {
  params: Promise<{ path: string[] }>;
};

export default async function CommunityPage({ params }: Props) {
  const { path: segments } = await params;
  const path = segments.join('/');

  const [community, tree] = await Promise.all([
    apiPublicGet<CommunityResponseDTO>(`/api/communities/by-path?path=${encodeURIComponent(path)}`),
    apiPublicGet<CommunityTreeNodeDTO[]>('/api/communities'),
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
    </main>
  );
}
