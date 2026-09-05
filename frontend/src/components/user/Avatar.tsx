import { avatarSrc } from '@/lib/url';

type Props = {
  name: string;
  avatarUrl?: string | null;
  previewSrc?: string | null;
  size?: 'sm' | 'md' | 'lg';
};

function initials(name: string): string {
  const parts = name.trim().split(/\s+/).filter(Boolean);
  if (parts.length === 0) {
    return '?';
  }
  if (parts.length === 1) {
    return parts[0].slice(0, 2).toUpperCase();
  }
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
}

export default function Avatar({ name, avatarUrl, previewSrc, size = 'lg' }: Props) {
  const dimension =
    size === 'lg' ? 'h-20 w-20 text-xl' : size === 'md' ? 'h-12 w-12 text-sm' : 'h-8 w-8 text-xs';
  const src = previewSrc || avatarSrc(avatarUrl);

  if (src) {
    return (
      // eslint-disable-next-line @next/next/no-img-element
      <img
        src={src}
        alt=""
        className={`${dimension} rounded-full object-cover ring-2 ring-lu/15`}
      />
    );
  }

  return (
    <span
      aria-hidden="true"
      className={`inline-flex ${dimension} items-center justify-center rounded-full bg-lu-soft font-semibold text-lu-dark`}
    >
      {initials(name)}
    </span>
  );
}
