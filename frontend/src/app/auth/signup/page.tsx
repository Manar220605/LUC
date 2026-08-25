'use client';

import Link from 'next/link';
import { FormEvent, useState } from 'react';
import { apiPublicPost } from '@/lib/apiPublic';
import { getErrorMessage } from '@/lib/apiError';
import type { LookupStudentResponseDTO, RegisterStudentResponseDTO } from '@/lib/types';

type Step = 'email' | 'activate';

export default function StudentSignupPage() {
  const [step, setStep] = useState<Step>('email');
  const [email, setEmail] = useState('');
  const [code, setCode] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [lookupMessage, setLookupMessage] = useState<string | null>(null);
  const [success, setSuccess] = useState<RegisterStudentResponseDTO | null>(null);

  async function handleLookup(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const result = await apiPublicPost<LookupStudentResponseDTO>('/api/register/student/lookup', {
        email: email.trim(),
      });
      setLookupMessage(result.message);
      setStep('activate');
    } catch (err) {
      setError(getErrorMessage(err, 'Could not start registration'));
    } finally {
      setSubmitting(false);
    }
  }

  async function handleActivate(event: FormEvent) {
    event.preventDefault();
    setError(null);
    if (password !== confirmPassword) {
      setError('Password and confirmation do not match');
      return;
    }
    setSubmitting(true);
    try {
      const result = await apiPublicPost<RegisterStudentResponseDTO>('/api/register/student', {
        email: email.trim(),
        code: code.trim().toUpperCase(),
        password,
        confirmPassword,
      });
      setSuccess(result);
    } catch (err) {
      setError(getErrorMessage(err, 'Could not create the account'));
    } finally {
      setSubmitting(false);
    }
  }

  if (success) {
    return (
      <main className="mx-auto max-w-lg px-4 py-12">
        <h1 className="text-3xl font-bold text-gray-900">Account ready</h1>
        <p className="mt-3 text-gray-700">{success.message}</p>
        <div className="mt-6 rounded-lg border border-green-200 bg-green-50 p-4 text-sm text-gray-800">
          <p className="font-medium text-green-900">{success.fullName}</p>
          <p className="mt-1">Enrollment year: {success.enrollmentYear}</p>
          <p>Faculty: {success.faculty.replaceAll('_', ' ')}</p>
          <p>Major: {success.major}</p>
        </div>
        <Link
          href="/auth/signin"
          className="mt-6 inline-block rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
        >
          Sign in
        </Link>
      </main>
    );
  }

  return (
    <main className="mx-auto max-w-lg px-4 py-12">
      <h1 className="text-3xl font-bold text-gray-900">Student registration</h1>
      <p className="mt-2 text-sm text-gray-600">
        Use the personal email you registered with in first year. We will email a code, then you set
        your password. Your file number and faculty are filled from university records.
      </p>

      {step === 'email' && (
        <form onSubmit={handleLookup} className="mt-8 space-y-4">
          <div>
            <label htmlFor="email" className="block text-sm font-medium text-gray-700">
              Email (provided during first-year registration)
            </label>
            <input
              id="email"
              type="email"
              required
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="you@gmail.com"
              className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
            />
          </div>
          {error && <p className="text-sm text-red-600">{error}</p>}
          <button
            type="submit"
            disabled={submitting}
            className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
          >
            {submitting ? 'Sending…' : 'Send code'}
          </button>
        </form>
      )}

      {step === 'activate' && (
        <form onSubmit={handleActivate} className="mt-8 space-y-4">
          <p className="text-sm text-gray-700">{lookupMessage}</p>
          <p className="text-sm text-gray-500">
            With mock data, open Mailhog at{' '}
            <a className="text-blue-600 hover:text-blue-800" href="http://localhost:8025">
              http://localhost:8025
            </a>
            . If the CSV row uses your real Gmail and Mailgun is configured, check that inbox.
          </p>
          <div>
            <label htmlFor="code" className="block text-sm font-medium text-gray-700">
              6-character code
            </label>
            <input
              id="code"
              type="text"
              required
              value={code}
              onChange={(event) => setCode(event.target.value.toUpperCase())}
              placeholder="ABC123"
              className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm tracking-widest"
            />
          </div>
          <div>
            <label htmlFor="password" className="block text-sm font-medium text-gray-700">
              Password
            </label>
            <input
              id="password"
              type="password"
              required
              minLength={8}
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
            />
          </div>
          <div>
            <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700">
              Confirm password
            </label>
            <input
              id="confirmPassword"
              type="password"
              required
              minLength={8}
              value={confirmPassword}
              onChange={(event) => setConfirmPassword(event.target.value)}
              className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
            />
          </div>
          {error && <p className="text-sm text-red-600">{error}</p>}
          <div className="flex gap-3">
            <button
              type="button"
              onClick={() => {
                setStep('email');
                setError(null);
                setCode('');
                setPassword('');
                setConfirmPassword('');
              }}
              className="rounded-md border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
            >
              Back
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
            >
              {submitting ? 'Creating…' : 'Create account'}
            </button>
          </div>
        </form>
      )}

      <p className="mt-6 text-sm text-gray-600">
        Already have an account?{' '}
        <Link href="/auth/signin" className="font-medium text-blue-600 hover:text-blue-800">
          Sign in
        </Link>
      </p>
      <p className="mt-2 text-sm text-gray-600">
        Forgot which email you used?{' '}
        <Link href="/auth/forgot-email" className="font-medium text-blue-600 hover:text-blue-800">
          Find it with your file number
        </Link>
      </p>

      <details className="mt-8 rounded-md border border-gray-200 bg-gray-50 p-4 text-sm text-gray-700">
        <summary className="cursor-pointer font-medium text-gray-900">Dev test emails</summary>
        <ul className="mt-3 space-y-2">
          <li>
            <strong>jad.mansour@gmail.com</strong> — file 2021005
          </li>
          <li>
            <strong>nour.saad@gmail.com</strong> — file 2022031
          </li>
          <li>
            <strong>karim.haddad@gmail.com</strong> — file 2024001
          </li>
        </ul>
        <p className="mt-2 text-xs text-gray-500">
          Codes go to Mailhog: http://localhost:8025. Skip emails you already registered.
        </p>
      </details>
    </main>
  );
}
