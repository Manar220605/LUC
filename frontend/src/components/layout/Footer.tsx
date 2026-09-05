import Link from 'next/link';

export default function Footer() {
  return (
    <footer className="mt-auto border-t border-white/10 bg-lu-deep text-white">
      <div className="mx-auto flex max-w-6xl flex-col gap-4 px-4 py-8 sm:flex-row sm:items-center sm:justify-between sm:px-6 lg:px-8">
        <div className="flex items-center gap-3">
          <img src="/lu-logo.jpg" alt="" className="h-10 w-10 rounded-md bg-white object-contain p-0.5" />
          <div>
            <p className="font-display text-sm font-semibold">Lebanese University Connect</p>
            <p className="text-xs text-white/70">Students and alumni of the Lebanese University</p>
          </div>
        </div>
        <nav className="flex flex-wrap gap-x-5 gap-y-2 text-sm text-white/80">
          <Link href="/" className="hover:text-white">
            Communities
          </Link>
          <Link href="/courses" className="hover:text-white">
            Courses
          </Link>
          <Link href="/alumni" className="hover:text-white">
            Alumni
          </Link>
          <Link href="/auth/signup" className="hover:text-white">
            Student sign up
          </Link>
          <Link href="/auth/alumni-signup" className="hover:text-white">
            Alumni sign up
          </Link>
        </nav>
      </div>
    </footer>
  );
}
