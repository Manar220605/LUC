export type UserRole = 'MEMBER' | 'STUDENT' | 'ALUMNI' | 'ADMIN';

export type UserResponseDTO = {
  id: number;
  email: string;
  displayName: string;
  avatarUrl: string | null;
  bio: string | null;
  role: UserRole;
  studentId: string | null;
  verifiedStudentAt: string | null;
  verifiedAlumniAt: string | null;
  createdAt: string;
};

export type CommunityResponseDTO = {
  id: number;
  path: string;
  slug: string;
  name: string;
  description: string | null;
  depth: number;
  questionCount: number;
  parentPath: string | null;
};

export type CommunityTreeNodeDTO = {
  id: number;
  path: string;
  slug: string;
  name: string;
  description: string | null;
  depth: number;
  questionCount: number;
  children: CommunityTreeNodeDTO[];
};

export type CreateCommunityRequestDTO = {
  slug: string;
  name: string;
  description?: string;
  parentPath?: string;
};

export type UpdateCommunityRequestDTO = {
  name: string;
  description?: string;
};

export type PublicAuthorDTO = {
  id?: number;
  displayName: string;
  role?: UserRole;
  avatarUrl?: string | null;
};

export type QuestionSummaryDTO = {
  id: number;
  title: string;
  author: PublicAuthorDTO;
  communityPath: string;
  communityName: string;
  score: number;
  answerCount: number;
  viewCount: number;
  createdAt: string;
  viewerVote?: number | null;
  ownedByCurrentUser?: boolean;
};

export type QuestionResponseDTO = {
  id: number;
  title: string;
  body: string;
  author: PublicAuthorDTO;
  community: CommunityResponseDTO;
  status: string;
  anonymous: boolean;
  viewCount: number;
  answerCount: number;
  score: number;
  createdAt: string;
  updatedAt: string;
  viewerVote?: number | null;
  ownedByCurrentUser?: boolean;
};

export type PageResponseDTO<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type CreateQuestionRequestDTO = {
  communityPath: string;
  title: string;
  body: string;
  anonymous?: boolean;
};

export type AnswerTreeNodeDTO = {
  id: number;
  body: string;
  author: PublicAuthorDTO;
  anonymous: boolean;
  deleted: boolean;
  score: number;
  createdAt: string;
  updatedAt: string;
  ownedByCurrentUser: boolean;
  viewerVote?: number | null;
  replies: AnswerTreeNodeDTO[];
};

export type AnswerResponseDTO = {
  id: number;
  questionId: number;
  parentAnswerId: number | null;
  body: string;
  author: PublicAuthorDTO;
  anonymous: boolean;
  deleted: boolean;
  score: number;
  createdAt: string;
  updatedAt: string;
  ownedByCurrentUser: boolean;
};

export type CreateAnswerRequestDTO = {
  body: string;
  anonymous?: boolean;
};

export type UpdateAnswerRequestDTO = {
  body: string;
  anonymous?: boolean;
};
