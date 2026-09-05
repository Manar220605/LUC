'use client';

import Link from 'next/link';
import { signOut, useSession } from 'next-auth/react';
import { useState } from 'react';
import QuestionSearchBox from '@/components/question/QuestionSearchBox';
import NotificationBell from '@/components/notifications/NotificationBell';

const NAV = [
  { href: '/', label: 'Communities' },
  { href: '/courses', label: 'Courses' },
  { href: '/alumni', label: 'Alumni' },
];

export default function Header() {
  const { data: session, status } = useSession();
  const [open, setOpen] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const isAdmin = session?.roles?.includes('ADMIN') ?? false;

  return (
    <header className="sticky top-0 z-40 bg-lu text-white shadow-md shadow-lu-deep/20">
      <div className="mx-auto flex max-w-6xl flex-wrap items-center gap-3 px-4 py-3 sm:px-6 lg:px-8">
        <Link href="/" className="flex shrink-0 items-center gap-2.5">
          <img
            src="/lu-logo.jpg"
            alt="Lebanese University"
            className="h-10 w-10 rounded-md bg-white object-contain p-0.5"
          />
          <span className="leading-tight">
            <span className="block font-display text-lg font-semibold tracking-tight">LUC</span>
            <span className="hidden text-[11px] font-medium text-white/80 sm:block">
              Lebanese University Connect
            </span>
          </span>
        </Link>

        <nav className="hidden items-center gap-1 md:flex">
          {NAV.map((item) => (
            <Link
              key={item.href}
              href={item.href}
              className="rounded-lg px-2.5 py-1.5 text-sm font-medium text-white/90 hover:bg-white/10 hover:text-white"
            >
              {item.label}
            </Link>
          ))}
          {session?.user && (
            <>
              <Link
                href="/following"
                className="rounded-lg px-2.5 py-1.5 text-sm font-medium text-white/90 hover:bg-white/10 hover:text-white"
              >
                Following
              </Link>
              <Link
                href="/saved"
                className="rounded-lg px-2.5 py-1.5 text-sm font-medium text-white/90 hover:bg-white/10 hover:text-white"
              >
                Saved
              </Link>
              <Link
                href="/mentorship"
                className="rounded-lg px-2.5 py-1.5 text-sm font-medium text-white/90 hover:bg-white/10 hover:text-white"
              >
                Mentorship
              </Link>
            </>
          )}
        </nav>

        <QuestionSearchBox
          action="global"
          variant="onBrand"
          className="min-w-[10rem] flex-1"
          placeholder="Search questions…"
        />

        <div className="ml-auto flex shrink-0 items-center gap-2">
          {status === 'loading' ? (
            <span className="text-sm text-white/70">Loading…</span>
          ) : session?.user ? (
            <>
              <NotificationBell />
              <div className="relative">
                <button
                  type="button"
                  onClick={() => setOpen((value) => !value)}
                  className="rounded-lg border border-white/25 bg-white/10 px-3 py-1.5 text-sm font-medium text-white hover:bg-white/20"
                >
                  {session.user.name ?? session.user.email ?? 'Account'} ▾
                </button>
                {open && (
                  <div className="absolute right-0 z-20 mt-2 w-48 overflow-hidden rounded-xl border border-lu/10 bg-white py-1 text-ink shadow-xl">
                    <Link
                      href="/profile"
                      className="block px-4 py-2 text-sm hover:bg-lu-soft"
                      onClick={() => setOpen(false)}
                    >
                      Profile
                    </Link>
                    {isAdmin && (
                      <Link
                        href="/admin/dashboard"
                        className="block px-4 py-2 text-sm font-semibold text-lu hover:bg-lu-soft"
                        onClick={() => setOpen(false)}
                      >
                        Admin panel
                      </Link>
                    )}
                    <button
                      type="button"
                      className="block w-full px-4 py-2 text-left text-sm hover:bg-lu-soft"
                      onClick={() => {
                        setOpen(false);
                        signOut({ callbackUrl: '/' });
                      }}
                    >
                      Sign out
                    </button>
                  </div>
                )}
              </div>
            </>
          ) : (
            <div className="hidden items-center gap-2 sm:flex">
              <Link
                href="/auth/signup"
                className="rounded-lg border border-white/30 px-3 py-1.5 text-sm font-medium text-white hover:bg-white/10"
              >
                Student sign up
              </Link>
              <Link
                href="/auth/alumni-signup"
                className="rounded-lg border border-white/30 px-3 py-1.5 text-sm font-medium text-white hover:bg-white/10"
              >
                Alumni sign up
              </Link>
              <Link
                href="/auth/signin"
                className="rounded-lg bg-white px-3 py-1.5 text-sm font-semibold text-lu hover:bg-lu-soft"
              >
                Sign in
              </Link>
            </div>
          )}

          <button
            type="button"
            className="rounded-lg border border-white/25 px-2.5 py-1.5 text-sm md:hidden"
            aria-label="Open menu"
            onClick={() => setMobileOpen((value) => !value)}
          >
            Menu
          </button>
        </div>
      </div>

      {mobileOpen && (
        <div className="border-t border-white/15 bg-lu-dark px-4 py-3 md:hidden">
          <nav className="flex flex-col gap-1">
            {NAV.map((item) => (
              <Link
                key={item.href}
                href={item.href}
                className="rounded-lg px-2 py-2 text-sm font-medium text-white hover:bg-white/10"
                onClick={() => setMobileOpen(false)}
              >
                {item.label}
              </Link>
            ))}
            {session?.user ? (
              <>
                <Link href="/following" className="rounded-lg px-2 py-2 text-sm" onClick={() => setMobileOpen(false)}>
                  Following
                </Link>
                <Link href="/saved" className="rounded-lg px-2 py-2 text-sm" onClick={() => setMobileOpen(false)}>
                  Saved
                </Link>
                <Link href="/mentorship" className="rounded-lg px-2 py-2 text-sm" onClick={() => setMobileOpen(false)}>
                  Mentorship
                </Link>
              </>
            ) : (
              <>
                <Link href="/auth/signup" className="rounded-lg px-2 py-2 text-sm" onClick={() => setMobileOpen(false)}>
                  Student sign up
                </Link>
                <Link href="/auth/alumni-signup" className="rounded-lg px-2 py-2 text-sm" onClick={() => setMobileOpen(false)}>
                  Alumni sign up
                </Link>
                <Link href="/auth/signin" className="rounded-lg px-2 py-2 text-sm font-semibold" onClick={() => setMobileOpen(false)}>
                  Sign in
                </Link>
              </>
            )}
          </nav>
        </div>
      )}
    </header>
  );
}
