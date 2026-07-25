'use client';

import Link from 'next/link';
import { signOut, useSession } from 'next-auth/react';
import { useState } from 'react';

export default function Header() {
  const { data: session, status } = useSession();
  const [open, setOpen] = useState(false);

  return (
    <header className="border-b border-gray-200 bg-white">
      <div className="mx-auto flex max-w-4xl items-center justify-between px-4 py-3">
        <Link href="/" className="text-lg font-semibold text-gray-900">
          Lebanese University Connect
        </Link>

        {status === 'loading' ? (
          <span className="text-sm text-gray-500">Loading…</span>
        ) : session?.user ? (
          <div className="relative">
            <button
              type="button"
              onClick={() => setOpen((value) => !value)}
              className="rounded-md border border-gray-300 px-3 py-1.5 text-sm font-medium text-gray-700 hover:bg-gray-50"
            >
              {session.user.name ?? session.user.email ?? 'Account'} ▾
            </button>
            {open && (
              <div className="absolute right-0 z-10 mt-2 w-44 rounded-md border border-gray-200 bg-white py-1 shadow-lg">
                <Link
                  href="/profile"
                  className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
                  onClick={() => setOpen(false)}
                >
                  Profile
                </Link>
                <button
                  type="button"
                  className="block w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-50"
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
        ) : (
          <Link
            href="/auth/signin"
            className="rounded-md bg-blue-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-blue-700"
          >
            Sign in
          </Link>
        )}
      </div>
    </header>
  );
}
