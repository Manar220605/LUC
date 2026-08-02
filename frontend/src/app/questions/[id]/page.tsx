import Link from 'next/link';
import { apiPublicGet } from '@/lib/apiPublic';
import type { QuestionResponseDTO } from '@/lib/types';

type Props = {
  params: Promise<{ id: string }>;
};

export default async function QuestionPage({ params }: Props) {
  const { id } = await params;
  const question = await apiPublicGet<QuestionResponseDTO>(`/api/questions/${id}`);

  return (
    <main className="mx-auto max-w-4xl px-4 py-8">
      <p className="text-sm text-gray-500">
        <Link href="/" className="hover:text-blue-700">
          Feed
        </Link>
        {' / '}
        <Link href={`/c/${question.community.path}`} className="hover:text-blue-700">
          {question.community.name}
        </Link>
      </p>

      <h1 className="mt-3 text-3xl font-bold text-gray-900">{question.title}</h1>

      <div className="mt-2 flex flex-wrap gap-x-4 gap-y-1 text-sm text-gray-500">
        <span>{question.author.displayName}</span>
        <span>{new Date(question.createdAt).toLocaleString()}</span>
        <span>{question.viewCount} views</span>
        <span>{question.answerCount} answers</span>
        <span>{question.score} score</span>
      </div>

      <article className="mt-6 whitespace-pre-wrap rounded-lg border border-gray-200 bg-white p-6 text-gray-800">
        {question.body}
      </article>
    </main>
  );
}
