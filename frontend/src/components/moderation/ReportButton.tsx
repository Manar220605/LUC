'use client';

import { FormEvent, useState } from 'react';
import { getSession, signIn } from 'next-auth/react';
import { clientApiRequest } from '@/lib/clientApi';
import { getErrorMessage, SIGN_IN_REQUIRED } from '@/lib/apiError';
import {
  REPORT_REASONS,
  type CreateReportRequestDTO,
  type ReportReason,
  type ReportResponseDTO,
  type ReportTargetType,
} from '@/lib/report';

type Props = {
  targetType: ReportTargetType;
  targetId: number;
  label?: string;
  className?: string;
};

async function submitReport(payload: CreateReportRequestDTO): Promise<ReportResponseDTO> {
  const session = await getSession();
  if (session?.error === 'RefreshAccessTokenError' || !session?.accessToken) {
    throw new Error(SIGN_IN_REQUIRED);
  }

  return clientApiRequest<ReportResponseDTO>('/api/reports', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export default function ReportButton({
  targetType,
  targetId,
  label = 'Report',
  className = 'font-medium text-red-600 hover:text-red-800',
}: Props) {
  const [open, setOpen] = useState(false);
  const [reason, setReason] = useState<ReportReason>('SPAM');
  const [details, setDetails] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  async function handleOpen() {
    setError(null);
    setSuccess(null);
    const session = await getSession();
    if (!session?.accessToken || session.error === 'RefreshAccessTokenError') {
      signIn('keycloak', { callbackUrl: window.location.href });
      return;
    }
    setOpen(true);
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setSuccess(null);
    setSubmitting(true);

    try {
      await submitReport({
        targetType,
        targetId,
        reason,
        details: details.trim() || undefined,
      });
      setSuccess('Report submitted. Moderators will review it.');
      setDetails('');
      setReason('SPAM');
    } catch (err) {
      if (err instanceof Error && err.message === SIGN_IN_REQUIRED) {
        signIn('keycloak', { callbackUrl: window.location.href });
        return;
      }
      setError(getErrorMessage(err, 'Failed to submit report'));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <>
      <button type="button" onClick={handleOpen} className={className}>
        {label}
      </button>

      {open && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
          <div
            role="dialog"
            aria-modal="true"
            aria-labelledby="report-dialog-title"
            className="w-full max-w-md rounded-lg border border-lu/10 bg-white p-5 shadow-xl"
          >
            <div className="flex items-start justify-between gap-4">
              <div>
                <h2 id="report-dialog-title" className="text-lg font-semibold text-lu-deep">
                  Report content
                </h2>
                <p className="mt-1 text-sm text-muted">
                  Tell moderators why this should be reviewed.
                </p>
              </div>
              <button
                type="button"
                onClick={() => {
                  setOpen(false);
                  setError(null);
                  setSuccess(null);
                }}
                className="text-muted hover:text-ink"
                aria-label="Close"
              >
                ✕
              </button>
            </div>

            {success ? (
              <div className="mt-4 space-y-4">
                <p className="rounded border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-800">
                  {success}
                </p>
                <button
                  type="button"
                  onClick={() => {
                    setOpen(false);
                    setSuccess(null);
                  }}
                  className="rounded-md bg-lu px-4 py-2 text-sm font-medium text-white hover:bg-lu-dark"
                >
                  Close
                </button>
              </div>
            ) : (
              <form onSubmit={handleSubmit} className="mt-4 space-y-4">
                <label className="block text-sm">
                  Reason
                  <select
                    className="mt-1 w-full rounded border border-lu/20 px-3 py-2"
                    value={reason}
                    onChange={(event) => setReason(event.target.value as ReportReason)}
                    required
                  >
                    {REPORT_REASONS.map((option) => (
                      <option key={option.value} value={option.value}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                </label>

                <label className="block text-sm">
                  Additional details (optional)
                  <textarea
                    className="mt-1 w-full rounded border border-lu/20 px-3 py-2"
                    rows={4}
                    value={details}
                    onChange={(event) => setDetails(event.target.value)}
                    maxLength={2000}
                    placeholder="Provide context for moderators…"
                  />
                </label>

                {error && (
                  <p className="rounded border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
                    {error}
                  </p>
                )}

                <div className="flex justify-end gap-2">
                  <button
                    type="button"
                    onClick={() => setOpen(false)}
                    className="rounded-md border border-lu/20 px-4 py-2 text-sm text-ink hover:bg-lu-soft"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={submitting}
                    className="rounded-md bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700 disabled:opacity-50"
                  >
                    {submitting ? 'Submitting…' : 'Submit report'}
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}
    </>
  );
}
