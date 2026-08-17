import type { NotificationResponseDTO } from '@/lib/types';

export function notificationHref(notification: NotificationResponseDTO): string {
  if (notification.questionId != null) {
    return `/questions/${notification.questionId}`;
  }
  return '/notifications';
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
