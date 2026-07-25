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
