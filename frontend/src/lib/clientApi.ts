'use client';

import { getSession } from 'next-auth/react';
import { ApiError } from '@/lib/apiError';

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080';

export async function clientApiRequest<T>(
  path: string,
  init: RequestInit = {}
): Promise<T> {
  const session = await getSession();
  if (session?.error === 'RefreshAccessTokenError') {
    throw new ApiError(401, 'Session expired; please sign in again');
  }

  const token = session?.accessToken;
  const res = await fetch(`${API_URL}${path}`, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...(token && { Authorization: `Bearer ${token}` }),
      ...init.headers,
    },
    cache: 'no-store',
  });

  if (!res.ok) {
    throw await ApiError.fromResponse(res);
  }

  if (res.status === 204) {
    return undefined as T;
  }

  return res.json() as Promise<T>;
}

export async function clientApiUpload<T>(path: string, file: File): Promise<T> {
  const session = await getSession();
  if (session?.error === 'RefreshAccessTokenError') {
    throw new ApiError(401, 'Session expired; please sign in again');
  }

  const token = session?.accessToken;
  const body = new FormData();
  body.append('file', file);

  const res = await fetch(`${API_URL}${path}`, {
    method: 'POST',
    headers: {
      ...(token && { Authorization: `Bearer ${token}` }),
    },
    body,
    cache: 'no-store',
  });

  if (!res.ok) {
    throw await ApiError.fromResponse(res);
  }
  if (res.status === 204) {
    return undefined as T;
  }
  return res.json() as Promise<T>;
}
