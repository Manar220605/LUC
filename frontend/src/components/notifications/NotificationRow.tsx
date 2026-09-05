import Link from 'next/link';
import { formatNotificationTime, notificationHref } from '@/lib/notification';
import type { NotificationResponseDTO } from '@/lib/types';

type Props = {
  notification: NotificationResponseDTO;
  compact?: boolean;
  onClick?: () => void;
};

export default function NotificationRow({ notification, compact = false, onClick }: Props) {
  const unread = !notification.read;

  return (
    <Link
      href={notificationHref(notification)}
      onClick={onClick}
      className={`flex items-start gap-3 ${compact ? 'px-4 py-3' : 'px-4 py-4'} ${
        unread
          ? 'bg-lu-soft hover:bg-lu-soft/70'
          : 'bg-white hover:bg-lu-soft'
      }`}
    >
      <span
        className={`mt-1.5 h-2 w-2 shrink-0 rounded-full ${
          unread ? 'bg-lu' : 'bg-transparent'
        }`}
        aria-hidden="true"
      />
      <span className="min-w-0 flex-1">
        <p
          className={`text-sm ${
            unread ? 'font-semibold text-lu-deep' : 'font-normal text-muted'
          }`}
        >
          {notification.message}
        </p>
        <p className="mt-1 text-xs text-muted/70">
          {formatNotificationTime(notification.createdAt)}
          {unread ? (
            <span className="ml-2 font-medium text-lu">New</span>
          ) : null}
        </p>
      </span>
    </Link>
  );
}
