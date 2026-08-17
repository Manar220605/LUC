'use client';

import { FormEvent, useCallback, useEffect, useState } from 'react';
import { useSession } from 'next-auth/react';
import { useRouter } from 'next/navigation';
import { clientApiRequest } from '@/lib/clientApi';
import { getErrorMessage, isApiError } from '@/lib/apiError';
import { DEGREES, FACULTIES, degreeLabel, facultyLabel } from '@/lib/alumni';
import type {
  AlumniProfileResponseDTO,
  Degree,
  Faculty,
  SubmitVerificationRequestDTO,
  UpdateAlumniProfileRequestDTO,
  VerificationResponseDTO,
} from '@/lib/types';

async function loadOptional<T>(path: string): Promise<T | null> {
  try {
    return await clientApiRequest<T>(path);
  } catch (err) {
    if (isApiError(err, 404)) {
      return null;
    }
    throw err;
  }
}

export default function AlumniVerifyPage() {
  const { status } = useSession();
  const router = useRouter();
  const [verification, setVerification] = useState<VerificationResponseDTO | null>(null);
  const [profile, setProfile] = useState<AlumniProfileResponseDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const [linkedinUrl, setLinkedinUrl] = useState('');
  const [claimedGradYear, setClaimedGradYear] = useState(String(new Date().getFullYear()));
  const [claimedFaculty, setClaimedFaculty] = useState<Faculty>('ENGINEERING');
  const [claimedDegree, setClaimedDegree] = useState<Degree>('BS');
  const [claimedMajor, setClaimedMajor] = useState('');
  const [claimedPosition, setClaimedPosition] = useState('');
  const [claimedCompany, setClaimedCompany] = useState('');

  const [currentPosition, setCurrentPosition] = useState('');
  const [currentCompany, setCurrentCompany] = useState('');
  const [isPublic, setIsPublic] = useState(true);
  const [savingProfile, setSavingProfile] = useState(false);

  const loadData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [verificationData, profileData] = await Promise.all([
        loadOptional<VerificationResponseDTO>('/api/alumni/verifications/me'),
        loadOptional<AlumniProfileResponseDTO>('/api/alumni/profiles/me'),
      ]);
      setVerification(verificationData);
      setProfile(profileData);
      if (profileData) {
        setCurrentPosition(profileData.currentPosition ?? '');
        setCurrentCompany(profileData.currentCompany ?? '');
        setIsPublic(profileData.isPublic);
      }
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to load alumni data'));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (status === 'unauthenticated') {
      router.push('/auth/signin');
      return;
    }
    if (status === 'authenticated') {
      loadData();
    }
  }, [status, router, loadData]);

  const canSubmit = !verification || verification.status === 'REJECTED';

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    if (verification?.status === 'PENDING') {
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      const body: SubmitVerificationRequestDTO = {
        linkedinUrl: linkedinUrl.trim(),
        claimedGradYear: Number(claimedGradYear),
        claimedFaculty,
        claimedDegree,
        claimedMajor: claimedMajor.trim(),
        claimedPosition: claimedPosition.trim() || undefined,
        claimedCompany: claimedCompany.trim() || undefined,
      };
      const created = await clientApiRequest<VerificationResponseDTO>('/api/alumni/verifications', {
        method: 'POST',
        body: JSON.stringify(body),
      });
      setVerification(created);
    } catch (err) {
      setError(getErrorMessage(err, 'Submission failed'));
    } finally {
      setSubmitting(false);
    }
  }

  async function handleProfileUpdate(e: FormEvent) {
    e.preventDefault();
    setSavingProfile(true);
    setError(null);
    try {
      const body: UpdateAlumniProfileRequestDTO = {
        currentPosition: currentPosition.trim() || undefined,
        currentCompany: currentCompany.trim() || undefined,
        isPublic,
      };
      const updated = await clientApiRequest<AlumniProfileResponseDTO>('/api/alumni/profiles/me', {
        method: 'PUT',
        body: JSON.stringify(body),
      });
      setProfile(updated);
    } catch (err) {
      setError(getErrorMessage(err, 'Profile update failed'));
    } finally {
      setSavingProfile(false);
    }
  }

  if (status === 'loading' || loading) {
    return (
      <main className="mx-auto max-w-4xl px-4 py-8">
        <p className="text-gray-600">Loading alumni verification…</p>
      </main>
    );
  }

  return (
    <main className="mx-auto max-w-4xl px-4 py-8">
      <h1 className="text-2xl font-semibold text-gray-900">Alumni verification</h1>
      <p className="mt-1 text-sm text-gray-600">
        Submit your LinkedIn profile and graduation details for manual review by an admin.
      </p>

      {error && (
        <p className="mt-4 rounded border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
          {error}
        </p>
      )}

      {verification && (
        <section className="mt-6 rounded-lg border border-gray-200 bg-white p-4">
          <h2 className="text-lg font-medium text-gray-900">Current request status</h2>
          <dl className="mt-3 grid gap-2 text-sm">
            <div className="flex gap-2">
              <dt className="w-32 font-medium text-gray-500">Status</dt>
              <dd className="text-gray-900">{verification.status}</dd>
            </div>
            <div className="flex gap-2">
              <dt className="w-32 font-medium text-gray-500">Submitted</dt>
              <dd className="text-gray-900">
                {new Date(verification.submittedAt).toLocaleString()}
              </dd>
            </div>
            <div className="flex gap-2">
              <dt className="w-32 font-medium text-gray-500">LinkedIn</dt>
              <dd>
                <a
                  href={verification.linkedinUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-blue-600 hover:text-blue-800"
                >
                  {verification.linkedinUrl}
                </a>
              </dd>
            </div>
            <div className="flex gap-2">
              <dt className="w-32 font-medium text-gray-500">Graduation</dt>
              <dd className="text-gray-900">
                {verification.claimedGradYear} · {facultyLabel(verification.claimedFaculty)} ·{' '}
                {degreeLabel(verification.claimedDegree)} · {verification.claimedMajor}
              </dd>
            </div>
            {verification.rejectionReason && (
              <div className="flex gap-2">
                <dt className="w-32 font-medium text-gray-500">Rejection reason</dt>
                <dd className="text-red-700">{verification.rejectionReason}</dd>
              </div>
            )}
          </dl>
        </section>
      )}

      {canSubmit && verification?.status !== 'PENDING' && (
        <section className="mt-8 rounded-lg border border-gray-200 bg-white p-4">
          <h2 className="text-lg font-medium text-gray-900">
            {verification?.status === 'REJECTED' ? 'Submit a new request' : 'Submit verification'}
          </h2>
          <form onSubmit={handleSubmit} className="mt-4 grid gap-3 sm:grid-cols-2">
            <label className="text-sm sm:col-span-2">
              LinkedIn profile URL
              <input
                type="url"
                className="mt-1 w-full rounded border border-gray-300 px-2 py-1"
                value={linkedinUrl}
                onChange={(e) => setLinkedinUrl(e.target.value)}
                required
              />
            </label>
            <label className="text-sm">
              Graduation year
              <input
                type="number"
                min={1950}
                max={2100}
                className="mt-1 w-full rounded border border-gray-300 px-2 py-1"
                value={claimedGradYear}
                onChange={(e) => setClaimedGradYear(e.target.value)}
                required
              />
            </label>
            <label className="text-sm">
              Degree
              <select
                className="mt-1 w-full rounded border border-gray-300 px-2 py-1"
                value={claimedDegree}
                onChange={(e) => setClaimedDegree(e.target.value as Degree)}
                required
              >
                {DEGREES.map((entry) => (
                  <option key={entry.value} value={entry.value}>
                    {entry.label}
                  </option>
                ))}
              </select>
            </label>
            <label className="text-sm sm:col-span-2">
              Faculty
              <select
                className="mt-1 w-full rounded border border-gray-300 px-2 py-1"
                value={claimedFaculty}
                onChange={(e) => setClaimedFaculty(e.target.value as Faculty)}
                required
              >
                {FACULTIES.map((entry) => (
                  <option key={entry.value} value={entry.value}>
                    {entry.label}
                  </option>
                ))}
              </select>
            </label>
            <label className="text-sm sm:col-span-2">
              Major
              <input
                className="mt-1 w-full rounded border border-gray-300 px-2 py-1"
                value={claimedMajor}
                onChange={(e) => setClaimedMajor(e.target.value)}
                required
              />
            </label>
            <label className="text-sm">
              Current position
              <input
                className="mt-1 w-full rounded border border-gray-300 px-2 py-1"
                value={claimedPosition}
                onChange={(e) => setClaimedPosition(e.target.value)}
              />
            </label>
            <label className="text-sm">
              Current company
              <input
                className="mt-1 w-full rounded border border-gray-300 px-2 py-1"
                value={claimedCompany}
                onChange={(e) => setClaimedCompany(e.target.value)}
              />
            </label>
            <div className="sm:col-span-2">
              <button
                type="submit"
                disabled={submitting}
                className="rounded bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
              >
                {submitting ? 'Submitting…' : 'Submit for review'}
              </button>
            </div>
          </form>
        </section>
      )}

      {profile && (
        <section className="mt-8 rounded-lg border border-purple-200 bg-purple-50 p-4">
          <h2 className="text-lg font-medium text-gray-900">Your alumni profile</h2>
          <p className="mt-1 text-sm text-gray-600">
            Update how your alumni details appear on your public posts.
          </p>
          <form onSubmit={handleProfileUpdate} className="mt-4 grid gap-3 sm:grid-cols-2">
            <label className="text-sm">
              Current position
              <input
                className="mt-1 w-full rounded border border-gray-300 px-2 py-1"
                value={currentPosition}
                onChange={(e) => setCurrentPosition(e.target.value)}
              />
            </label>
            <label className="text-sm">
              Current company
              <input
                className="mt-1 w-full rounded border border-gray-300 px-2 py-1"
                value={currentCompany}
                onChange={(e) => setCurrentCompany(e.target.value)}
              />
            </label>
            <label className="flex items-center gap-2 text-sm sm:col-span-2">
              <input
                type="checkbox"
                checked={isPublic}
                onChange={(e) => setIsPublic(e.target.checked)}
              />
              Show graduation year and position on my public posts
            </label>
            <div className="sm:col-span-2">
              <button
                type="submit"
                disabled={savingProfile}
                className="rounded bg-purple-700 px-4 py-2 text-sm font-medium text-white hover:bg-purple-800 disabled:opacity-50"
              >
                {savingProfile ? 'Saving…' : 'Save profile'}
              </button>
            </div>
          </form>
        </section>
      )}
    </main>
  );
}
