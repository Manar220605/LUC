'use client';

import { FormEvent, useEffect, useState } from 'react';
import { getSession } from 'next-auth/react';
import { useRouter } from 'next/navigation';
import { ApiError, getErrorMessage } from '@/lib/apiError';
import { clientApiRequest } from '@/lib/clientApi';
import CommunityPicker from '@/components/community/CommunityPicker';
import { apiPublicGet } from '@/lib/apiPublic';
import type {
  CommunityTreeNodeDTO,
  CreateQuestionRequestDTO,
} from '@/lib/types';
import { alertError, btnPrimary, input, label } from '@/lib/ui';

type Props = {
  initialCommunityPath?: string;
  initialCourseCode?: string;
};

function pathExists(nodes: CommunityTreeNodeDTO[], path: string): boolean {
  for (const node of nodes) {
    if (node.path === path) {
      return true;
    }
    if (node.children.length > 0 && pathExists(node.children, path)) {
      return true;
    }
  }
  return false;
}

function communityName(nodes: CommunityTreeNodeDTO[], path: string): string | null {
  for (const node of nodes) {
    if (node.path === path) {
      return node.name;
    }
    if (node.children.length > 0) {
      const nested = communityName(node.children, path);
      if (nested) {
        return nested;
      }
    }
  }
  return null;
}

export default function NewQuestionForm({ initialCommunityPath, initialCourseCode }: Props) {
  const router = useRouter();
  const [tree, setTree] = useState<CommunityTreeNodeDTO[]>([]);
  const [communityPath, setCommunityPath] = useState(initialCommunityPath?.trim() || 'cs');
  const [courseCode] = useState(initialCourseCode?.trim() || '');
  const [title, setTitle] = useState('');
  const [body, setBody] = useState('');
  const [anonymous, setAnonymous] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const lockedPath = initialCommunityPath?.trim() || '';
  const lockedName = lockedPath ? communityName(tree, lockedPath) : null;
  const communityLocked = Boolean(lockedPath && (tree.length === 0 || lockedName));

  useEffect(() => {
    apiPublicGet<CommunityTreeNodeDTO[]>('/api/communities')
      .then((data) => {
        setTree(data);
        const preferred = initialCommunityPath?.trim();
        if (preferred && pathExists(data, preferred)) {
          setCommunityPath(preferred);
        } else if (data.length > 0) {
          setCommunityPath(data[0].path);
        }
      })
      .catch(() => setError('Failed to load communities'));
  }, [initialCommunityPath]);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);

    try {
      const session = await getSession();
      if (session?.error === 'RefreshAccessTokenError') {
        throw new ApiError(401, 'Session expired; please sign in again');
      }
      if (!session?.accessToken) {
        throw new ApiError(401, 'You must be signed in to post a question');
      }

      const payload: CreateQuestionRequestDTO = {
        communityPath: lockedPath || communityPath,
        title: title.trim(),
        body: body.trim(),
        anonymous,
        ...(courseCode ? { courseCode } : {}),
      };

      const created = await clientApiRequest<{ id: number }>('/api/questions', {
        method: 'POST',
        body: JSON.stringify(payload),
      });

      router.push(`/questions/${created.id}`);
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to create question'));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {error && <p className={alertError}>{error}</p>}

      <div>
        <label htmlFor="community" className={label}>
          Community
        </label>
        {communityLocked ? (
          <p className="mt-1 rounded-lg border border-lu/10 bg-lu-mist px-3 py-2 text-sm text-ink">
            {lockedName ? `${lockedName} (c/${lockedPath})` : `c/${lockedPath}`}
          </p>
        ) : tree.length > 0 ? (
          <CommunityPicker nodes={tree} value={communityPath} onChange={setCommunityPath} />
        ) : (
          <p className="mt-1 text-sm text-muted">Loading communities…</p>
        )}
      </div>

      {courseCode && (
        <div>
          <p className={label}>Course</p>
          <p className="mt-1 rounded-lg border border-lu/10 bg-lu-mist px-3 py-2 text-sm font-mono text-lu">
            {courseCode}
          </p>
        </div>
      )}

      <div>
        <label htmlFor="title" className={label}>
          Title
        </label>
        <input
          id="title"
          type="text"
          value={title}
          onChange={(event) => setTitle(event.target.value)}
          maxLength={300}
          required
          className={input}
        />
      </div>

      <div>
        <label htmlFor="body" className={label}>
          Body
        </label>
        <textarea
          id="body"
          value={body}
          onChange={(event) => setBody(event.target.value)}
          required
          rows={8}
          className={input}
        />
      </div>

      <label className="flex items-center gap-2 text-sm text-ink">
        <input
          type="checkbox"
          checked={anonymous}
          onChange={(event) => setAnonymous(event.target.checked)}
        />
        Post anonymously
      </label>

      <button type="submit" disabled={submitting || tree.length === 0} className={btnPrimary}>
        {submitting ? 'Posting…' : 'Post question'}
      </button>
    </form>
  );
}
