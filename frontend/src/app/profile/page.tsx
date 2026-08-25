import { redirect } from 'next/navigation';
import { auth } from '@/lib/auth';
import { api } from '@/lib/api';
import ProfileSettings from '@/components/user/ProfileSettings';
import type { UserResponseDTO } from '@/lib/types';

export default async function ProfilePage() {
  const session = await auth();
  if (!session) {
    redirect('/auth/signin');
  }

  const profile = await api.get<UserResponseDTO>('/api/me');
  return <ProfileSettings initialProfile={profile} />;
}
