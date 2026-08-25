import type { NotificationResponseDTO } from '@/lib/types';

export function notificationHref(notification: NotificationResponseDTO): string {
  if (
    notification.type === 'MENTORSHIP_REQUEST' ||
    notification.type === 'MENTORSHIP_ACCEPTED' ||
    notification.type === 'MENTORSHIP_DECLINED'
  ) {
    return '/mentorship';
  }
  if (notification.questionId != null) {
    return `/questions/${notification.questionId}`;
  }
  return '/notifications';
}

const NOTIFICATIONS_READ_EVENT = 'luc:notifications-read';

export function notifyNotificationsRead(): void {
  if (typeof window !== 'undefined') {
    window.dispatchEvent(new Event(NOTIFICATIONS_READ_EVENT));
  }
}

export function onNotificationsRead(handler: () => void): () => void {
  if (typeof window === 'undefined') {
    return () => undefined;
  }
  window.addEventListener(NOTIFICATIONS_READ_EVENT, handler);
  return () => window.removeEventListener(NOTIFICATIONS_READ_EVENT, handler);
}

export function formatNotificationTime(iso: string): string {
  const date = new Date(iso);
  const now = Date.now();
  const diffMs = now - date.getTime();
  const diffMinutes = Math.floor(diffMs / 60_000);

  if (diffMinutes < 1) {
    return 'Just now';
  }
  if (diffMinutes < 60) {
    return `${diffMinutes}m ago`;
  }
  const diffHours = Math.floor(diffMinutes / 60);
  if (diffHours < 24) {
    return `${diffHours}h ago`;
  }
  return date.toLocaleDateString();
}
