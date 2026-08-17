import { ApiError } from '@/lib/apiError';

const API_URL =
  process.env.API_URL ??
  process.env.NEXT_PUBLIC_API_URL ??
  'http://localhost:8080';

export async function apiPublicGet<T>(path: string): Promise<T> {
  const res = await fetch(`${API_URL}${path}`, { cache: 'no-store' });
  if (!res.ok) {
    throw await ApiError.fromResponse(res);
  }
  return res.json();
}

export async function apiPublicPost<T>(path: string, body: unknown): Promise<T> {
  const res = await fetch(`${API_URL}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
    cache: 'no-store',
  });
  if (!res.ok) {
    throw await ApiError.fromResponse(res);
  }
  return res.json();
}
