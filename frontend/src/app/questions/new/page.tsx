import { auth } from '@/lib/auth';
import { redirect } from 'next/navigation';
import NewQuestionForm from '@/components/question/NewQuestionForm';
import { cardPad, heading, pageNarrow } from '@/lib/ui';

type Props = {
  searchParams: Promise<{ community?: string; course?: string }>;
};

export default async function NewQuestionPage({ searchParams }: Props) {
  const session = await auth();
  const query = await searchParams;
  const community = query.community?.trim();
  const course = query.course?.trim();
  if (!session) {
    const params = new URLSearchParams();
    if (community) params.set('community', community);
    if (course) params.set('course', course);
    const next = params.toString() ? `/questions/new?${params}` : '/questions/new';
    redirect(`/auth/signin?callbackUrl=${encodeURIComponent(next)}`);
  }

  return (
    <main className={pageNarrow}>
      <div className={cardPad}>
        <h1 className={heading}>Ask a question</h1>
        <p className="mt-2 text-sm text-muted">
          {course
            ? `This question will be linked to course ${course}.`
            : community
              ? 'This question will be posted in the community you were viewing.'
              : 'Choose a community and share your question with the LUC community.'}
        </p>
        <div className="mt-6">
          <NewQuestionForm initialCommunityPath={community} initialCourseCode={course} />
        </div>
      </div>
    </main>
  );
}
