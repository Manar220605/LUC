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
