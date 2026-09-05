'use client';

import Link from 'next/link';
import { FormEvent, useState } from 'react';
import { apiPublicPost } from '@/lib/apiPublic';
import { getErrorMessage } from '@/lib/apiError';
import type { LookupStudentResponseDTO, RegisterStudentResponseDTO } from '@/lib/types';
import { alertOk, btnPrimary, btnSecondary, cardPad, errorText, heading, input, label, pageNarrow } from '@/lib/ui';

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
      <main className={pageNarrow}>
        <div className={cardPad}>
          <h1 className={heading}>Account ready</h1>
          <p className="mt-3 text-ink">{success.message}</p>
          <div className={`${alertOk} mt-6`}>
            <p className="font-medium">{success.fullName}</p>
            <p className="mt-1">Enrollment year: {success.enrollmentYear}</p>
            <p>Faculty: {success.faculty.replaceAll('_', ' ')}</p>
            <p>Major: {success.major}</p>
          </div>
          <Link href="/auth/signin" className={`${btnPrimary} mt-6`}>
            Sign in
          </Link>
        </div>
      </main>
    );
  }

  return (
    <main className={pageNarrow}>
      <div className={cardPad}>
        <h1 className={heading}>Student registration</h1>
        <p className="mt-2 text-sm text-muted">
          Use a seeded student email from the list below. Activation codes are captured by{' '}
          <a className="font-medium text-lu hover:text-lu-dark" href="http://localhost:8025" target="_blank" rel="noreferrer">
            Mailhog
          </a>{' '}
          (they are not delivered to real Gmail inboxes in local dev).
        </p>

        <div className="mt-4 rounded-xl border border-lu/15 bg-lu-mist p-4 text-sm text-ink">
          <p className="font-medium text-lu-deep">Dev test emails (use one of these)</p>
          <ul className="mt-2 space-y-1.5">
            <li>
              <strong>jad.mansour@gmail.com</strong> — file 91567
            </li>
            <li>
              <strong>nour.saad@gmail.com</strong> — file 100334
            </li>
            <li>
              <strong>karim.haddad@gmail.com</strong> — file 95201
            </li>
          </ul>
          <p className="mt-2 text-xs text-muted">
            After “Send code”, open http://localhost:8025 and read the message subject “Your LUC
            account activation code”. Unknown emails still show a success message but no code is sent.
          </p>
        </div>

        {step === 'email' && (
          <form onSubmit={handleLookup} className="mt-8 space-y-4">
            <div>
              <label htmlFor="email" className={label}>
                Email (from the seed list above)
              </label>
              <input
                id="email"
                type="email"
                required
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                placeholder="jad.mansour@gmail.com"
                className={input}
              />
            </div>
            {error && <p className={errorText}>{error}</p>}
            <button type="submit" disabled={submitting} className={btnPrimary}>
              {submitting ? 'Sending…' : 'Send code'}
            </button>
          </form>
        )}

        {step === 'activate' && (
          <form onSubmit={handleActivate} className="mt-8 space-y-4">
            <p className="text-sm text-ink">{lookupMessage}</p>
            <p className="rounded-lg border border-lu/15 bg-lu-soft px-3 py-2 text-sm text-lu-deep">
              Get your code from{' '}
              <a className="font-semibold underline" href="http://localhost:8025" target="_blank" rel="noreferrer">
                Mailhog (localhost:8025)
              </a>
              — not from Gmail.
            </p>
            <div>
              <label htmlFor="code" className={label}>
                6-character code
              </label>
              <input
                id="code"
                type="text"
                required
                value={code}
                onChange={(event) => setCode(event.target.value.toUpperCase())}
                placeholder="ABC123"
                className={`${input} tracking-widest`}
              />
            </div>
            <div>
              <label htmlFor="password" className={label}>
                Password
              </label>
              <input
                id="password"
                type="password"
                required
                minLength={8}
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                className={input}
              />
            </div>
            <div>
              <label htmlFor="confirmPassword" className={label}>
                Confirm password
              </label>
              <input
                id="confirmPassword"
                type="password"
                required
                minLength={8}
                value={confirmPassword}
                onChange={(event) => setConfirmPassword(event.target.value)}
                className={input}
              />
            </div>
            {error && <p className={errorText}>{error}</p>}
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
                className={btnSecondary}
              >
                Back
              </button>
              <button type="submit" disabled={submitting} className={btnPrimary}>
                {submitting ? 'Creating…' : 'Create account'}
              </button>
            </div>
          </form>
        )}

        <p className="mt-6 text-sm text-muted">
          Already have an account?{' '}
          <Link href="/auth/signin" className="font-medium text-lu hover:text-lu-dark">
            Sign in
          </Link>
        </p>
        <p className="mt-2 text-sm text-muted">
          Alumni?{' '}
          <Link href="/auth/alumni-signup" className="font-medium text-lu hover:text-lu-dark">
            Sign up with LinkedIn
          </Link>
        </p>
        <p className="mt-2 text-sm text-muted">
          Forgot which email you used?{' '}
          <Link href="/auth/forgot-email" className="font-medium text-lu hover:text-lu-dark">
            Find it with your file number
          </Link>
        </p>
      </div>
    </main>
  );
}
