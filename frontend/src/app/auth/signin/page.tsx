'use client';

import Link from 'next/link';
import { signIn } from 'next-auth/react';
import { useEffect } from 'react';
import { btnPrimary, cardPad, heading, pageNarrow } from '@/lib/ui';

export default function SignInPage() {
  useEffect(() => {
    signIn('keycloak', { callbackUrl: '/' });
  }, []);

  return (
    <main className={pageNarrow}>
      <div className={`${cardPad} text-center`}>
        <img src="/lu-logo.jpg" alt="" className="mx-auto h-16 w-16 rounded-lg object-contain" />
        <h1 className={`${heading} mt-4`}>Redirecting to sign in…</h1>
        <p className="mt-2 text-muted">If you are not redirected, use the button below.</p>
        <div className="mt-4 space-y-2 text-sm text-muted">
          <p>
            New student?{' '}
            <Link href="/auth/signup" className="font-medium text-lu hover:text-lu-dark">
              Sign up with your first-year email
            </Link>
          </p>
          <p>
            Alumni?{' '}
            <Link href="/auth/alumni-signup" className="font-medium text-lu hover:text-lu-dark">
              Sign up with LinkedIn
            </Link>
          </p>
          <p>
            Forgot which email you used?{' '}
            <Link href="/auth/forgot-email" className="font-medium text-lu hover:text-lu-dark">
              Find it with your file number
            </Link>
          </p>
        </div>
        <button
          type="button"
          onClick={() => signIn('keycloak', { callbackUrl: '/' })}
          className={`${btnPrimary} mt-6`}
        >
          Sign in
        </button>
      </div>
    </main>
  );
}
