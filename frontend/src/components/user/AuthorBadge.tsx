import type { PublicAuthorDTO } from '@/lib/types';

type Props = {
  author: PublicAuthorDTO;
};

export default function AuthorBadge({ author }: Props) {
  if (author.displayName === 'Anonymous' || author.displayName === '[deleted]') {
    return null;
  }

  if (author.role === 'STUDENT') {
    return (
      <span className="inline-flex items-center rounded-full bg-lu-soft px-2 py-0.5 text-xs font-medium text-lu-dark">
        Student
      </span>
    );
  }

  if (author.role === 'ALUMNI') {
    return (
      <span className="inline-flex flex-wrap items-center gap-x-1 rounded-full bg-lu px-2 py-0.5 text-xs font-medium text-white">
        <span>Alumni</span>
        {author.gradYear != null && author.currentPosition && (
          <span className="font-normal text-white/85">
            · {author.gradYear} · {author.currentPosition}
          </span>
        )}
      </span>
    );
  }

  return null;
}
