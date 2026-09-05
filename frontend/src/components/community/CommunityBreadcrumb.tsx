import Link from 'next/link';

type Props = {
  path: string;
};

export default function CommunityBreadcrumb({ path }: Props) {
  const segments = path.split('/');
  let cumulative = '';

  return (
    <nav className="mb-4 text-sm text-muted">
      <Link href="/" className="hover:text-lu-deep">
        Home
      </Link>
      {segments.map((segment) => {
        cumulative = cumulative ? `${cumulative}/${segment}` : segment;
        const href = `/c/${cumulative.split('/').join('/')}`;
        return (
          <span key={cumulative}>
            {' / '}
            <Link href={href} className="hover:text-lu-deep">
              {segment.replace(/_/g, ' ')}
            </Link>
          </span>
        );
      })}
    </nav>
  );
}
