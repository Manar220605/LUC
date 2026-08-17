'use client';

import { FormEvent, useCallback, useEffect, useState } from 'react';
import { useSession } from 'next-auth/react';
import { useRouter } from 'next/navigation';
import { clientApiRequest } from '@/lib/clientApi';
import { getErrorMessage, isApiError } from '@/lib/apiError';
import type { PageResponseDTO, UserRole } from '@/lib/types';

type AdminUserResponseDTO = {
  id: number;
  email: string;
  displayName: string;
  role: UserRole;
  studentId: string | null;
  banned: boolean;
  banReason: string | null;
  createdAt: string;
};

const ROLE_OPTIONS: { value: '' | UserRole; label: string }[] = [
  { value: '', label: 'All roles' },
  { value: 'MEMBER', label: 'Member' },
  { value: 'STUDENT', label: 'Student' },
  { value: 'ALUMNI', label: 'Alumni' },
  { value: 'ADMIN', label: 'Admin' },
];

const BANNED_OPTIONS: { value: '' | 'true' | 'false'; label: string }[] = [
  { value: '', label: 'All' },
  { value: 'false', label: 'Active' },
  { value: 'true', label: 'Banned' },
];

export default function AdminUsersPage() {
  const { status } = useSession();
  const router = useRouter();
  const [items, setItems] = useState<AdminUserResponseDTO[]>([]);
  const [search, setSearch] = useState('');
  const [searchInput, setSearchInput] = useState('');
  const [roleFilter, setRoleFilter] = useState<'' | UserRole>('');
  const [bannedFilter, setBannedFilter] = useState<'' | 'true' | 'false'>('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [forbidden, setForbidden] = useState(false);
  const [banTargetId, setBanTargetId] = useState<number | null>(null);
  const [banReason, setBanReason] = useState('');
  const [roleTargetId, setRoleTargetId] = useState<number | null>(null);
  const [roleTargetCurrent, setRoleTargetCurrent] = useState<UserRole>('MEMBER');
  const [roleSelection, setRoleSelection] = useState<UserRole>('MEMBER');
  const [actionId, setActionId] = useState<number | null>(null);

  const loadUsers = useCallback(async () => {
    setLoading(true);
    setError(null);
    setForbidden(false);
    try {
      const params = new URLSearchParams({ page: String(page), size: '20' });
      if (search.trim()) params.set('search', search.trim());
      if (roleFilter) params.set('role', roleFilter);
      if (bannedFilter) params.set('isBanned', bannedFilter);

      const result = await clientApiRequest<PageResponseDTO<AdminUserResponseDTO>>(
        `/api/admin/users?${params.toString()}`
      );
      setItems(result.content);
      setTotalPages(result.totalPages);
    } catch (err) {
      if (isApiError(err, 401)) {
        router.push('/auth/signin');
        return;
      }
      if (isApiError(err, 403)) {
        setForbidden(true);
        setItems([]);
        return;
      }
      setError(getErrorMessage(err, 'Failed to load users'));
    } finally {
      setLoading(false);
    }
  }, [router, page, search, roleFilter, bannedFilter]);

  useEffect(() => {
    if (status === 'unauthenticated') {
      router.push('/auth/signin');
      return;
    }
    if (status === 'authenticated') {
      loadUsers();
    }
  }, [status, router, loadUsers]);

  function handleSearch(event: FormEvent) {
    event.preventDefault();
    setPage(0);
    setSearch(searchInput);
  }

  async function submitBan(event: FormEvent) {
    event.preventDefault();
    if (banTargetId == null || !banReason.trim()) return;
    setActionId(banTargetId);
    try {
      await clientApiRequest(`/api/admin/users/${banTargetId}/ban`, {
        method: 'PUT',
        body: JSON.stringify({ reason: banReason.trim() }),
      });
      setBanTargetId(null);
      setBanReason('');
      await loadUsers();
    } catch (err) {
      setError(getErrorMessage(err, 'Ban failed'));
    } finally {
      setActionId(null);
    }
  }

  async function submitRoleChange(event: FormEvent) {
    event.preventDefault();
    if (roleTargetId == null || roleSelection === roleTargetCurrent) return;
    setActionId(roleTargetId);
    try {
      await clientApiRequest(`/api/admin/users/${roleTargetId}/role`, {
        method: 'PUT',
        body: JSON.stringify({ role: roleSelection }),
      });
      setRoleTargetId(null);
      await loadUsers();
    } catch (err) {
      setError(getErrorMessage(err, 'Role change failed'));
    } finally {
      setActionId(null);
    }
  }

  async function unban(userId: number) {
    if (!window.confirm('Unban this user?')) return;
    setActionId(userId);
    try {
      await clientApiRequest(`/api/admin/users/${userId}/unban`, { method: 'PUT' });
      await loadUsers();
    } catch (err) {
      setError(getErrorMessage(err, 'Unban failed'));
    } finally {
      setActionId(null);
    }
  }

  if (status === 'loading' || loading) {
    return (
      <main className="mx-auto max-w-5xl px-4 py-8">
        <p className="text-gray-600">Loading users…</p>
      </main>
    );
  }

  if (forbidden) {
    return (
      <main className="mx-auto max-w-5xl px-4 py-8">
        <h1 className="text-2xl font-semibold text-gray-900">Users</h1>
        <p className="mt-4 rounded border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-800">
          You do not have permission to view this page.
        </p>
      </main>
    );
  }

  return (
    <main className="mx-auto max-w-5xl px-4 py-8">
      <h1 className="text-2xl font-semibold text-gray-900">Users</h1>
      <p className="mt-1 text-sm text-gray-600">Search, filter, and moderate platform users.</p>

      {error && (
        <p className="mt-4 rounded border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
          {error}
        </p>
      )}

      <form onSubmit={handleSearch} className="mt-6 flex flex-wrap items-end gap-3">
        <div className="grow">
          <label className="block text-xs font-medium text-gray-600">Search</label>
          <input
            type="text"
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            placeholder="Email or display name…"
            className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
          />
        </div>
        <div>
          <label className="block text-xs font-medium text-gray-600">Role</label>
          <select
            value={roleFilter}
            onChange={(e) => {
              setRoleFilter(e.target.value as '' | UserRole);
              setPage(0);
            }}
            className="mt-1 rounded-md border border-gray-300 px-3 py-2 text-sm"
          >
            {ROLE_OPTIONS.map((o) => (
              <option key={o.value} value={o.value}>
                {o.label}
              </option>
            ))}
          </select>
        </div>
        <div>
          <label className="block text-xs font-medium text-gray-600">Status</label>
          <select
            value={bannedFilter}
            onChange={(e) => {
              setBannedFilter(e.target.value as '' | 'true' | 'false');
              setPage(0);
            }}
            className="mt-1 rounded-md border border-gray-300 px-3 py-2 text-sm"
          >
            {BANNED_OPTIONS.map((o) => (
              <option key={o.value} value={o.value}>
                {o.label}
              </option>
            ))}
          </select>
        </div>
        <button
          type="submit"
          className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
        >
          Apply
        </button>
      </form>

      <div className="mt-6 overflow-hidden rounded-lg border border-gray-200 bg-white">
        <table className="min-w-full divide-y divide-gray-200 text-sm">
          <thead className="bg-gray-50 text-left text-xs font-medium uppercase text-gray-500">
            <tr>
              <th className="px-4 py-3">User</th>
              <th className="px-4 py-3">Role</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3">Created</th>
              <th className="px-4 py-3 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {items.length === 0 ? (
              <tr>
                <td colSpan={5} className="px-4 py-6 text-center text-sm text-gray-500">
                  No users match the current filters.
                </td>
              </tr>
            ) : (
              items.map((user) => (
                <tr key={user.id}>
                  <td className="px-4 py-3">
                    <div className="font-medium text-gray-900">{user.displayName}</div>
                    <div className="text-xs text-gray-500">{user.email}</div>
                  </td>
                  <td className="px-4 py-3">
                    <span className="rounded bg-gray-100 px-2 py-0.5 text-xs font-medium text-gray-700">
                      {user.role}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    {user.banned ? (
                      <div>
                        <span className="rounded bg-red-100 px-2 py-0.5 text-xs font-medium text-red-700">
                          Banned
                        </span>
                        {user.banReason && (
                          <div className="mt-1 text-xs text-gray-500">{user.banReason}</div>
                        )}
                      </div>
                    ) : (
                      <span className="rounded bg-emerald-100 px-2 py-0.5 text-xs font-medium text-emerald-700">
                        Active
                      </span>
                    )}
                  </td>
                  <td className="px-4 py-3 text-xs text-gray-500">
                    {new Date(user.createdAt).toLocaleDateString()}
                  </td>
                  <td className="px-4 py-3 text-right">
                    <div className="flex justify-end gap-2">
                      <button
                        type="button"
                        onClick={() => {
                          setRoleTargetId(user.id);
                          setRoleTargetCurrent(user.role);
                          setRoleSelection(user.role);
                        }}
                        disabled={actionId === user.id}
                        className="rounded-md border border-gray-300 px-3 py-1 text-xs font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50"
                      >
                        Change role
                      </button>
                      {user.banned ? (
                        <button
                          type="button"
                          onClick={() => unban(user.id)}
                          disabled={actionId === user.id}
                          className="rounded-md border border-emerald-300 px-3 py-1 text-xs font-medium text-emerald-700 hover:bg-emerald-50 disabled:opacity-50"
                        >
                          Unban
                        </button>
                      ) : (
                        <button
                          type="button"
                          onClick={() => {
                            setBanTargetId(user.id);
                            setBanReason('');
                          }}
                          disabled={actionId === user.id}
                          className="rounded-md border border-red-300 px-3 py-1 text-xs font-medium text-red-700 hover:bg-red-50 disabled:opacity-50"
                        >
                          Ban
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {totalPages > 1 && (
        <div className="mt-4 flex items-center justify-between text-sm">
          <button
            type="button"
            disabled={page === 0}
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            className="rounded-md border border-gray-300 px-3 py-1 text-gray-700 hover:bg-gray-50 disabled:opacity-50"
          >
            Previous
          </button>
          <span className="text-gray-500">
            Page {page + 1} of {totalPages}
          </span>
          <button
            type="button"
            disabled={page + 1 >= totalPages}
            onClick={() => setPage((p) => p + 1)}
            className="rounded-md border border-gray-300 px-3 py-1 text-gray-700 hover:bg-gray-50 disabled:opacity-50"
          >
            Next
          </button>
        </div>
      )}

      {roleTargetId != null && (
        <div className="fixed inset-0 z-20 flex items-center justify-center bg-gray-900/50 px-4">
          <form
            onSubmit={submitRoleChange}
            className="w-full max-w-md rounded-lg bg-white p-6 shadow-lg"
          >
            <h2 className="text-lg font-semibold text-gray-900">Change role</h2>
            <p className="mt-1 text-sm text-gray-600">
              This updates the user&apos;s role locally and syncs the Keycloak realm role.
            </p>
            <label className="mt-4 block text-sm font-medium text-gray-700">New role</label>
            <select
              value={roleSelection}
              onChange={(e) => setRoleSelection(e.target.value as UserRole)}
              className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
            >
              {ROLE_OPTIONS.filter((o) => o.value !== '').map((o) => (
                <option key={o.value} value={o.value}>
                  {o.label}
                </option>
              ))}
            </select>
            <div className="mt-4 flex justify-end gap-2">
              <button
                type="button"
                onClick={() => setRoleTargetId(null)}
                className="rounded-md border border-gray-300 px-3 py-2 text-sm text-gray-700 hover:bg-gray-50"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={roleSelection === roleTargetCurrent || actionId === roleTargetId}
                className="rounded-md bg-blue-600 px-3 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
              >
                Save
              </button>
            </div>
          </form>
        </div>
      )}

      {banTargetId != null && (
        <div className="fixed inset-0 z-20 flex items-center justify-center bg-gray-900/50 px-4">
          <form
            onSubmit={submitBan}
            className="w-full max-w-md rounded-lg bg-white p-6 shadow-lg"
          >
            <h2 className="text-lg font-semibold text-gray-900">Ban user</h2>
            <p className="mt-1 text-sm text-gray-600">
              Provide a reason. This will be visible to the user and other admins.
            </p>
            <textarea
              value={banReason}
              onChange={(e) => setBanReason(e.target.value)}
              required
              rows={4}
              className="mt-4 w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
              placeholder="Reason for ban…"
            />
            <div className="mt-4 flex justify-end gap-2">
              <button
                type="button"
                onClick={() => {
                  setBanTargetId(null);
                  setBanReason('');
                }}
                className="rounded-md border border-gray-300 px-3 py-2 text-sm text-gray-700 hover:bg-gray-50"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={!banReason.trim() || actionId === banTargetId}
                className="rounded-md bg-red-600 px-3 py-2 text-sm font-medium text-white hover:bg-red-700 disabled:opacity-50"
              >
                Confirm ban
              </button>
            </div>
          </form>
        </div>
      )}
    </main>
  );
}
