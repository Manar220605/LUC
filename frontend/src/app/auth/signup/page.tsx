'use client';

import Link from 'next/link';
import { FormEvent, useState } from 'react';
import { apiPublicPost } from '@/lib/apiPublic';
import { getErrorMessage } from '@/lib/apiError';
import type { RegisterStudentResponseDTO } from '@/lib/types';

export default function StudentSignupPage() {
  const [fileNumber, setFileNumber] = useState('');
  const [email, setEmail] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<RegisterStudentResponseDTO | null>(null);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);

    try {
      const result = await apiPublicPost<RegisterStudentResponseDTO>('/api/register/student', {
        fileNumber: fileNumber.trim(),
        email: email.trim(),
      });
      setSuccess(result);
    } catch (err) {
      setError(getErrorMessage(err, 'Registration failed'));
    } finally {
      setSubmitting(false);
    }
  }

  if (success) {
    return (
      <main className="mx-auto max-w-lg px-4 py-12">
        <h1 className="text-3xl font-bold text-gray-900">Check your email</h1>
        <p className="mt-3 text-gray-700">{success.message}</p>
        <div className="mt-6 rounded-lg border border-green-200 bg-green-50 p-4 text-sm text-gray-800">
          <p className="font-medium text-green-900">{success.fullName}</p>
          <p className="mt-1">Enrollment year: {success.enrollmentYear}</p>
          <p>Faculty: {success.faculty.replaceAll('_', ' ')}</p>
          <p>Major: {success.major}</p>
        </div>
        <p className="mt-4 text-sm text-gray-600">
          We sent a temporary password to your university email. Use it to sign in, then change
          your password in Keycloak if prompted.
        </p>
        <Link
          href="/auth/signin"
          className="mt-6 inline-block rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
        >
          Go to sign in
        </Link>
      </main>
    );
  }

  return (
    <main className="mx-auto max-w-lg px-4 py-12">
      <h1 className="text-3xl font-bold text-gray-900">Student registration</h1>
      <p className="mt-2 text-sm text-gray-600">
        Register with your university file number and the email on file with LU. We will email
        you a login password — no admin approval required.
      </p>

      <form onSubmit={handleSubmit} className="mt-8 space-y-4">
        <div>
          <label htmlFor="fileNumber" className="block text-sm font-medium text-gray-700">
            University file number
          </label>
          <input
            id="fileNumber"
            type="text"
            required
            value={fileNumber}
            onChange={(event) => setFileNumber(event.target.value)}
            placeholder="e.g. 2024001"
            className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
          />
        </div>

        <div>
          <label htmlFor="email" className="block text-sm font-medium text-gray-700">
            University email
          </label>
          <input
            id="email"
            type="email"
            required
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            placeholder="you@lu.edu.lb"
            className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
          />
        </div>

        {error && <p className="text-sm text-red-600">{error}</p>}

        <button
          type="submit"
          disabled={submitting}
          className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
        >
          {submitting ? 'Registering…' : 'Register as student'}
        </button>
      </form>

      <p className="mt-6 text-sm text-gray-600">
        Already have an account?{' '}
        <Link href="/auth/signin" className="font-medium text-blue-600 hover:text-blue-800">
          Sign in
        </Link>
      </p>

      <details className="mt-8 rounded-md border border-gray-200 bg-gray-50 p-4 text-sm text-gray-700">
        <summary className="cursor-pointer font-medium text-gray-900">Dev test accounts</summary>
        <ul className="mt-3 space-y-2">
          <li>
            <strong>2024001</strong> — karim.student@lu.edu.lb (Engineering / CS)
          </li>
          <li>
            <strong>2024002</strong> — sara.student@lu.edu.lb (Sciences / Mathematics)
          </li>
          <li>
            <strong>2023015</strong> — omar.student@lu.edu.lb (Literature / English)
          </li>
        </ul>
        <p className="mt-2 text-xs text-gray-500">
          View sent emails at Mailhog: http://localhost:8025
        </p>
      </details>
    </main>
  );
}
