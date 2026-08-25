export function isSafeHttpUrl(value: string | null | undefined): value is string {
  if (!value) {
    return false;
  }
  try {
    const url = new URL(value);
    return url.protocol === 'http:' || url.protocol === 'https:';
  } catch {
    return false;
  }
}

const STORED_AVATAR =
  /^\/api\/uploads\/avatars\/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\.(jpg|png|webp)$/;

export function avatarSrc(avatarUrl: string | null | undefined): string | null {
  if (!avatarUrl) {
    return null;
  }
  if (STORED_AVATAR.test(avatarUrl)) {
    const base = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080';
    return `${base}${avatarUrl}`;
  }
  if (isSafeHttpUrl(avatarUrl)) {
    return avatarUrl;
  }
  return null;
}

