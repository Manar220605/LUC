import Link from 'next/link';
import type { QuestionSummaryDTO } from '@/lib/types';

type Props = {
  items: QuestionSummaryDTO[];
  emptyMessage?: string;
};

export default function FeedList({ items, emptyMessage = 'No questions yet.' }: Props) {
  if (items.length === 0) {
    return <p className="text-sm text-gray-500">{emptyMessage}</p>;
  }

  return (
    <ul className="divide-y divide-gray-200 rounded-lg border border-gray-200 bg-white">
      {items.map((item) => (
        <li key={item.id} className="px-4 py-4">
          <div className="flex items-start justify-between gap-4">
            <div className="min-w-0 flex-1">
              <Link
                href={`/questions/${item.id}`}
                className="text-base font-semibold text-gray-900 hover:text-blue-700"
              >
                {item.title}
              </Link>
              <p className="mt-1 text-sm text-gray-600">
                {item.author.displayName} in{' '}
                <Link href={`/c/${item.communityPath}`} className="hover:text-blue-700">
                  {item.communityName}
                </Link>
              </p>
              <p className="mt-1 text-xs text-gray-500">
                {new Date(item.createdAt).toLocaleString()}
              </p>
            </div>
            <div className="shrink-0 text-right text-xs text-gray-500">
              <div>{item.score} score</div>
              <div>{item.answerCount} answers</div>
              <div>{item.viewCount} views</div>
            </div>
          </div>
        </li>
      ))}
    </ul>
  );
}
