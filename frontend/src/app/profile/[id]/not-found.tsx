import Link from 'next/link';

export default function PublicProfileNotFound() {
  return (
    <main className="mx-auto max-w-4xl px-4 py-8">
      <h1 className="text-2xl font-semibold text-lu-deep">Profile not found</h1>
      <p className="mt-2 text-sm text-muted">This user does not exist, or the link is invalid.</p>
      <p className="mt-4">
        <Link href="/" className="text-sm font-medium text-lu hover:text-lu-dark">
          Back to the feed
        </Link>
      </p>
    </main>
  );
}
