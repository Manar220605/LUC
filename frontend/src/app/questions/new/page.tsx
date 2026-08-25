import { auth } from '@/lib/auth';
import { redirect } from 'next/navigation';
import NewQuestionForm from '@/components/question/NewQuestionForm';

type Props = {
  searchParams: Promise<{ community?: string }>;
};

export default async function NewQuestionPage({ searchParams }: Props) {
  const session = await auth();
  const query = await searchParams;
  const community = query.community?.trim();
  if (!session) {
    const next = community
      ? `/questions/new?community=${encodeURIComponent(community)}`
      : '/questions/new';
    redirect(`/auth/signin?callbackUrl=${encodeURIComponent(next)}`);
  }

  return (
    <main className="mx-auto max-w-4xl px-4 py-8">
      <h1 className="text-2xl font-semibold text-gray-900">Ask a question</h1>
      <p className="mt-2 text-sm text-gray-600">
        {community
          ? 'This question will be posted in the community you were viewing.'
          : 'Choose a community and share your question with the LUC community.'}
      </p>
      <div className="mt-6">
        <NewQuestionForm initialCommunityPath={community} />
      </div>
    </main>
  );
}
