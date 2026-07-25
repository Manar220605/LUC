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
