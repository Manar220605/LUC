import QuestionView from '@/components/question/QuestionView';
import { apiPublicGet } from '@/lib/apiPublic';
import type { QuestionResponseDTO } from '@/lib/types';

type Props = {
  params: Promise<{ id: string }>;
};

export default async function QuestionPage({ params }: Props) {
  const { id } = await params;
  const question = await apiPublicGet<QuestionResponseDTO>(`/api/questions/${id}`);

  return <QuestionView question={question} questionId={id} />;
}
