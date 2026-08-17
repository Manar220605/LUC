import QuestionView from '@/components/question/QuestionView';
import { auth } from '@/lib/auth';
import { ApiError } from '@/lib/apiError';
import type { QuestionResponseDTO } from '@/lib/types';

const API_URL =
  process.env.API_URL ??
  process.env.NEXT_PUBLIC_API_URL ??
  'http://localhost:8080';

type Props = {
  params: Promise<{ id: string }>;
};

async function fetchQuestion(id: string): Promise<QuestionResponseDTO> {
  const session = await auth();
  const headers: HeadersInit = {};
  if (session?.accessToken && session.error !== 'RefreshAccessTokenError') {
    headers.Authorization = `Bearer ${session.accessToken}`;
  }

  const res = await fetch(`${API_URL}/api/questions/${id}`, {
    headers,
    cache: 'no-store',
  });
  if (!res.ok) {
    throw await ApiError.fromResponse(res);
  }
  return res.json();
}

export default async function QuestionPage({ params }: Props) {
  const { id } = await params;
  const question = await fetchQuestion(id);

  return <QuestionView question={question} questionId={id} />;
}
