import Link from 'next/link';
import { notFound } from 'next/navigation';
import Avatar from '@/components/user/Avatar';
import AuthorBadge from '@/components/user/AuthorBadge';
import CourseQuestionList from '@/components/course/CourseQuestionList';
import MentorshipRequestPanel from '@/components/mentorship/MentorshipRequestPanel';
import { apiPublicGet } from '@/lib/apiPublic';
import { isApiError } from '@/lib/apiError';
import type { CourseDetailDTO } from '@/lib/types';
import { btnPrimary, card, heading, pageNarrow, sectionTitle } from '@/lib/ui';

type Props = {
  params: Promise<{ code: string }>;
};

export default async function CoursePage({ params }: Props) {
  const { code } = await params;
  let detail: CourseDetailDTO;
  try {
    detail = await apiPublicGet<CourseDetailDTO>(`/api/courses/${encodeURIComponent(code)}`);
  } catch (err) {
    if (isApiError(err, 404)) {
      notFound();
    }
    throw err;
  }

  const course = detail.course;
  const related = detail.relatedQuestions ?? [];

  return (
    <main className={pageNarrow}>
      <p className="text-sm text-muted">
        <Link href="/courses" className="text-lu hover:text-lu-dark">
          Courses
        </Link>
        {' / '}
        {course.code}
      </p>
      <div className="mt-2 flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 className={heading}>{course.title}</h1>
          <p className="mt-1 text-sm text-muted">
            {course.code} · Year {course.yearLevel} · Semester {course.semester} · {course.credits}{' '}
            credits
          </p>
          {detail.description && <p className="mt-3 text-ink">{detail.description}</p>}
          {detail.topics.length > 0 && (
            <p className="mt-3 flex flex-wrap gap-2">
              {detail.topics.map((topic) => (
                <span
                  key={topic.slug}
                  className="rounded-full bg-lu-soft px-2.5 py-0.5 text-xs font-medium text-lu-dark"
                >
                  {topic.name}
                </span>
              ))}
            </p>
          )}
        </div>
        <Link
          href={`/questions/new?community=cs&course=${encodeURIComponent(course.code)}`}
          className={btnPrimary}
        >
          Ask question
        </Link>
      </div>

      <section className="mt-8">
        <h2 className={sectionTitle}>Questions in this course</h2>
        <p className="mt-1 text-sm text-muted">Posts someone attached to {course.code}.</p>
        <div className="mt-3">
          <CourseQuestionList
            items={detail.linkedQuestions}
            emptyMessage="No questions are linked to this course yet."
          />
        </div>
      </section>

      {related.length > 0 && (
        <section className="mt-8">
          <h2 className={sectionTitle}>Related questions</h2>
          <p className="mt-1 text-sm text-muted">
            Questions LUC associates with this course from the text.
          </p>
          <div className="mt-3">
            <CourseQuestionList items={related} emptyMessage="No related questions yet." />
          </div>
        </section>
      )}

      <section className="mt-8">
        <h2 className={sectionTitle}>People who know this</h2>
        <p className="mt-1 text-sm text-muted">
          Ranked from good answers on topics this course covers.
        </p>
        {detail.people.length === 0 ? (
          <p className="mt-3 text-sm text-muted">
            Nobody is listed yet. Answers on tagged questions will fill this list.
          </p>
        ) : (
          <ul className={`${card} mt-3 divide-y divide-lu-soft overflow-hidden`}>
            {detail.people.map((person) => (
              <li key={person.author.id ?? person.author.displayName} className="px-4 py-4">
                <div className="flex items-start gap-3">
                  {person.author.id != null && (
                    <Avatar
                      name={person.author.displayName}
                      avatarUrl={person.author.avatarUrl}
                      size="sm"
                    />
                  )}
                  <div className="min-w-0 flex-1">
                    {person.author.id != null ? (
                      <Link
                        href={`/profile/${person.author.id}`}
                        className="font-medium text-lu-deep hover:text-lu"
                      >
                        {person.author.displayName}
                      </Link>
                    ) : (
                      <span className="font-medium text-lu-deep">{person.author.displayName}</span>
                    )}
                    <span className="ml-2 inline-flex align-middle">
                      <AuthorBadge author={person.author} />
                    </span>
                    {person.currentCompany && (
                      <p className="text-sm text-muted">{person.currentCompany}</p>
                    )}
                    {person.alumni && person.author.id != null && (
                      <MentorshipRequestPanel
                        alumniUserId={person.author.id}
                        alumniName={person.author.displayName}
                      />
                    )}
                  </div>
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>
    </main>
  );
}
