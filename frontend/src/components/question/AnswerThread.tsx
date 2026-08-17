'use client';

import { FormEvent, useCallback, useEffect, useRef, useState } from 'react';
import { getSession, signIn, useSession } from 'next-auth/react';
import { clientApiRequest } from '@/lib/clientApi';
import { ApiError, getErrorMessage } from '@/lib/apiError';
import VoteControls from '@/components/vote/VoteControls';
import MarkdownContent from '@/components/markdown/MarkdownContent';
import ReportButton from '@/components/moderation/ReportButton';
import AuthorBadge from '@/components/user/AuthorBadge';
import type {
  AnswerResponseDTO,
  AnswerTreeNodeDTO,
  CreateAnswerRequestDTO,
  QuestionResponseDTO,
  UpdateAnswerRequestDTO,
} from '@/lib/types';

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080';

type Props = {
  questionId: string;
  answerCount: number;
  questionOwnedByCurrentUser?: boolean;
  onAnswersChanged?: () => Promise<void>;
  onAcceptedAnswerChange?: (acceptedAnswerId: number | null) => void;
};

type OnAnswersChanged = (
  ownedAnswer?: AnswerResponseDTO,
  removedAnswerId?: number
) => Promise<void>;

async function authFetch<T>(path: string, init: RequestInit = {}): Promise<T | undefined> {
  const session = await getSession();
  if (session?.error === 'RefreshAccessTokenError') {
    throw new ApiError(401, 'Session expired; please sign in again');
  }
  if (!session?.accessToken) {
    throw new ApiError(401, 'You must be signed in');
  }

  return clientApiRequest<T>(path, init);
}

function normalizeAnswerTree(nodes: AnswerTreeNodeDTO[]): AnswerTreeNodeDTO[] {
  return nodes.map((node) => ({
    ...node,
    ownedByCurrentUser: node.ownedByCurrentUser === true,
    replies: normalizeAnswerTree(node.replies ?? []),
  }));
}

function syncOwnedIdsFromTree(nodes: AnswerTreeNodeDTO[], ownedIds: Set<number>) {
  for (const node of nodes) {
    if (node.ownedByCurrentUser) {
      ownedIds.add(node.id);
    }
    syncOwnedIdsFromTree(node.replies, ownedIds);
  }
}

function applyPersistedOwnership(
  nodes: AnswerTreeNodeDTO[],
  ownedIds: Set<number>
): AnswerTreeNodeDTO[] {
  return nodes.map((node) => ({
    ...node,
    ownedByCurrentUser:
      !node.deleted && (node.ownedByCurrentUser || ownedIds.has(node.id)),
    replies: applyPersistedOwnership(node.replies, ownedIds),
  }));
}

function patchAnswerInTree(
  nodes: AnswerTreeNodeDTO[],
  updated: AnswerResponseDTO
): AnswerTreeNodeDTO[] {
  return nodes.map((node) => {
    if (node.id === updated.id) {
      return {
        ...node,
        body: updated.body,
        anonymous: updated.anonymous,
        deleted: updated.deleted,
        updatedAt: updated.updatedAt,
        ownedByCurrentUser: !updated.deleted,
      };
    }
    return {
      ...node,
      replies: patchAnswerInTree(node.replies, updated),
    };
  });
}

function patchDeletedAnswerInTree(
  nodes: AnswerTreeNodeDTO[],
  answerId: number
): AnswerTreeNodeDTO[] {
  return nodes.map((node) => {
    if (node.id === answerId) {
      return {
        ...node,
        body: '[deleted]',
        author: { displayName: '[deleted]' },
        deleted: true,
        ownedByCurrentUser: false,
        replies: node.replies,
      };
    }
    return {
      ...node,
      replies: patchDeletedAnswerInTree(node.replies, answerId),
    };
  });
}

function countAnswerNodes(nodes: AnswerTreeNodeDTO[]): number {
  return nodes.reduce((total, node) => total + 1 + countAnswerNodes(node.replies), 0);
}

function patchVoteInTree(
  nodes: AnswerTreeNodeDTO[],
  answerId: number,
  score: number,
  viewerVote: number | null
): AnswerTreeNodeDTO[] {
  return nodes.map((node) => {
    if (node.id === answerId) {
      return { ...node, score, viewerVote };
    }
    return {
      ...node,
      replies: patchVoteInTree(node.replies, answerId, score, viewerVote),
    };
  });
}

function finalizeAnswerTree(
  nodes: AnswerTreeNodeDTO[],
  ownedIds: Set<number>
): AnswerTreeNodeDTO[] {
  syncOwnedIdsFromTree(nodes, ownedIds);
  return applyPersistedOwnership(normalizeAnswerTree(nodes), ownedIds);
}

async function fetchAnswers(
  questionId: string,
  accessToken?: string
): Promise<AnswerTreeNodeDTO[]> {
  const headers: HeadersInit = {};
  if (accessToken) {
    headers.Authorization = `Bearer ${accessToken}`;
  }

  const res = await fetch(`${API_URL}/api/questions/${questionId}/answers`, {
    headers,
    cache: 'no-store',
  });
  if (!res.ok) {
    throw await ApiError.fromResponse(res);
  }
  return res.json();
}

async function resolveAccessToken(): Promise<string | undefined> {
  const session = await getSession();
  if (session?.error === 'RefreshAccessTokenError' || !session?.accessToken) {
    return undefined;
  }
  return session.accessToken;
}

function treeContainsId(nodes: AnswerTreeNodeDTO[], answerId: number): boolean {
  return nodes.some(
    (node) => node.id === answerId || treeContainsId(node.replies, answerId)
  );
}

function treeHasOwnership(nodes: AnswerTreeNodeDTO[]): boolean {
  return nodes.some(
    (node) => node.ownedByCurrentUser || treeHasOwnership(node.replies)
  );
}

function collectOwnedAnswerIds(nodes: AnswerTreeNodeDTO[], ownedIds = new Set<number>()): Set<number> {
  for (const node of nodes) {
    if (node.ownedByCurrentUser) {
      ownedIds.add(node.id);
    }
    collectOwnedAnswerIds(node.replies, ownedIds);
  }
  return ownedIds;
}

function mergeOwnershipIntoTree(
  current: AnswerTreeNodeDTO[],
  next: AnswerTreeNodeDTO[],
  ownedIds: Set<number>
): AnswerTreeNodeDTO[] {
  collectOwnedAnswerIds(current, ownedIds);
  collectOwnedAnswerIds(next, ownedIds);

  function apply(nodes: AnswerTreeNodeDTO[]): AnswerTreeNodeDTO[] {
    return nodes.map((node) => ({
      ...node,
      ownedByCurrentUser:
        !node.deleted && (node.ownedByCurrentUser || ownedIds.has(node.id)),
      replies: apply(node.replies),
    }));
  }

  return apply(next);
}

function shouldReplaceAnswerTree(
  current: AnswerTreeNodeDTO[],
  next: AnswerTreeNodeDTO[],
  usedAuth: boolean,
  ownedIds: Set<number>
): AnswerTreeNodeDTO[] {
  if (usedAuth || ownedIds.size > 0 || treeHasOwnership(current)) {
    return mergeOwnershipIntoTree(current, next, ownedIds);
  }
  if (treeHasOwnership(current) && !treeHasOwnership(next)) {
    return current;
  }
  return next;
}

function AnswerComposer({
  label,
  initialBody = '',
  initialAnonymous = false,
  onSubmit,
  onCancel,
}: {
  label: string;
  initialBody?: string;
  initialAnonymous?: boolean;
  onSubmit: (body: string, anonymous: boolean) => Promise<void>;
  onCancel?: () => void;
}) {
  const [body, setBody] = useState(initialBody);
  const [anonymous, setAnonymous] = useState(initialAnonymous);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await onSubmit(body.trim(), anonymous);
      setBody('');
      setAnonymous(false);
    } catch (err) {
      setError(getErrorMessage(err, 'Request failed'));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="mt-3 space-y-2 rounded-md border border-gray-200 bg-gray-50 p-3">
      <label className="block text-sm font-medium text-gray-700">{label}</label>
      <textarea
        value={body}
        onChange={(event) => setBody(event.target.value)}
        rows={3}
        required
        className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
        placeholder="Write your answer…"
      />
      <label className="flex items-center gap-2 text-sm text-gray-600">
        <input
          type="checkbox"
          checked={anonymous}
          onChange={(event) => setAnonymous(event.target.checked)}
        />
        Post anonymously
      </label>
      {error && <p className="text-sm text-red-600">{error}</p>}
      <div className="flex gap-2">
        <button
          type="submit"
          disabled={submitting || !body.trim()}
          className="rounded-md bg-blue-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
        >
          {submitting ? 'Submitting…' : 'Submit'}
        </button>
        {onCancel && (
          <button
            type="button"
            onClick={onCancel}
            className="rounded-md border border-gray-300 px-3 py-1.5 text-sm text-gray-700 hover:bg-gray-100"
          >
            Cancel
          </button>
        )}
      </div>
    </form>
  );
}

function AnswerNode({
  answer,
  depth,
  questionId,
  questionOwnedByCurrentUser,
  ownedAnswerIds,
  onChanged,
  onSignInRequired,
  onVoteChange,
  onAcceptedAnswerChange,
}: {
  answer: AnswerTreeNodeDTO;
  depth: number;
  questionId: string;
  questionOwnedByCurrentUser: boolean;
  ownedAnswerIds: ReadonlySet<number>;
  onChanged: OnAnswersChanged;
  onSignInRequired: () => void;
  onVoteChange: (answerId: number, score: number, viewerVote: number | null) => void;
  onAcceptedAnswerChange?: (acceptedAnswerId: number | null) => void;
}) {
  const [collapsed, setCollapsed] = useState(false);
  const [replyOpen, setReplyOpen] = useState(false);
  const [editOpen, setEditOpen] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const isOwner = answer.ownedByCurrentUser || ownedAnswerIds.has(answer.id);
  const hasReplies = answer.replies.length > 0;

  async function handleReply(body: string, anonymous: boolean) {
    const payload: CreateAnswerRequestDTO = { body, anonymous };
    const created = await authFetch<AnswerResponseDTO>(`/api/answers/${answer.id}/replies`, {
      method: 'POST',
      body: JSON.stringify(payload),
    });
    setReplyOpen(false);
    await onChanged(created);
  }

  async function handleEdit(body: string, anonymous: boolean) {
    const payload: UpdateAnswerRequestDTO = { body, anonymous };
    const updated = await authFetch<AnswerResponseDTO>(`/api/answers/${answer.id}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    });
    setEditOpen(false);
    await onChanged(updated);
  }

  async function handleDelete() {
    if (!window.confirm('Delete this answer? Replies will be preserved.')) {
      return;
    }
    setError(null);
    try {
      await authFetch<void>(`/api/answers/${answer.id}`, { method: 'DELETE' });
      if (answer.accepted) {
        onAcceptedAnswerChange?.(null);
      }
      await onChanged(undefined, answer.id);
    } catch (err) {
      setError(getErrorMessage(err, 'Delete failed'));
    }
  }

  async function handleAccept() {
    setError(null);
    try {
      await authFetch<QuestionResponseDTO>(
        `/api/questions/${questionId}/accepted-answer/${answer.id}`,
        { method: 'PUT' }
      );
      onAcceptedAnswerChange?.(answer.id);
      await onChanged();
    } catch (err) {
      setError(getErrorMessage(err, 'Could not accept answer'));
    }
  }

  async function handleUnaccept() {
    setError(null);
    try {
      await authFetch<QuestionResponseDTO>(
        `/api/questions/${questionId}/accepted-answer`,
        { method: 'DELETE' }
      );
      onAcceptedAnswerChange?.(null);
      await onChanged();
    } catch (err) {
      setError(getErrorMessage(err, 'Could not remove accepted answer'));
    }
  }

  async function handleReplyClick() {
    const session = await getSession();
    if (!session?.accessToken || session.error === 'RefreshAccessTokenError') {
      onSignInRequired();
      return;
    }
    setReplyOpen((open) => !open);
  }

  return (
    <div
      className="border-l border-gray-200 pl-4"
      style={{ marginLeft: depth > 0 ? '0.75rem' : undefined }}
    >
      <article
        className={`rounded-lg border bg-white p-4 ${
          answer.accepted
            ? 'border-green-500 bg-green-50 ring-1 ring-green-200'
            : 'border-gray-200'
        }`}
      >
        <div className="flex items-start gap-3">
          {!answer.deleted && (
            <VoteControls
              targetType="ANSWER"
              targetId={answer.id}
              score={answer.score}
              viewerVote={answer.viewerVote}
              disabled={isOwner}
              onVoteChange={(score, viewerVote) => onVoteChange(answer.id, score, viewerVote)}
            />
          )}
          <div className="min-w-0 flex-1">
            {answer.accepted && !answer.deleted && (
              <p className="mb-2 inline-flex items-center gap-1 rounded-full bg-green-100 px-2.5 py-0.5 text-xs font-semibold text-green-800">
                <span aria-hidden="true">✓</span>
                Accepted answer
              </p>
            )}
            <div className="flex flex-wrap items-center gap-x-3 gap-y-1 text-sm text-gray-500">
              <span className="flex flex-wrap items-center gap-2 font-medium text-gray-700">
                <span>{answer.author.displayName}</span>
                <AuthorBadge author={answer.author} />
              </span>
              <span>{new Date(answer.createdAt).toLocaleString()}</span>
              {answer.deleted && <span>{answer.score} score</span>}
            </div>

            {editOpen && isOwner ? (
              <AnswerComposer
                key={`edit-${answer.id}-${answer.updatedAt}`}
                label="Edit answer"
                initialBody={answer.body}
                initialAnonymous={answer.anonymous}
                onSubmit={handleEdit}
                onCancel={() => setEditOpen(false)}
              />
            ) : (
              <div className="mt-2">
                <MarkdownContent content={answer.body} />
              </div>
            )}

            {error && <p className="mt-2 text-sm text-red-600">{error}</p>}

            <div className="mt-3 flex flex-wrap items-center gap-3 text-sm">
          {!answer.deleted && (
            <button
              type="button"
              onClick={handleReplyClick}
              className="font-medium text-blue-600 hover:text-blue-800"
            >
              Reply
            </button>
          )}
          {!answer.deleted && (
            <>
              <ReportButton targetType="ANSWER" targetId={answer.id} />
              {answer.author.id != null && (
                <ReportButton
                  targetType="USER"
                  targetId={answer.author.id}
                  label="Report user"
                />
              )}
            </>
          )}
          {isOwner && !editOpen && !answer.deleted && (
            <>
              <button
                type="button"
                onClick={() => setEditOpen(true)}
                className="font-medium text-gray-600 hover:text-gray-900"
              >
                Edit
              </button>
              <button
                type="button"
                onClick={handleDelete}
                className="font-medium text-red-600 hover:text-red-800"
              >
                Delete
              </button>
            </>
          )}
          {questionOwnedByCurrentUser && depth === 0 && !answer.deleted && (
            answer.accepted ? (
              <button
                type="button"
                onClick={handleUnaccept}
                className="font-medium text-green-700 hover:text-green-900"
              >
                Unaccept
              </button>
            ) : (
              <button
                type="button"
                onClick={handleAccept}
                className="font-medium text-green-700 hover:text-green-900"
              >
                Accept answer
              </button>
            )
          )}
          {hasReplies && (
            <button
              type="button"
              onClick={() => setCollapsed((value) => !value)}
              className="font-medium text-gray-600 hover:text-gray-900"
            >
              {collapsed
                ? `Show ${answer.replies.length} ${answer.replies.length === 1 ? 'reply' : 'replies'}`
                : 'Hide replies'}
            </button>
          )}
            </div>
          </div>
        </div>

        {replyOpen && (
          <AnswerComposer
            label={`Reply to ${answer.author.displayName}`}
            onSubmit={handleReply}
            onCancel={() => setReplyOpen(false)}
          />
        )}
      </article>

      {!collapsed && hasReplies && (
        <div className="mt-3 space-y-3">
          {answer.replies.map((reply) => (
            <AnswerNode
              key={reply.id}
              answer={reply}
              depth={depth + 1}
              questionId={questionId}
              questionOwnedByCurrentUser={questionOwnedByCurrentUser}
              ownedAnswerIds={ownedAnswerIds}
              onChanged={onChanged}
              onSignInRequired={onSignInRequired}
              onVoteChange={onVoteChange}
              onAcceptedAnswerChange={onAcceptedAnswerChange}
            />
          ))}
        </div>
      )}
    </div>
  );
}

export default function AnswerThread({
  questionId,
  answerCount,
  questionOwnedByCurrentUser = false,
  onAnswersChanged,
  onAcceptedAnswerChange,
}: Props) {
  const { status } = useSession();
  const [answers, setAnswers] = useState<AnswerTreeNodeDTO[]>([]);
  const [ownedAnswerIds, setOwnedAnswerIds] = useState<Set<number>>(() => new Set());
  const [topLevelOpen, setTopLevelOpen] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const ownedAnswerIdsRef = useRef<Set<number>>(new Set());
  const mutatingRef = useRef(false);
  const signedInRef = useRef(status === 'authenticated');

  const syncOwnedAnswerIds = useCallback((ownedIds: Set<number>) => {
    ownedAnswerIdsRef.current = ownedIds;
    setOwnedAnswerIds(new Set(ownedIds));
  }, []);

  const registerOwnedAnswer = useCallback((answerId: number) => {
    if (ownedAnswerIdsRef.current.has(answerId)) {
      return;
    }
    const nextOwnedIds = new Set(ownedAnswerIdsRef.current);
    nextOwnedIds.add(answerId);
    syncOwnedAnswerIds(nextOwnedIds);
  }, [syncOwnedAnswerIds]);

  const commitAnswerTree = useCallback((tree: AnswerTreeNodeDTO[]) => {
    const ownedIds = new Set(ownedAnswerIdsRef.current);
    syncOwnedIdsFromTree(tree, ownedIds);
    const finalized = finalizeAnswerTree(tree, ownedIds);
    syncOwnedAnswerIds(ownedIds);
    setAnswers(finalized);
    return finalized;
  }, [syncOwnedAnswerIds]);

  const reloadAnswers = useCallback(async () => {
    if (mutatingRef.current) {
      return;
    }
    const token = await resolveAccessToken();
    const next = await fetchAnswers(questionId, token);
    if (mutatingRef.current) {
      return;
    }
    setAnswers((current) =>
      finalizeAnswerTree(
        shouldReplaceAnswerTree(current, next, Boolean(token), ownedAnswerIdsRef.current),
        ownedAnswerIdsRef.current
      )
    );
  }, [questionId]);

  useEffect(() => {
    let cancelled = false;

    async function loadInitialAnswers() {
      const token = await resolveAccessToken();
      const next = await fetchAnswers(questionId, token);
      if (cancelled || mutatingRef.current) {
        return;
      }
      commitAnswerTree(
        shouldReplaceAnswerTree([], next, Boolean(token), ownedAnswerIdsRef.current)
      );
    }

    loadInitialAnswers().catch(() => {
      // keep the current tree if refresh fails
    });

    return () => {
      cancelled = true;
    };
  }, [questionId, commitAnswerTree]);

  useEffect(() => {
    const wasSignedIn = signedInRef.current;
    signedInRef.current = status === 'authenticated';

    if (wasSignedIn && status === 'unauthenticated') {
      syncOwnedAnswerIds(new Set());
    }

    if (!wasSignedIn && status === 'authenticated') {
      reloadAnswers().catch(() => {
        // keep the current tree if refresh fails
      });
    }
  }, [status, reloadAnswers, syncOwnedAnswerIds]);

  const refreshAfterMutation = useCallback<OnAnswersChanged>(
    async (ownedAnswer, removedAnswerId) => {
      mutatingRef.current = true;
      try {
        if (removedAnswerId != null) {
          const nextOwnedIds = new Set(ownedAnswerIdsRef.current);
          nextOwnedIds.delete(removedAnswerId);
          syncOwnedAnswerIds(nextOwnedIds);

          setAnswers((current) =>
            finalizeAnswerTree(
              patchDeletedAnswerInTree(current, removedAnswerId),
              ownedAnswerIdsRef.current
            )
          );

          if (onAnswersChanged) {
            await onAnswersChanged();
          }

          void (async () => {
            try {
              const token = await resolveAccessToken();
              const next = await fetchAnswers(questionId, token);
              setAnswers((current) => {
                if (countAnswerNodes(next) < countAnswerNodes(current)) {
                  return current;
                }
                return finalizeAnswerTree(
                  mergeOwnershipIntoTree(current, next, ownedAnswerIdsRef.current),
                  ownedAnswerIdsRef.current
                );
              });
            } catch {
              // keep the patched tree if background reconciliation fails
            }
          })();

          return;
        }

        if (ownedAnswer?.id) {
          registerOwnedAnswer(ownedAnswer.id);

          let patchedExistingAnswer = false;
          setAnswers((current) => {
            if (!treeContainsId(current, ownedAnswer.id)) {
              return current;
            }
            patchedExistingAnswer = true;
            return finalizeAnswerTree(
              patchAnswerInTree(current, ownedAnswer),
              ownedAnswerIdsRef.current
            );
          });

          if (patchedExistingAnswer) {
            if (onAnswersChanged) {
              await onAnswersChanged();
            }
            return;
          }
        }

        const token = await resolveAccessToken();
        if (!token) {
          if (onAnswersChanged) {
            await onAnswersChanged();
          }
          return;
        }

        const next = await fetchAnswers(questionId, token);
        setAnswers((current) =>
          finalizeAnswerTree(
            mergeOwnershipIntoTree(current, next, ownedAnswerIdsRef.current),
            ownedAnswerIdsRef.current
          )
        );
        if (onAnswersChanged) {
          await onAnswersChanged();
        }
      } finally {
        mutatingRef.current = false;
      }
    },
    [questionId, onAnswersChanged, registerOwnedAnswer, syncOwnedAnswerIds]
  );

  const handleVoteChange = useCallback(
    (answerId: number, score: number, viewerVote: number | null) => {
      setAnswers((current) => patchVoteInTree(current, answerId, score, viewerVote));
    },
    []
  );

  function requireSignIn() {
    signIn('keycloak', { callbackUrl: window.location.href });
  }

  async function handleTopLevelOpen() {
    const token = await resolveAccessToken();
    if (!token) {
      requireSignIn();
      return;
    }
    setTopLevelOpen((open) => !open);
  }

  async function handleTopLevelSubmit(body: string, anonymous: boolean) {
    const payload: CreateAnswerRequestDTO = { body, anonymous };
    const created = await authFetch<AnswerResponseDTO>(
      `/api/questions/${questionId}/answers`,
      {
        method: 'POST',
        body: JSON.stringify(payload),
      }
    );
    setTopLevelOpen(false);
    if (created) {
      await refreshAfterMutation(created);
    }
  }

  return (
    <section className="mt-10">
      <div className="flex items-center justify-between gap-4">
        <h2 className="text-xl font-semibold text-gray-900">
          Answers ({answerCount})
        </h2>
        <button
          type="button"
          onClick={handleTopLevelOpen}
          className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
        >
          {topLevelOpen ? 'Cancel' : 'Add answer'}
        </button>
      </div>

      {error && <p className="mt-3 text-sm text-red-600">{error}</p>}

      {topLevelOpen && (
        <AnswerComposer
          label="Your answer"
          onSubmit={async (body, anonymous) => {
            setError(null);
            try {
              await handleTopLevelSubmit(body, anonymous);
            } catch (err) {
              setError(getErrorMessage(err, 'Request failed'));
              throw err;
            }
          }}
          onCancel={() => setTopLevelOpen(false)}
        />
      )}

      <div className="mt-6 space-y-4">
        {answers.length === 0 ? (
          <p className="text-sm text-gray-500">No answers yet. Be the first to respond.</p>
        ) : (
          answers.map((answer) => (
            <AnswerNode
              key={answer.id}
              answer={answer}
              depth={0}
              questionId={questionId}
              questionOwnedByCurrentUser={questionOwnedByCurrentUser}
              ownedAnswerIds={ownedAnswerIds}
              onChanged={refreshAfterMutation}
              onSignInRequired={requireSignIn}
              onVoteChange={handleVoteChange}
              onAcceptedAnswerChange={onAcceptedAnswerChange}
            />
          ))
        )}
      </div>
    </section>
  );
}
