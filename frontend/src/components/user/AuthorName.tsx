import Link from 'next/link';
import type { PublicAuthorDTO } from '@/lib/types';

type Props = {
  author: PublicAuthorDTO;
};

export default function AuthorName({ author }: Props) {
  const name = author.displayName;
  if (
    author.id == null ||
    name === 'Anonymous' ||
    name === '[deleted]'
  ) {
    return <span>{name}</span>;
  }

  return (
    <Link href={`/profile/${author.id}`} className="hover:text-blue-700">
      {name}
    </Link>
  );
}
