'use client';

import Link from 'next/link';
import { signIn } from 'next-auth/react';
import { useState } from 'react';
import { btnPrimary, btnSecondary, cardPad, errorText, heading, pageNarrow } from '@/lib/ui';

export default function AlumniSignupPage() {
  const [starting, setStarting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function startLinkedInSignup() {
    setError(null);
    setStarting(true);
    try {
      // Skip Keycloak username/password and go straight to the LinkedIn IdP.
      await signIn(
        'keycloak',
        { callbackUrl: '/alumni/verify' },
        { kc_idp_hint: 'linkedin' }
      );
    } catch {
      setStarting(false);
      setError('Could not start LinkedIn sign-up. Check that LinkedIn is configured in Keycloak.');
    }
  }

  return (
    <main className={pageNarrow}>
      <div className={cardPad}>
        <img src="/lu-logo.jpg" alt="" className="h-14 w-14 rounded-lg object-contain" />
        <h1 className={`${heading} mt-4`}>Alumni sign up</h1>
        <p className="mt-2 text-sm leading-6 text-muted">
          Create an account with LinkedIn. No university email or file number is required.
          After you sign in, you can submit your graduation details for verification.
        </p>

        <ol className="mt-6 list-decimal space-y-2 pl-5 text-sm text-ink">
          <li>Sign up with your LinkedIn account</li>
          <li>Submit your LinkedIn profile URL and graduation info</li>
          <li>An admin reviews and approves your alumni badge</li>
        </ol>

        {error && <p className={`${errorText} mt-4`}>{error}</p>}

        <button
          type="button"
          onClick={startLinkedInSignup}
          disabled={starting}
          className={`${btnPrimary} mt-8 w-full sm:w-auto`}
        >
          {starting ? 'Redirecting to LinkedIn…' : 'Continue with LinkedIn'}
        </button>

        <p className="mt-6 text-sm text-muted">
          Already have an account?{' '}
          <Link href="/auth/signin" className="font-medium text-lu hover:text-lu-dark">
            Sign in
          </Link>
        </p>
        <p className="mt-2 text-sm text-muted">
          Current student?{' '}
          <Link href="/auth/signup" className="font-medium text-lu hover:text-lu-dark">
            Sign up with your first-year email
          </Link>
        </p>

        <Link href="/alumni/verify" className={`${btnSecondary} mt-8`}>
          Already signed in? Open verification
        </Link>
      </div>
    </main>
  );
}
