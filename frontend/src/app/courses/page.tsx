import Link from 'next/link';
import { apiPublicGet } from '@/lib/apiPublic';
import type { CourseSummaryDTO } from '@/lib/types';
import { card, heading, pageNarrow, sectionTitle } from '@/lib/ui';

export default async function CoursesPage() {
  const courses = await apiPublicGet<CourseSummaryDTO[]>('/api/courses');
  const byYear = new Map<number, CourseSummaryDTO[]>();
  for (const course of courses) {
    const list = byYear.get(course.yearLevel) ?? [];
    list.push(course);
    byYear.set(course.yearLevel, list);
  }
  const years = [...byYear.keys()].sort((a, b) => a - b);

  return (
    <main className={pageNarrow}>
      <h1 className={heading}>Courses</h1>
      <p className="mt-2 text-sm text-muted">
        Lebanese University Faculty of Sciences Computer Science licence. Open a course to see
        questions about it.
      </p>

      {years.length === 0 ? (
        <p className="mt-8 text-sm text-muted">No courses yet.</p>
      ) : (
        years.map((year) => (
          <section key={year} className="mt-8">
            <h2 className={sectionTitle}>Year {year}</h2>
            <ul className={`${card} mt-3 divide-y divide-lu-soft overflow-hidden`}>
              {(byYear.get(year) ?? []).map((course) => (
                <li key={course.code}>
                  <Link
                    href={`/courses/${encodeURIComponent(course.code)}`}
                    className="flex flex-wrap items-baseline justify-between gap-2 px-4 py-3 transition hover:bg-lu-soft"
                  >
                    <span>
                      <span className="font-mono text-sm font-semibold text-lu">{course.code}</span>{' '}
                      <span className="font-medium text-lu-deep">{course.title}</span>
                    </span>
                    <span className="text-xs text-muted">
                      Semester {course.semester} · {course.credits} credits
                    </span>
                  </Link>
                </li>
              ))}
            </ul>
          </section>
        ))
      )}
    </main>
  );
}
