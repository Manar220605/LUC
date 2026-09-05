import Link from 'next/link';
import { apiPublicGet } from '@/lib/apiPublic';
import type { AnnouncementResponseDTO, CommunityTreeNodeDTO } from '@/lib/types';
import { btnPrimary, btnSecondary, card, heading, pageWide } from '@/lib/ui';

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

  let announcements: AnnouncementResponseDTO[] = [];
  try {
    announcements = (
      await apiPublicGet<AnnouncementResponseDTO[]>('/api/announcements')
    ).slice(0, 3);
  } catch {
    announcements = [];
  }

  return (
    <>
      <section className="bg-lu text-white">
        <div className={`${pageWide} py-12 sm:py-16`}>
          <div className="grid items-center gap-10 lg:grid-cols-[1.2fr_0.8fr]">
            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.2em] text-white/70">
                Lebanese University
              </p>
              <h1 className="mt-3 font-display text-4xl font-semibold tracking-tight sm:text-5xl">
                Connect with students and alumni
              </h1>
              <p className="mt-4 max-w-xl text-base leading-7 text-white/85">
                Ask questions, share answers, and find mentors across Computer Science
                communities — the same blue and white as the university mark.
              </p>
              <div className="mt-8 flex flex-wrap gap-3">
                <Link href="/questions/new" className="inline-flex items-center justify-center rounded-lg bg-white px-4 py-2.5 text-sm font-semibold text-lu hover:bg-lu-soft">
                  Ask a question
                </Link>
                <Link href="/auth/signup" className="inline-flex items-center justify-center rounded-lg border border-white/35 px-4 py-2.5 text-sm font-semibold text-white hover:bg-white/10">
                  Student sign up
                </Link>
                <Link href="/auth/alumni-signup" className="inline-flex items-center justify-center rounded-lg border border-white/35 px-4 py-2.5 text-sm font-semibold text-white hover:bg-white/10">
                  Alumni sign up
                </Link>
                <Link href="/alumni" className="inline-flex items-center justify-center rounded-lg px-4 py-2.5 text-sm font-medium text-white/90 hover:bg-white/10">
                  Browse alumni
                </Link>
              </div>
            </div>
            <div className="hidden justify-end lg:flex">
              <div className="rounded-3xl bg-white p-5 shadow-2xl shadow-black/20">
                <img src="/lu-logo.jpg" alt="Lebanese University logo" className="h-56 w-56 object-contain" />
              </div>
            </div>
          </div>
        </div>
      </section>

      {announcements.length > 0 && (
        <section className="border-b border-lu/10 bg-lu-soft">
          <div className={`${pageWide} py-6`}>
            <h2 className="font-display text-lg font-semibold text-lu-deep">
              Announcements
            </h2>
            <ul className="mt-3 grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
              {announcements.map((a) => (
                <li
                  key={a.id}
                  className="rounded-xl border border-lu/15 bg-white p-4 shadow-sm"
                >
                  <p className="text-sm font-semibold text-lu-deep">{a.title}</p>
                  <p className="mt-1 line-clamp-3 whitespace-pre-wrap text-sm text-muted">
                    {a.body}
                  </p>
                  <p className="mt-2 text-xs text-muted">
                    {new Date(a.createdAt).toLocaleDateString()}
                  </p>
                </li>
              ))}
            </ul>
          </div>
        </section>
      )}

      <section className="border-b border-lu/10 bg-white">
        <div className={`${pageWide} grid gap-6 py-8 sm:grid-cols-3`}>
          {[
            { title: 'Communities', body: 'Browse CS topics from study abroad to jobs and courses.' },
            { title: 'Courses', body: 'Open a licence course and see questions asked about it.' },
            { title: 'Mentorship', body: 'Students can ask verified alumni for guidance.' },
          ].map((item) => (
            <div key={item.title}>
              <h2 className="font-display text-lg font-semibold text-lu-deep">{item.title}</h2>
              <p className="mt-1 text-sm leading-6 text-muted">{item.body}</p>
            </div>
          ))}
        </div>
      </section>

      <main className={pageWide}>
        <div className="flex flex-wrap items-end justify-between gap-4">
          <div>
            <h2 className={heading}>Communities</h2>
            <p className="mt-2 text-sm text-muted">
              Pick a community to browse its questions or ask a new one.
            </p>
          </div>
          <div className="flex gap-2">
            <Link href="/courses" className={btnSecondary}>
              View courses
            </Link>
            <Link href="/questions/new" className={btnPrimary}>
              Ask question
            </Link>
          </div>
        </div>

        {communities.length === 0 ? (
          <p className="mt-10 text-sm text-muted">No communities yet.</p>
        ) : (
          <ul className="mt-8 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {communities.map((c) => {
              const initial = c.name.charAt(0).toUpperCase();
              const subCount = c.children.length;
              return (
                <li key={c.path}>
                  <Link
                    href={`/c/${c.path}`}
                    className={`${card} group flex h-full flex-col p-5 transition hover:-translate-y-0.5 hover:border-lu/30 hover:shadow-lg`}
                  >
                    <div className="flex items-center gap-3">
                      <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-lu text-base font-semibold text-white">
                        {initial}
                      </div>
                      <div className="min-w-0">
                        <h3 className="truncate text-base font-semibold text-lu-deep group-hover:text-lu">
                          {c.name}
                        </h3>
                        <p className="truncate text-xs text-muted">c/{c.path}</p>
                      </div>
                    </div>

                    {c.description && (
                      <p className="mt-3 line-clamp-2 text-sm text-muted">{c.description}</p>
                    )}

                    <div className="mt-4 flex items-center gap-3 text-xs text-muted">
                      <span>
                        {c.questionCount} {c.questionCount === 1 ? 'question' : 'questions'}
                      </span>
                      {subCount > 0 && (
                        <>
                          <span aria-hidden>•</span>
                          <span>
                            {subCount} {subCount === 1 ? 'subcommunity' : 'subcommunities'}
                          </span>
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
    </>
  );
}
