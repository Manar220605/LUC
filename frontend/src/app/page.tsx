import Link from 'next/link';
import { apiPublicGet } from '@/lib/apiPublic';
import type { CommunityTreeNodeDTO } from '@/lib/types';

const AVATAR_PALETTE = [
  'bg-blue-100 text-blue-700',
  'bg-emerald-100 text-emerald-700',
  'bg-amber-100 text-amber-700',
  'bg-rose-100 text-rose-700',
  'bg-violet-100 text-violet-700',
  'bg-sky-100 text-sky-700',
  'bg-teal-100 text-teal-700',
  'bg-fuchsia-100 text-fuchsia-700',
];

function avatarClasses(path: string): string {
  let hash = 0;
  for (let i = 0; i < path.length; i += 1) {
    hash = (hash * 31 + path.charCodeAt(i)) >>> 0;
  }
  return AVATAR_PALETTE[hash % AVATAR_PALETTE.length];
}

function flatten(nodes: CommunityTreeNodeDTO[]): CommunityTreeNodeDTO[] {
  const out: CommunityTreeNodeDTO[] = [];
  const walk = (list: CommunityTreeNodeDTO[]) => {
    for (const node of list) {
      out.push(node);
      if (node.children.length > 0) walk(node.children);
    }
  };
  walk(nodes);
  return out;
}

export default async function HomePage() {
  const tree = await apiPublicGet<CommunityTreeNodeDTO[]>('/api/communities');
  const communities = flatten(tree).sort((a, b) => a.path.localeCompare(b.path));

  return (
    <main className="mx-auto max-w-6xl px-4 py-10">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Communities</h1>
          <p className="mt-2 text-sm text-gray-600">
            Pick a community to browse its questions or ask a new one.
          </p>
        </div>
        <Link
          href="/questions/new"
          className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
        >
          Ask question
        </Link>
      </div>

      {communities.length === 0 ? (
        <p className="mt-10 text-sm text-gray-500">No communities yet.</p>
      ) : (
        <ul className="mt-8 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {communities.map((c) => {
            const initial = c.name.charAt(0).toUpperCase();
            const subCount = c.children.length;
            return (
              <li key={c.path}>
                <Link
                  href={`/c/${c.path}`}
                  className="group flex h-full flex-col rounded-xl border border-gray-200 bg-white p-5 transition hover:border-blue-400 hover:shadow-md"
                >
                  <div className="flex items-center gap-3">
                    <div
                      className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-full text-base font-semibold ${avatarClasses(c.path)}`}
                    >
                      {initial}
                    </div>
                    <div className="min-w-0">
                      <h2 className="truncate text-base font-semibold text-gray-900 group-hover:text-blue-700">
                        {c.name}
                      </h2>
                      <p className="truncate text-xs text-gray-500">c/{c.path}</p>
                    </div>
                  </div>

                  {c.description && (
                    <p className="mt-3 line-clamp-2 text-sm text-gray-600">{c.description}</p>
                  )}

                  <div className="mt-4 flex items-center gap-3 text-xs text-gray-500">
                    <span>{c.questionCount} {c.questionCount === 1 ? 'question' : 'questions'}</span>
                    {subCount > 0 && (
                      <>
                        <span aria-hidden>•</span>
                        <span>{subCount} {subCount === 1 ? 'subcommunity' : 'subcommunities'}</span>
                      </>
                    )}
                  </div>
                </Link>
              </li>
            );
          })}
        </ul>
      )}
    </main>
  );
}
