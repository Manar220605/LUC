import Link from 'next/link';
import Avatar from '@/components/user/Avatar';
import { apiPublicGet } from '@/lib/apiPublic';
import { FACULTIES, degreeLabel, facultyLabel } from '@/lib/alumni';
import { buildAlumniQuery } from '@/lib/alumniDirectory';
import { isSafeHttpUrl } from '@/lib/url';
import type { AlumniDirectoryEntryDTO, Faculty, PageResponseDTO } from '@/lib/types';

type Props = {
  searchParams: Promise<{
    q?: string;
    company?: string;
    gradYear?: string;
    faculty?: string;
    page?: string;
  }>;
};

export default async function AlumniDirectoryPage({ searchParams }: Props) {
  const query = await searchParams;
  const page = Math.max(Number(query.page) || 0, 0);
  const apiParams = new URLSearchParams();
  if (query.q?.trim()) apiParams.set('q', query.q.trim());
  if (query.company?.trim()) apiParams.set('company', query.company.trim());
  if (query.gradYear?.trim()) apiParams.set('gradYear', query.gradYear.trim());
  if (query.faculty?.trim()) apiParams.set('faculty', query.faculty.trim());
  apiParams.set('page', String(page));
  apiParams.set('size', '20');
  const result = await apiPublicGet<PageResponseDTO<AlumniDirectoryEntryDTO>>(
    `/api/alumni?${apiParams.toString()}`
  );

  const selectedFaculty = (query.faculty?.trim() ?? '') as Faculty | '';

  return (
    <main className="mx-auto max-w-3xl px-4 py-8 sm:px-6">
      <h1 className="font-display text-3xl font-semibold tracking-tight text-lu-deep sm:text-4xl">
        Alumni
      </h1>
      <p className="mt-2 text-sm text-muted">
        Find verified alumni who chose to show a public profile. Open a name to see their questions and
        answers. Students can ask an alumnus for mentorship.
      </p>
      <p className="mt-3 text-sm">
        <Link href="/auth/alumni-signup" className="font-medium text-lu hover:text-lu-dark">
          Are you an alumnus? Sign up with LinkedIn
        </Link>
      </p>

      <form method="get" action="/alumni" className="mt-6 grid gap-3 rounded-2xl border border-lu/10 bg-white p-5 shadow-[0_8px_30px_rgba(22,50,86,0.06)] sm:grid-cols-2">
        <label className="block text-sm">
          <span className="font-medium text-ink">Search</span>
          <input
            name="q"
            defaultValue={query.q ?? ''}
            placeholder="Name, job, company, or major"
            className="mt-1 w-full rounded-md border border-lu/20 px-3 py-2 text-sm"
          />
        </label>
        <label className="block text-sm">
          <span className="font-medium text-ink">Company</span>
          <input
            name="company"
            defaultValue={query.company ?? ''}
            placeholder="Company"
            className="mt-1 w-full rounded-md border border-lu/20 px-3 py-2 text-sm"
          />
        </label>
        <label className="block text-sm">
          <span className="font-medium text-ink">Graduation year</span>
          <input
            name="gradYear"
            type="number"
            min={1950}
            max={2100}
            defaultValue={query.gradYear ?? ''}
            placeholder="e.g. 2020"
            className="mt-1 w-full rounded-md border border-lu/20 px-3 py-2 text-sm"
          />
        </label>
        <label className="block text-sm">
          <span className="font-medium text-ink">Faculty</span>
          <select
            name="faculty"
            defaultValue={selectedFaculty}
            className="mt-1 w-full rounded-md border border-lu/20 px-3 py-2 text-sm"
          >
            <option value="">All faculties</option>
            {FACULTIES.map((faculty) => (
              <option key={faculty.value} value={faculty.value}>
                {faculty.label}
              </option>
            ))}
          </select>
        </label>
        <div className="flex items-end gap-2 sm:col-span-2">
          <button
            type="submit"
            className="rounded-md bg-lu px-4 py-2 text-sm font-medium text-white hover:bg-lu-dark"
          >
            Filter
          </button>
          <Link href="/alumni" className="px-3 py-2 text-sm font-medium text-muted hover:text-lu-deep">
            Clear
          </Link>
        </div>
      </form>

      <p className="mt-6 text-sm text-muted">
        {result.totalElements === 0
          ? 'No alumni match these filters.'
          : `${result.totalElements} alumn${result.totalElements === 1 ? 'us' : 'i'}`}
      </p>

      {result.content.length > 0 && (
        <ul className="mt-3 divide-y divide-lu-soft rounded-lg border border-lu/10 bg-white">
          {result.content.map((alumnus) => {
            const jobLine = [alumnus.currentPosition, alumnus.currentCompany]
              .filter(Boolean)
              .join(' at ');
            const studyLine = [
              degreeLabel(alumnus.degree),
              alumnus.major,
              facultyLabel(alumnus.faculty),
              alumnus.gradYear,
            ]
              .filter(Boolean)
              .join(' · ');

            return (
              <li key={alumnus.userId} className="px-4 py-4">
                <div className="flex items-start gap-3">
                  <Avatar name={alumnus.displayName} avatarUrl={alumnus.avatarUrl} size="md" />
                  <div className="min-w-0 flex-1">
                    <Link
                      href={`/profile/${alumnus.userId}`}
                      className="font-semibold text-lu-deep hover:text-lu"
                    >
                      {alumnus.displayName}
                    </Link>
                    {jobLine && <p className="mt-1 text-sm text-ink">{jobLine}</p>}
                    <p className="mt-1 text-sm text-muted">{studyLine}</p>
                    {isSafeHttpUrl(alumnus.linkedinUrl) && (
                      <p className="mt-1">
                        <a
                          href={alumnus.linkedinUrl}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="text-sm font-medium text-lu hover:text-lu-dark"
                        >
                          LinkedIn
                        </a>
                      </p>
                    )}
                    <p className="mt-2">
                      <Link
                        href={`/profile/${alumnus.userId}#mentorship`}
                        className="text-sm font-medium text-lu hover:text-lu-dark"
                      >
                        Ask for mentorship
                      </Link>
                    </p>
                  </div>
                </div>
              </li>
            );
          })}
        </ul>
      )}

      {result.totalPages > 1 && (
        <div className="mt-4 flex items-center gap-3">
          {page > 0 ? (
            <Link
              href={`/alumni${buildAlumniQuery(query, page - 1)}`}
              className="rounded-md border border-lu/20 px-3 py-1.5 text-sm text-ink hover:bg-lu-soft"
            >
              Previous
            </Link>
          ) : (
            <span className="rounded-md border border-lu/10 px-3 py-1.5 text-sm text-muted/70">
              Previous
            </span>
          )}
          <span className="text-sm text-muted">
            Page {page + 1} of {result.totalPages}
          </span>
          {page + 1 < result.totalPages ? (
            <Link
              href={`/alumni${buildAlumniQuery(query, page + 1)}`}
              className="rounded-md border border-lu/20 px-3 py-1.5 text-sm text-ink hover:bg-lu-soft"
            >
              Next
            </Link>
          ) : (
            <span className="rounded-md border border-lu/10 px-3 py-1.5 text-sm text-muted/70">
              Next
            </span>
          )}
        </div>
      )}
    </main>
  );
}
