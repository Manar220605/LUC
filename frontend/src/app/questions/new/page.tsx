import { auth } from '@/lib/auth';
import { redirect } from 'next/navigation';
import NewQuestionForm from '@/components/question/NewQuestionForm';

export default async function NewQuestionPage() {
  const session = await auth();
  if (!session) {
    redirect('/auth/signin');
  }

  return (
    <main className="mx-auto max-w-4xl px-4 py-8">
      <h1 className="text-2xl font-semibold text-gray-900">Ask a question</h1>
      <p className="mt-2 text-sm text-gray-600">
        Choose a community and share your question with the LUC community.
      </p>
      <div className="mt-6">
        <NewQuestionForm />
      </div>
    </main>
  );
}
