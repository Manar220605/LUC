'use client';

import Link from 'next/link';
import { useCallback, useEffect, useRef, useState } from 'react';
import { useSession } from 'next-auth/react';
import { clientApiRequest } from '@/lib/clientApi';
import {
  notifyNotificationsRead,
  onNotificationsRead,
} from '@/lib/notification';
import NotificationRow from '@/components/notifications/NotificationRow';
import type {
  NotificationResponseDTO,
  PageResponseDTO,
  UnreadCountResponseDTO,
} from '@/lib/types';

const POLL_INTERVAL_MS = 30_000;

function markRead(
  items: NotificationResponseDTO[],
  id?: number
): NotificationResponseDTO[] {
  return items.map((item) =>
    id == null || item.id === id ? { ...item, read: true } : item
  );
}

export default function NotificationBell() {
  const { status } = useSession();
  const [open, setOpen] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);
  const [items, setItems] = useState<NotificationResponseDTO[]>([]);
  const [loading, setLoading] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  const refreshUnreadCount = useCallback(async () => {
    if (status !== 'authenticated') {
      setUnreadCount(0);
      return;
    }
    try {
      const result = await clientApiRequest<UnreadCountResponseDTO>(
        '/api/notifications/unread-count'
      );
      setUnreadCount(result.count);
    } catch {
      // ignore polling errors
    }
  }, [status]);

  const loadRecent = useCallback(async () => {
    if (status !== 'authenticated') {
      return;
    }
    setLoading(true);
    try {
      const result = await clientApiRequest<PageResponseDTO<NotificationResponseDTO>>(
        '/api/notifications?page=0&size=8'
      );
      setItems(result.content);
    } catch {
      // ignore load errors
    } finally {
      setLoading(false);
    }
  }, [status]);

  useEffect(() => {
    refreshUnreadCount().catch(() => undefined);
    const interval = window.setInterval(() => {
      refreshUnreadCount().catch(() => undefined);
    }, POLL_INTERVAL_MS);
    return () => window.clearInterval(interval);
  }, [refreshUnreadCount]);

  useEffect(() => onNotificationsRead(() => {
    setUnreadCount(0);
    setItems((current) => markRead(current));
  }), []);

  useEffect(() => {
    if (open) {
      loadRecent().catch(() => undefined);
    }
  }, [open, loadRecent]);

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (
        containerRef.current &&
        !containerRef.current.contains(event.target as Node)
      ) {
        setOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  async function handleNotificationClick(notification: NotificationResponseDTO) {
    if (!notification.read) {
      try {
        await clientApiRequest<NotificationResponseDTO>(
          `/api/notifications/${notification.id}/read`,
          { method: 'POST' }
        );
        setUnreadCount((count) => Math.max(0, count - 1));
        setItems((current) => markRead(current, notification.id));
      } catch {
        // navigation still works if mark-read fails
      }
    }
    setOpen(false);
  }

  async function handleMarkAllRead() {
    try {
      await clientApiRequest<void>('/api/notifications/read-all', { method: 'POST' });
      setUnreadCount(0);
      setItems((current) => markRead(current));
      notifyNotificationsRead();
    } catch {
      // ignore
    }
  }

  if (status !== 'authenticated') {
    return null;
  }

  const hasUnread = items.some((item) => !item.read);

  return (
    <div ref={containerRef} className="relative">
      <button
        type="button"
        onClick={() => setOpen((value) => !value)}
        aria-label={`Notifications${unreadCount > 0 ? `, ${unreadCount} unread` : ''}`}
        className="relative rounded-md border border-gray-300 p-2 text-gray-700 hover:bg-gray-50"
      >
        <svg
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="1.8"
          className="h-5 w-5"
          aria-hidden="true"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            d="M15 17h5l-1.4-1.4A2 2 0 0 1 18 14.2V11a6 6 0 1 0-12 0v3.2c0 .5-.2 1-.6 1.4L4 17h5m6 0a3 3 0 0 1-6 0m6 0H9"
          />
        </svg>
        {unreadCount > 0 && (
          <span className="absolute -right-1 -top-1 flex h-5 min-w-5 items-center justify-center rounded-full bg-red-600 px-1 text-[10px] font-bold text-white">
            {unreadCount > 99 ? '99+' : unreadCount}
          </span>
        )}
      </button>

      {open && (
        <div className="absolute right-0 z-20 mt-2 w-80 rounded-md border border-gray-200 bg-white shadow-lg">
          <div className="flex items-center justify-between border-b border-gray-100 px-4 py-3">
            <h2 className="text-sm font-semibold text-gray-900">Notifications</h2>
            {hasUnread && (
              <button
                type="button"
                onClick={handleMarkAllRead}
                className="text-xs font-medium text-blue-600 hover:text-blue-800"
              >
                Mark all read
              </button>
            )}
          </div>

          <div className="max-h-96 overflow-y-auto">
            {loading ? (
              <p className="px-4 py-6 text-sm text-gray-500">Loading…</p>
            ) : items.length === 0 ? (
              <p className="px-4 py-6 text-sm text-gray-500">No notifications yet.</p>
            ) : (
              <ul className="divide-y divide-gray-100">
                {items.map((notification) => (
                  <li key={notification.id}>
                    <NotificationRow
                      notification={notification}
                      compact
                      onClick={() => handleNotificationClick(notification)}
                    />
                  </li>
                ))}
              </ul>
            )}
          </div>

          <div className="border-t border-gray-100 px-4 py-2 text-center">
            <Link
              href="/notifications"
              onClick={() => setOpen(false)}
              className="text-xs font-medium text-blue-600 hover:text-blue-800"
            >
              View all
            </Link>
          </div>
        </div>
      )}
    </div>
  );
}
