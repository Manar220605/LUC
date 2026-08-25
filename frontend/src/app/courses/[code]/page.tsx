import Link from 'next/link';
import { notFound } from 'next/navigation';
import Avatar from '@/components/user/Avatar';
import AuthorBadge from '@/components/user/AuthorBadge';
import CourseQuestionList from '@/components/course/CourseQuestionList';
import MentorshipRequestPanel from '@/components/mentorship/MentorshipRequestPanel';
import { apiPublicGet } from '@/lib/apiPublic';
import { isApiError } from '@/lib/apiError';
import type { CourseDetailDTO } from '@/lib/types';

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

  return (
    <main className="mx-auto max-w-4xl px-4 py-8">
      <p className="text-sm text-gray-500">
        <Link href="/courses" className="hover:text-blue-700">
          Courses
        </Link>
        {' / '}
        {course.code}
      </p>
      <div className="mt-2 flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">{course.title}</h1>
          <p className="mt-1 text-sm text-gray-600">
            {course.code} · Year {course.yearLevel} · Semester {course.semester} · {course.credits}{' '}
            credits
          </p>
          {detail.description && <p className="mt-3 text-gray-700">{detail.description}</p>}
          {detail.topics.length > 0 && (
            <p className="mt-3 flex flex-wrap gap-2">
              {detail.topics.map((topic) => (
                <span
                  key={topic.slug}
                  className="rounded-full bg-gray-100 px-2.5 py-0.5 text-xs font-medium text-gray-700"
                >
                  {topic.name}
                </span>
              ))}
            </p>
          )}
        </div>
        <Link
          href={`/questions/new?community=cs&course=${encodeURIComponent(course.code)}`}
          className="shrink-0 rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
        >
          Ask question
        </Link>
      </div>

      <section className="mt-8">
        <h2 className="text-lg font-semibold text-gray-900">Questions in this course</h2>
        <p className="mt-1 text-sm text-gray-600">Posts someone attached to {course.code}.</p>
        <div className="mt-3">
          <CourseQuestionList
            items={detail.linkedQuestions}
            emptyMessage="No questions are linked to this course yet. Ask one, or related questions may still appear below."
          />
        </div>
      </section>

      <section className="mt-8">
        <h2 className="text-lg font-semibold text-gray-900">Related questions</h2>
        <p className="mt-1 text-sm text-gray-600">
          LUC thinks these belong here from the text, even if nobody picked this course.
        </p>
        <div className="mt-3">
          <CourseQuestionList
            items={detail.relatedQuestions}
            emptyMessage="No related questions yet. They appear after the meaning index runs."
          />
        </div>
      </section>

      <section className="mt-8">
        <h2 className="text-lg font-semibold text-gray-900">People who know this</h2>
        <p className="mt-1 text-sm text-gray-600">
          Ranked from good answers on topics this course covers.
        </p>
        {detail.people.length === 0 ? (
          <p className="mt-3 text-sm text-gray-500">
            Nobody is listed yet. Answers on tagged questions will fill this list.
          </p>
        ) : (
          <ul className="mt-3 divide-y divide-gray-200 rounded-lg border border-gray-200 bg-white">
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
                        className="font-medium text-gray-900 hover:text-blue-700"
                      >
                        {person.author.displayName}
                      </Link>
                    ) : (
                      <span className="font-medium text-gray-900">{person.author.displayName}</span>
                    )}
                    <span className="ml-2 inline-flex align-middle">
                      <AuthorBadge author={person.author} />
                    </span>
                    {person.currentCompany && (
                      <p className="text-sm text-gray-600">{person.currentCompany}</p>
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
