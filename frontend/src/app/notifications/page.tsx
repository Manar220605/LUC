'use client';

import { useCallback, useEffect, useState } from 'react';
import { useSession } from 'next-auth/react';
import { useRouter } from 'next/navigation';
import { clientApiRequest } from '@/lib/clientApi';
import { getErrorMessage } from '@/lib/apiError';
import { notifyNotificationsRead } from '@/lib/notification';
import NotificationRow from '@/components/notifications/NotificationRow';
import type {
  NotificationResponseDTO,
  PageResponseDTO,
} from '@/lib/types';

function markRead(
  items: NotificationResponseDTO[],
  id?: number
): NotificationResponseDTO[] {
  return items.map((item) =>
    id == null || item.id === id ? { ...item, read: true } : item
  );
}

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

  const hasUnread = items.some((item) => !item.read);

  async function handleMarkAllRead() {
    try {
      await clientApiRequest<void>('/api/notifications/read-all', { method: 'POST' });
      setItems((current) => markRead(current));
      notifyNotificationsRead();
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to mark notifications as read'));
    }
  }

  async function handleItemClick(notification: NotificationResponseDTO) {
    if (notification.read) {
      return;
    }
    try {
      await clientApiRequest<NotificationResponseDTO>(
        `/api/notifications/${notification.id}/read`,
        { method: 'POST' }
      );
      setItems((current) => markRead(current, notification.id));
    } catch {
      // still navigate
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
            Unread items stay highlighted. Opening one marks it read and keeps it here.
          </p>
        </div>
        {hasUnread && (
          <button
            type="button"
            onClick={handleMarkAllRead}
            className="rounded-md border border-gray-300 px-3 py-1.5 text-sm font-medium text-gray-700 hover:bg-gray-50"
          >
            Mark all read
          </button>
        )}
      </div>

      {error && <p className="mt-4 text-sm text-red-600">{error}</p>}

      <section className="mt-6">
        {loading ? (
          <p className="text-sm text-gray-500">Loading notifications…</p>
        ) : items.length === 0 ? (
          <p className="text-sm text-gray-500">No notifications yet.</p>
        ) : (
          <ul className="divide-y divide-gray-200 overflow-hidden rounded-lg border border-gray-200 bg-white">
            {items.map((notification) => (
              <li key={notification.id}>
                <NotificationRow
                  notification={notification}
                  onClick={() => handleItemClick(notification)}
                />
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
