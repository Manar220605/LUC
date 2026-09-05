'use client';

import { FormEvent, useCallback, useEffect, useState } from 'react';
import { useSession } from 'next-auth/react';
import { useRouter } from 'next/navigation';
import { clientApiRequest } from '@/lib/clientApi';
import { getErrorMessage } from '@/lib/apiError';
import type {
  AnnouncementResponseDTO,
  CreateAnnouncementRequestDTO,
  UpdateAnnouncementRequestDTO,
} from '@/lib/types';

export default function AdminAnnouncementsPage() {
  const { status } = useSession();
  const router = useRouter();
  const [items, setItems] = useState<AnnouncementResponseDTO[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const [createTitle, setCreateTitle] = useState('');
  const [createBody, setCreateBody] = useState('');
  const [createPublished, setCreatePublished] = useState(true);

  const [editId, setEditId] = useState<number | null>(null);
  const [editTitle, setEditTitle] = useState('');
  const [editBody, setEditBody] = useState('');
  const [editPublished, setEditPublished] = useState(true);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setItems(
        await clientApiRequest<AnnouncementResponseDTO[]>('/api/admin/announcements'),
      );
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to load announcements'));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (status === 'unauthenticated') {
      router.push('/auth/signin');
      return;
    }
    if (status === 'authenticated') {
      load();
    }
  }, [status, router, load]);

  async function handleCreate(e: FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      const body: CreateAnnouncementRequestDTO = {
        title: createTitle.trim(),
        body: createBody,
        published: createPublished,
      };
      await clientApiRequest('/api/admin/announcements', {
        method: 'POST',
        body: JSON.stringify(body),
      });
      setCreateTitle('');
      setCreateBody('');
      setCreatePublished(true);
      await load();
    } catch (err) {
      setError(getErrorMessage(err, 'Create failed'));
    }
  }

  async function handleUpdate(e: FormEvent) {
    e.preventDefault();
    if (editId == null) {
      return;
    }
    setError(null);
    try {
      const body: UpdateAnnouncementRequestDTO = {
        title: editTitle.trim(),
        body: editBody,
        published: editPublished,
      };
      await clientApiRequest(`/api/admin/announcements/${editId}`, {
        method: 'PUT',
        body: JSON.stringify(body),
      });
      setEditId(null);
      await load();
    } catch (err) {
      setError(getErrorMessage(err, 'Update failed'));
    }
  }

  async function handleDelete(id: number) {
    setError(null);
    try {
      await clientApiRequest(`/api/admin/announcements/${id}`, { method: 'DELETE' });
      if (editId === id) {
        setEditId(null);
      }
      await load();
    } catch (err) {
      setError(getErrorMessage(err, 'Delete failed'));
    }
  }

  function startEdit(a: AnnouncementResponseDTO) {
    setEditId(a.id);
    setEditTitle(a.title);
    setEditBody(a.body);
    setEditPublished(a.published);
  }

  if (status === 'loading' || loading) {
    return (
      <main className="mx-auto max-w-4xl px-4 py-8">
        <p className="text-muted">Loading announcements…</p>
      </main>
    );
  }

  return (
    <main className="mx-auto max-w-4xl px-4 py-8">
      <h1 className="text-2xl font-semibold text-lu-deep">Manage announcements</h1>
      <p className="mt-1 text-sm text-muted">
        Admin only — create, edit, and delete announcements shown on the home page.
      </p>

      {error && (
        <p className="mt-4 rounded border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
          {error}
        </p>
      )}

      <section className="mt-8 rounded-lg border border-lu/10 bg-white p-4">
        <h2 className="text-lg font-medium text-lu-deep">Create announcement</h2>
        <form onSubmit={handleCreate} className="mt-4 grid gap-3">
          <label className="text-sm">
            Title
            <input
              className="mt-1 w-full rounded border border-lu/20 px-2 py-1"
              value={createTitle}
              onChange={(e) => setCreateTitle(e.target.value)}
              maxLength={200}
              required
            />
          </label>
          <label className="text-sm">
            Body
            <textarea
              className="mt-1 w-full rounded border border-lu/20 px-2 py-1"
              rows={5}
              value={createBody}
              onChange={(e) => setCreateBody(e.target.value)}
              maxLength={20000}
              required
            />
          </label>
          <label className="inline-flex items-center gap-2 text-sm">
            <input
              type="checkbox"
              checked={createPublished}
              onChange={(e) => setCreatePublished(e.target.checked)}
            />
            Published (visible to everyone)
          </label>
          <div>
            <button
              type="submit"
              className="rounded bg-lu px-4 py-2 text-sm font-medium text-white hover:bg-lu-dark"
            >
              Create
            </button>
          </div>
        </form>
      </section>

      {editId != null && (
        <section className="mt-6 rounded-lg border border-lu/20 bg-lu-soft p-4">
          <h2 className="text-lg font-medium text-lu-deep">Edit announcement</h2>
          <form onSubmit={handleUpdate} className="mt-4 grid gap-3">
            <label className="text-sm">
              Title
              <input
                className="mt-1 w-full rounded border border-lu/20 px-2 py-1"
                value={editTitle}
                onChange={(e) => setEditTitle(e.target.value)}
                maxLength={200}
                required
              />
            </label>
            <label className="text-sm">
              Body
              <textarea
                className="mt-1 w-full rounded border border-lu/20 px-2 py-1"
                rows={5}
                value={editBody}
                onChange={(e) => setEditBody(e.target.value)}
                maxLength={20000}
                required
              />
            </label>
            <label className="inline-flex items-center gap-2 text-sm">
              <input
                type="checkbox"
                checked={editPublished}
                onChange={(e) => setEditPublished(e.target.checked)}
              />
              Published
            </label>
            <div className="flex gap-2">
              <button
                type="submit"
                className="rounded bg-lu px-4 py-2 text-sm font-medium text-white hover:bg-lu-dark"
              >
                Save
              </button>
              <button
                type="button"
                onClick={() => setEditId(null)}
                className="rounded border border-lu/20 px-4 py-2 text-sm text-ink hover:bg-lu-soft"
              >
                Cancel
              </button>
            </div>
          </form>
        </section>
      )}

      <section className="mt-8">
        <h2 className="text-lg font-medium text-lu-deep">All announcements</h2>
        {items.length === 0 ? (
          <p className="mt-3 text-sm text-muted">No announcements yet.</p>
        ) : (
          <ul className="mt-3 divide-y divide-lu-soft rounded-lg border border-lu/10 bg-white">
            {items.map((a) => (
              <li key={a.id} className="px-4 py-3">
                <div className="flex items-start justify-between gap-4">
                  <div className="min-w-0">
                    <p className="flex items-center gap-2 font-medium text-lu-deep">
                      <span className="truncate">{a.title}</span>
                      {!a.published && (
                        <span className="rounded bg-amber-100 px-1.5 py-0.5 text-xs text-amber-800">
                          Draft
                        </span>
                      )}
                    </p>
                    <p className="mt-1 whitespace-pre-wrap text-sm text-muted">{a.body}</p>
                    <p className="mt-1 text-xs text-muted">
                      {new Date(a.createdAt).toLocaleString()}
                    </p>
                  </div>
                  <div className="flex shrink-0 gap-2">
                    <button
                      type="button"
                      onClick={() => startEdit(a)}
                      className="rounded border border-lu/20 px-3 py-1 text-sm hover:bg-lu-soft"
                    >
                      Edit
                    </button>
                    <button
                      type="button"
                      onClick={() => handleDelete(a.id)}
                      className="rounded border border-red-300 px-3 py-1 text-sm text-red-700 hover:bg-red-50"
                    >
                      Delete
                    </button>
                  </div>
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>
    </main>
  );
}
