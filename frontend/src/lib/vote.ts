export type VoteTargetType = 'QUESTION' | 'ANSWER';

export type CastVoteRequestDTO = {
  targetType: VoteTargetType;
  targetId: number;
  value: -1 | 1;
};

export type VoteResponseDTO = {
  targetType: VoteTargetType;
  targetId: number;
  viewerVote: -1 | 1 | null;
  score: number;
};

export function nextVoteState(
  currentVote: number | null | undefined,
  clicked: -1 | 1
): { viewerVote: -1 | 1 | null; scoreDelta: number } {
  if (currentVote == null) {
    return { viewerVote: clicked, scoreDelta: clicked };
  }
  if (currentVote === clicked) {
    return { viewerVote: null, scoreDelta: -clicked };
  }
  return { viewerVote: clicked, scoreDelta: 2 * clicked };
}
