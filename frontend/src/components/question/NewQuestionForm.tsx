'use client';

import { FormEvent, useEffect, useState } from 'react';
import { getSession } from 'next-auth/react';
import { useRouter } from 'next/navigation';
import CommunityPicker from '@/components/community/CommunityPicker';
import type {
  CommunityTreeNodeDTO,
  CreateQuestionRequestDTO,
} from '@/lib/types';

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080';

export default function NewQuestionForm() {
  const router = useRouter();
  const [tree, setTree] = useState<CommunityTreeNodeDTO[]>([]);
  const [communityPath, setCommunityPath] = useState('cs');
  const [title, setTitle] = useState('');
  const [body, setBody] = useState('');
  const [anonymous, setAnonymous] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetch(`${API_URL}/api/communities`, { cache: 'no-store' })
      .then((res) => {
        if (!res.ok) {
          throw new Error(`API ${res.status}`);
        }
        return res.json();
      })
      .then((data: CommunityTreeNodeDTO[]) => {
        setTree(data);
        if (data.length > 0) {
          setCommunityPath(data[0].path);
        }
      })
      .catch(() => setError('Failed to load communities'));
  }, []);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);

    try {
      const session = await getSession();
      if (session?.error === 'RefreshAccessTokenError') {
        throw new Error('Session expired; please sign in again');
      }
      const token = session?.accessToken;
      if (!token) {
        throw new Error('You must be signed in to post a question');
      }

      const payload: CreateQuestionRequestDTO = {
        communityPath,
        title: title.trim(),
        body: body.trim(),
        anonymous,
      };

      const res = await fetch(`${API_URL}/api/questions`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(payload),
      });

      if (!res.ok) {
        throw new Error(`API ${res.status}: ${await res.text()}`);
      }

      const created = await res.json();
      router.push(`/questions/${created.id}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create question');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {error && (
        <p className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
          {error}
        </p>
      )}

      <div>
        <label htmlFor="community" className="block text-sm font-medium text-gray-700">
          Community
        </label>
        {tree.length > 0 ? (
          <CommunityPicker nodes={tree} value={communityPath} onChange={setCommunityPath} />
        ) : (
          <p className="mt-1 text-sm text-gray-500">Loading communities…</p>
        )}
      </div>

      <div>
        <label htmlFor="title" className="block text-sm font-medium text-gray-700">
          Title
        </label>
        <input
          id="title"
          type="text"
          value={title}
          onChange={(event) => setTitle(event.target.value)}
          maxLength={300}
          required
          className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
        />
      </div>

      <div>
        <label htmlFor="body" className="block text-sm font-medium text-gray-700">
          Body
        </label>
        <textarea
          id="body"
          value={body}
          onChange={(event) => setBody(event.target.value)}
          required
          rows={8}
          className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
        />
      </div>

      <label className="flex items-center gap-2 text-sm text-gray-700">
        <input
          type="checkbox"
          checked={anonymous}
          onChange={(event) => setAnonymous(event.target.checked)}
        />
        Post anonymously
      </label>

      <button
        type="submit"
        disabled={submitting || tree.length === 0}
        className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
      >
        {submitting ? 'Posting…' : 'Post question'}
      </button>
    </form>
  );
}
