'use client';

import Link from 'next/link';
import { useCallback, useEffect, useState } from 'react';
import { useSession } from 'next-auth/react';
import { useRouter } from 'next/navigation';
import { clientApiRequest } from '@/lib/clientApi';
import { getErrorMessage } from '@/lib/apiError';
import { formatNotificationTime, notificationHref } from '@/lib/notification';
import type {
  NotificationResponseDTO,
  PageResponseDTO,
} from '@/lib/types';

export default function NotificationsPage() {
  const { status } = useSession();
  const router = useRouter();
  const [items, setItems] = useState<NotificationResponseDTO[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadPage = useCallback(async (pageNumber: number) => {
    setLoading(true);
    setError(null);
    try {
      const result = await clientApiRequest<PageResponseDTO<NotificationResponseDTO>>(
        `/api/notifications?page=${pageNumber}&size=20`
      );
      setItems(result.content);
      setPage(result.page);
      setTotalPages(result.totalPages);
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to load notifications'));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (status === 'unauthenticated') {
      router.replace('/auth/signin?callbackUrl=/notifications');
      return;
    }
    if (status === 'authenticated') {
      loadPage(0).catch(() => undefined);
    }
  }, [status, router, loadPage]);

  async function handleMarkAllRead() {
    try {
      await clientApiRequest<void>('/api/notifications/read-all', { method: 'POST' });
      setItems((current) => current.map((item) => ({ ...item, read: true })));
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to mark notifications as read'));
    }
  }

  async function handleItemClick(notification: NotificationResponseDTO) {
    if (!notification.read) {
      try {
        await clientApiRequest<NotificationResponseDTO>(
          `/api/notifications/${notification.id}/read`,
          { method: 'POST' }
        );
        setItems((current) =>
          current.map((item) =>
            item.id === notification.id ? { ...item, read: true } : item
          )
        );
      } catch {
        // still navigate
      }
    }
  }

  if (status === 'loading' || status === 'unauthenticated') {
    return (
      <main className="mx-auto max-w-3xl px-4 py-8">
        <p className="text-sm text-gray-500">Loading…</p>
      </main>
    );
  }

  return (
    <main className="mx-auto max-w-3xl px-4 py-8">
      <div className="flex items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Notifications</h1>
          <p className="mt-1 text-sm text-gray-600">
            Answers, upvotes, and mentions.
          </p>
        </div>
        <button
          type="button"
          onClick={handleMarkAllRead}
          className="rounded-md border border-gray-300 px-3 py-1.5 text-sm font-medium text-gray-700 hover:bg-gray-50"
        >
          Mark all read
        </button>
      </div>

      {error && <p className="mt-4 text-sm text-red-600">{error}</p>}

      <section className="mt-6">
        {loading ? (
          <p className="text-sm text-gray-500">Loading notifications…</p>
        ) : items.length === 0 ? (
          <p className="text-sm text-gray-500">You have no notifications yet.</p>
        ) : (
          <ul className="divide-y divide-gray-200 rounded-lg border border-gray-200 bg-white">
            {items.map((notification) => (
              <li key={notification.id}>
                <Link
                  href={notificationHref(notification)}
                  onClick={() => handleItemClick(notification)}
                  className={`block px-4 py-4 hover:bg-gray-50 ${
                    notification.read ? '' : 'bg-blue-50/60'
                  }`}
                >
                  <p className="font-medium text-gray-900">{notification.message}</p>
                  <p className="mt-1 text-xs text-gray-500">
                    {formatNotificationTime(notification.createdAt)}
                  </p>
                </Link>
              </li>
            ))}
          </ul>
        )}
      </section>

      {totalPages > 1 && (
        <div className="mt-4 flex items-center gap-3">
          <button
            type="button"
            disabled={page <= 0 || loading}
            onClick={() => loadPage(page - 1)}
            className="rounded-md border border-gray-300 px-3 py-1.5 text-sm disabled:opacity-50"
          >
            Previous
          </button>
          <span className="text-sm text-gray-600">
            Page {page + 1} of {totalPages}
          </span>
          <button
            type="button"
            disabled={page + 1 >= totalPages || loading}
            onClick={() => loadPage(page + 1)}
            className="rounded-md border border-gray-300 px-3 py-1.5 text-sm disabled:opacity-50"
          >
            Next
          </button>
        </div>
      )}
    </main>
  );
}
