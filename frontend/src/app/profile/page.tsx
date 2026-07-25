import { redirect } from 'next/navigation';
import { auth } from '@/lib/auth';
import { api } from '@/lib/api';
import type { UserResponseDTO } from '@/lib/types';

export default async function ProfilePage() {
  const session = await auth();
  if (!session) {
    redirect('/auth/signin');
  }

  const profile = await api.get<UserResponseDTO>('/api/me');

  return (
    <main className="mx-auto max-w-4xl px-4 py-8">
      <h1 className="text-2xl font-semibold text-gray-900">Your profile</h1>
      <dl className="mt-6 divide-y divide-gray-200 rounded-lg border border-gray-200 bg-white">
        <div className="grid grid-cols-3 gap-4 px-4 py-3">
          <dt className="text-sm font-medium text-gray-500">Display name</dt>
          <dd className="col-span-2 text-sm text-gray-900">{profile.displayName}</dd>
        </div>
        <div className="grid grid-cols-3 gap-4 px-4 py-3">
          <dt className="text-sm font-medium text-gray-500">Email</dt>
          <dd className="col-span-2 text-sm text-gray-900">{profile.email}</dd>
        </div>
        <div className="grid grid-cols-3 gap-4 px-4 py-3">
          <dt className="text-sm font-medium text-gray-500">Role</dt>
          <dd className="col-span-2 text-sm text-gray-900">{profile.role}</dd>
        </div>
        <div className="grid grid-cols-3 gap-4 px-4 py-3">
          <dt className="text-sm font-medium text-gray-500">Member since</dt>
          <dd className="col-span-2 text-sm text-gray-900">
            {new Date(profile.createdAt).toLocaleDateString()}
          </dd>
        </div>
      </dl>
    </main>
  );
}
