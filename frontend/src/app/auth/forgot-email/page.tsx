'use client';

import Link from 'next/link';
import { FormEvent, useState } from 'react';
import { apiPublicPost } from '@/lib/apiPublic';
import { getErrorMessage } from '@/lib/apiError';
import type { ForgotEmailResponseDTO } from '@/lib/types';

export default function ForgotEmailPage() {
  const [fileNumber, setFileNumber] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<ForgotEmailResponseDTO | null>(null);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const response = await apiPublicPost<ForgotEmailResponseDTO>('/api/register/forgot-email', {
        fileNumber: fileNumber.trim(),
      });
      setResult(response);
    } catch (err) {
      setError(getErrorMessage(err, 'Could not look up that file number'));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <main className="mx-auto max-w-lg px-4 py-12">
      <h1 className="text-3xl font-bold text-gray-900">Forgot email</h1>
      <p className="mt-2 text-sm text-gray-600">
        Enter your university file number. If it is in our records, we show a hidden version of the
        email you used in first year.
      </p>

      <form onSubmit={handleSubmit} className="mt-8 space-y-4">
        <div>
          <label htmlFor="fileNumber" className="block text-sm font-medium text-gray-700">
            Student file number
          </label>
          <input
            id="fileNumber"
            type="text"
            required
            value={fileNumber}
            onChange={(event) => setFileNumber(event.target.value)}
            placeholder="e.g. 2021005"
            className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
          />
        </div>
        {error && <p className="text-sm text-red-600">{error}</p>}
        <button
          type="submit"
          disabled={submitting}
          className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
        >
          {submitting ? 'Looking up…' : 'Find my email'}
        </button>
      </form>

      {result && (
        <div className="mt-8 rounded-lg border border-gray-200 bg-gray-50 p-4">
          <h2 className="text-lg font-semibold text-gray-900">Recovering your email</h2>
          <p className="mt-2 text-sm text-gray-600">{result.message}</p>
          {result.maskedEmail && (
            <p className="mt-4 text-center text-xl font-medium text-blue-800">{result.maskedEmail}</p>
          )}
        </div>
      )}

      <p className="mt-6 text-sm text-gray-600">
        <Link href="/auth/signin" className="font-medium text-blue-600 hover:text-blue-800">
          Sign in
        </Link>
        {' · '}
        <Link href="/auth/signup" className="font-medium text-blue-600 hover:text-blue-800">
          Student sign up
        </Link>
      </p>
    </main>
  );
}
