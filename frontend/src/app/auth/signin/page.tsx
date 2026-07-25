'use client';

import { signIn } from 'next-auth/react';
import { useEffect } from 'react';

export default function SignInPage() {
  useEffect(() => {
    signIn('keycloak', { callbackUrl: '/' });
  }, []);

  return (
    <main className="mx-auto max-w-4xl px-4 py-16 text-center">
      <h1 className="text-2xl font-semibold text-gray-900">Redirecting to sign in…</h1>
      <p className="mt-2 text-gray-600">If you are not redirected, use the button below.</p>
      <button
        type="button"
        onClick={() => signIn('keycloak', { callbackUrl: '/' })}
        className="mt-6 rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
      >
        Sign in with Keycloak
      </button>
    </main>
  );
}
