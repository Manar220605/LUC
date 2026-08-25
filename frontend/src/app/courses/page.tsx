import Link from 'next/link';
import { apiPublicGet } from '@/lib/apiPublic';
import type { CourseSummaryDTO } from '@/lib/types';

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
    <main className="mx-auto max-w-4xl px-4 py-8">
      <h1 className="text-3xl font-bold text-gray-900">Courses</h1>
      <p className="mt-2 text-sm text-gray-600">
        Lebanese University Faculty of Sciences Computer Science licence. Open a course to see
        questions about it and related posts LUC finds by meaning.
      </p>

      {years.length === 0 ? (
        <p className="mt-8 text-sm text-gray-500">No courses yet.</p>
      ) : (
        years.map((year) => (
          <section key={year} className="mt-8">
            <h2 className="text-lg font-semibold text-gray-900">Year {year}</h2>
            <ul className="mt-3 divide-y divide-gray-200 rounded-lg border border-gray-200 bg-white">
              {(byYear.get(year) ?? []).map((course) => (
                <li key={course.code}>
                  <Link
                    href={`/courses/${encodeURIComponent(course.code)}`}
                    className="flex flex-wrap items-baseline justify-between gap-2 px-4 py-3 hover:bg-gray-50"
                  >
                    <span>
                      <span className="font-mono text-sm text-blue-700">{course.code}</span>{' '}
                      <span className="font-medium text-gray-900">{course.title}</span>
                    </span>
                    <span className="text-xs text-gray-500">
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
