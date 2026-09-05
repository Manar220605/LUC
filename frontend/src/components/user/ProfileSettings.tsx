'use client';

import { FormEvent, useRef, useState } from 'react';
import Link from 'next/link';
import Avatar from '@/components/user/Avatar';
import { clientApiRequest, clientApiUpload } from '@/lib/clientApi';
import { getErrorMessage } from '@/lib/apiError';
import type { UserResponseDTO } from '@/lib/types';

const MAX_BYTES = 2 * 1024 * 1024;

type Props = {
  initialProfile: UserResponseDTO;
};

export default function ProfileSettings({ initialProfile }: Props) {
  const [profile, setProfile] = useState(initialProfile);
  const [displayName, setDisplayName] = useState(initialProfile.displayName);
  const [bio, setBio] = useState(initialProfile.bio ?? '');
  const [saving, setSaving] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [removing, setRemoving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saved, setSaved] = useState(false);
  const [previewSrc, setPreviewSrc] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  async function handleSave(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setSaved(false);
    const name = displayName.trim();
    if (!name) {
      setError('Display name is required');
      return;
    }
    setSaving(true);
    try {
      const updated = await clientApiRequest<UserResponseDTO>('/api/me', {
        method: 'PUT',
        body: JSON.stringify({ displayName: name, bio: bio.trim() }),
      });
      setProfile(updated);
      setDisplayName(updated.displayName);
      setBio(updated.bio ?? '');
      setSaved(true);
    } catch (err) {
      setError(getErrorMessage(err, 'Could not save your profile'));
    } finally {
      setSaving(false);
    }
  }

  async function handlePhotoChange(file: File | undefined) {
    if (!file) {
      return;
    }
    setError(null);
    setSaved(false);
    if (file.size > MAX_BYTES) {
      setError('Photo is too large. Maximum size is 2 MB.');
      return;
    }
    setUploading(true);
    const localPreview = URL.createObjectURL(file);
    setPreviewSrc(localPreview);
    try {
      const updated = await clientApiUpload<UserResponseDTO>('/api/me/avatar', file);
      setProfile(updated);
    } catch (err) {
      setError(getErrorMessage(err, 'Could not upload the photo'));
    } finally {
      URL.revokeObjectURL(localPreview);
      setPreviewSrc(null);
      setUploading(false);
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  }

  async function handleRemovePhoto() {
    setError(null);
    setSaved(false);
    setRemoving(true);
    try {
      await clientApiRequest<void>('/api/me/avatar', { method: 'DELETE' });
      setProfile((current) => ({ ...current, avatarUrl: null }));
    } catch (err) {
      setError(getErrorMessage(err, 'Could not remove the photo'));
    } finally {
      setRemoving(false);
    }
  }

  return (
    <main className="mx-auto max-w-4xl px-4 py-8">
      <h1 className="text-2xl font-semibold text-lu-deep">Your profile</h1>
      <p className="mt-2 text-sm text-muted">
        <Link href={`/profile/${profile.id}`} className="font-medium text-lu hover:text-lu-dark">
          View public profile
        </Link>
      </p>

      <section className="mt-6 rounded-lg border border-lu/10 bg-white p-6">
        <h2 className="text-lg font-medium text-lu-deep">Photo</h2>
        <p className="mt-1 text-sm text-muted">JPEG, PNG, or WebP. Maximum 2 MB.</p>
        <div className="mt-4 flex flex-wrap items-center gap-4">
          <Avatar
            name={displayName || profile.displayName}
            avatarUrl={profile.avatarUrl}
            previewSrc={previewSrc}
          />
          <div className="flex flex-wrap gap-2">
            <label className="cursor-pointer rounded-md bg-lu px-3 py-1.5 text-sm font-medium text-white hover:bg-lu-dark">
              {uploading ? 'Uploading…' : 'Upload photo'}
              <input
                ref={fileInputRef}
                type="file"
                accept="image/jpeg,image/png,image/webp"
                className="hidden"
                disabled={uploading || removing}
                onChange={(event) => handlePhotoChange(event.target.files?.[0])}
              />
            </label>
            {profile.avatarUrl && (
              <button
                type="button"
                onClick={handleRemovePhoto}
                disabled={uploading || removing}
                className="rounded-md border border-lu/20 px-3 py-1.5 text-sm font-medium text-ink hover:bg-lu-soft disabled:opacity-50"
              >
                {removing ? 'Removing…' : 'Remove photo'}
              </button>
            )}
          </div>
        </div>
      </section>

      <form onSubmit={handleSave} className="mt-6 rounded-lg border border-lu/10 bg-white p-6">
        <h2 className="text-lg font-medium text-lu-deep">Details</h2>
        <label className="mt-4 block text-sm font-medium text-ink">
          Display name
          <input
            className="mt-1 w-full rounded-md border border-lu/20 px-3 py-2 text-sm text-lu-deep"
            value={displayName}
            maxLength={100}
            onChange={(event) => setDisplayName(event.target.value)}
            required
          />
        </label>
        <label className="mt-4 block text-sm font-medium text-ink">
          Bio
          <textarea
            className="mt-1 w-full rounded-md border border-lu/20 px-3 py-2 text-sm text-lu-deep"
            rows={5}
            maxLength={5000}
            value={bio}
            onChange={(event) => setBio(event.target.value)}
            placeholder="A short introduction. This appears on your public profile."
          />
        </label>
        <p className="mt-1 text-xs text-muted">{bio.length}/5000</p>

        {error && <p className="mt-4 text-sm text-red-600">{error}</p>}
        {saved && <p className="mt-4 text-sm text-green-700">Saved.</p>}

        <button
          type="submit"
          disabled={saving}
          className="mt-4 rounded-md bg-lu px-4 py-2 text-sm font-medium text-white hover:bg-lu-dark disabled:opacity-50"
        >
          {saving ? 'Saving…' : 'Save'}
        </button>
      </form>

      <dl className="mt-6 divide-y divide-lu-soft rounded-lg border border-lu/10 bg-white">
        <div className="grid grid-cols-3 gap-4 px-4 py-3">
          <dt className="text-sm font-medium text-muted">Email</dt>
          <dd className="col-span-2 text-sm text-lu-deep">{profile.email}</dd>
        </div>
        <div className="grid grid-cols-3 gap-4 px-4 py-3">
          <dt className="text-sm font-medium text-muted">Role</dt>
          <dd className="col-span-2 text-sm text-lu-deep">{profile.role}</dd>
        </div>
        <div className="grid grid-cols-3 gap-4 px-4 py-3">
          <dt className="text-sm font-medium text-muted">Member since</dt>
          <dd className="col-span-2 text-sm text-lu-deep">
            {new Date(profile.createdAt).toLocaleDateString()}
          </dd>
        </div>
      </dl>
    </main>
  );
}
