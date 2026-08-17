'use client';

import { FormEvent, useCallback, useEffect, useState } from 'react';
import { useSession } from 'next-auth/react';
import { useRouter } from 'next/navigation';
import { clientApiRequest } from '@/lib/clientApi';
import { getErrorMessage } from '@/lib/apiError';
import { apiPublicGet } from '@/lib/apiPublic';
import type {
  CommunityTreeNodeDTO,
  CreateCommunityRequestDTO,
  UpdateCommunityRequestDTO,
} from '@/lib/types';

function flattenTree(nodes: CommunityTreeNodeDTO[]): CommunityTreeNodeDTO[] {
  const result: CommunityTreeNodeDTO[] = [];
  for (const node of nodes) {
    result.push(node);
    result.push(...flattenTree(node.children));
  }
  return result;
}

export default function AdminCommunitiesPage() {
  const { status } = useSession();
  const router = useRouter();
  const [tree, setTree] = useState<CommunityTreeNodeDTO[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const [createSlug, setCreateSlug] = useState('');
  const [createName, setCreateName] = useState('');
  const [createDescription, setCreateDescription] = useState('');
  const [createParentPath, setCreateParentPath] = useState('cs');

  const [editId, setEditId] = useState<number | null>(null);
  const [editName, setEditName] = useState('');
  const [editDescription, setEditDescription] = useState('');

  const loadTree = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setTree(await apiPublicGet<CommunityTreeNodeDTO[]>('/api/communities'));
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to load communities'));
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
      loadTree();
    }
  }, [status, router, loadTree]);

  async function handleCreate(e: FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      const body: CreateCommunityRequestDTO = {
        slug: createSlug.trim(),
        name: createName.trim(),
        description: createDescription.trim() || undefined,
        parentPath: createParentPath.trim() || undefined,
      };
      await clientApiRequest('/api/admin/communities', {
        method: 'POST',
        body: JSON.stringify(body),
      });
      setCreateSlug('');
      setCreateName('');
      setCreateDescription('');
      await loadTree();
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
      const body: UpdateCommunityRequestDTO = {
        name: editName.trim(),
        description: editDescription.trim() || undefined,
      };
      await clientApiRequest(`/api/admin/communities/${editId}`, {
        method: 'PUT',
        body: JSON.stringify(body),
      });
      setEditId(null);
      await loadTree();
    } catch (err) {
      setError(getErrorMessage(err, 'Update failed'));
    }
  }

  async function handleDelete(id: number) {
    setError(null);
    try {
      await clientApiRequest(`/api/admin/communities/${id}`, { method: 'DELETE' });
      if (editId === id) {
        setEditId(null);
      }
      await loadTree();
    } catch (err) {
      setError(getErrorMessage(err, 'Delete failed'));
    }
  }

  function startEdit(node: CommunityTreeNodeDTO) {
    setEditId(node.id);
    setEditName(node.name);
    setEditDescription(node.description ?? '');
  }

  const flat = flattenTree(tree);

  if (status === 'loading' || loading) {
    return (
      <main className="mx-auto max-w-4xl px-4 py-8">
        <p className="text-gray-600">Loading communities…</p>
      </main>
    );
  }

  return (
    <main className="mx-auto max-w-4xl px-4 py-8">
      <h1 className="text-2xl font-semibold text-gray-900">Manage communities</h1>
      <p className="mt-1 text-sm text-gray-600">Admin only — create, edit, and delete communities.</p>

      {error && (
        <p className="mt-4 rounded border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
          {error}
        </p>
      )}

      <section className="mt-8 rounded-lg border border-gray-200 bg-white p-4">
        <h2 className="text-lg font-medium text-gray-900">Create community</h2>
        <form onSubmit={handleCreate} className="mt-4 grid gap-3 sm:grid-cols-2">
          <label className="text-sm">
            Slug
            <input
              className="mt-1 w-full rounded border border-gray-300 px-2 py-1"
              value={createSlug}
              onChange={(e) => setCreateSlug(e.target.value)}
              required
            />
          </label>
          <label className="text-sm">
            Name
            <input
              className="mt-1 w-full rounded border border-gray-300 px-2 py-1"
              value={createName}
              onChange={(e) => setCreateName(e.target.value)}
              required
            />
          </label>
          <label className="text-sm sm:col-span-2">
            Parent path
            <select
              className="mt-1 w-full rounded border border-gray-300 px-2 py-1"
              value={createParentPath}
              onChange={(e) => setCreateParentPath(e.target.value)}
            >
              <option value="">(root)</option>
              {flat.map((node) => (
                <option key={node.id} value={node.path}>
                  {node.path}
                </option>
              ))}
            </select>
          </label>
          <label className="text-sm sm:col-span-2">
            Description
            <textarea
              className="mt-1 w-full rounded border border-gray-300 px-2 py-1"
              rows={2}
              value={createDescription}
              onChange={(e) => setCreateDescription(e.target.value)}
            />
          </label>
          <div className="sm:col-span-2">
            <button
              type="submit"
              className="rounded bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
            >
              Create
            </button>
          </div>
        </form>
      </section>

      {editId != null && (
        <section className="mt-6 rounded-lg border border-blue-200 bg-blue-50 p-4">
          <h2 className="text-lg font-medium text-gray-900">Edit community</h2>
          <form onSubmit={handleUpdate} className="mt-4 grid gap-3">
            <label className="text-sm">
              Name
              <input
                className="mt-1 w-full rounded border border-gray-300 px-2 py-1"
                value={editName}
                onChange={(e) => setEditName(e.target.value)}
                required
              />
            </label>
            <label className="text-sm">
              Description
              <textarea
                className="mt-1 w-full rounded border border-gray-300 px-2 py-1"
                rows={2}
                value={editDescription}
                onChange={(e) => setEditDescription(e.target.value)}
              />
            </label>
            <div className="flex gap-2">
              <button
                type="submit"
                className="rounded bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
              >
                Save
              </button>
              <button
                type="button"
                onClick={() => setEditId(null)}
                className="rounded border border-gray-300 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
              >
                Cancel
              </button>
            </div>
          </form>
        </section>
      )}

      <section className="mt-8">
        <h2 className="text-lg font-medium text-gray-900">All communities</h2>
        <ul className="mt-3 divide-y divide-gray-200 rounded-lg border border-gray-200 bg-white">
          {flat.map((node) => (
            <li key={node.id} className="flex items-center justify-between px-4 py-3">
              <div>
                <p className="font-medium text-gray-900">{node.name}</p>
                <p className="text-sm text-gray-500">{node.path}</p>
              </div>
              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={() => startEdit(node)}
                  className="rounded border border-gray-300 px-3 py-1 text-sm hover:bg-gray-50"
                >
                  Edit
                </button>
                <button
                  type="button"
                  onClick={() => handleDelete(node.id)}
                  className="rounded border border-red-300 px-3 py-1 text-sm text-red-700 hover:bg-red-50"
                >
                  Delete
                </button>
              </div>
            </li>
          ))}
        </ul>
      </section>
    </main>
  );
}
