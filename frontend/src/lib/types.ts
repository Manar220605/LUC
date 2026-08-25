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

export type FollowStatusDTO = {
  following: boolean;
  followerCount: number;
};

export type CommunityFollowResponseDTO = {
  community: CommunityResponseDTO;
  followedAt: string;
};

export type SaveStatusDTO = {
  saved: boolean;
};

export type QuestionSaveResponseDTO = {
  question: QuestionSummaryDTO;
  savedAt: string;
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
  gradYear?: number | null;
  currentPosition?: string | null;
};

export type PublicAlumniInfoDTO = {
  gradYear: number;
  faculty: Faculty;
  degree: Degree;
  major: string | null;
  currentPosition: string | null;
  currentCompany: string | null;
  linkedinUrl: string | null;
};

export type PublicProfileQuestionDTO = {
  id: number;
  title: string;
  communityPath: string;
  communityName: string;
  score: number;
  answerCount: number;
  createdAt: string;
};

export type PublicProfileAnswerDTO = {
  id: number;
  questionId: number;
  questionTitle: string;
  bodyPreview: string;
  score: number;
  createdAt: string;
};

export type PublicProfileDTO = {
  id: number;
  displayName: string;
  avatarUrl: string | null;
  bio: string | null;
  role: UserRole;
  createdAt: string;
  questionCount: number;
  answerCount: number;
  score: number;
  alumni: PublicAlumniInfoDTO | null;
  questions: PublicProfileQuestionDTO[];
  answers: PublicProfileAnswerDTO[];
};

export type AlumniDirectoryEntryDTO = {
  userId: number;
  displayName: string;
  avatarUrl: string | null;
  gradYear: number;
  faculty: Faculty;
  degree: Degree;
  major: string | null;
  currentPosition: string | null;
  currentCompany: string | null;
  linkedinUrl: string | null;
};

export type Faculty =
  | 'LITERATURE_AND_HUMAN_SCIENCES'
  | 'INFORMATION_AND_DOCUMENTATION'
  | 'LAW_POLITICAL_AND_ADMINISTRATIVE_SCIENCES'
  | 'ECONOMIC_SCIENCES_AND_BUSINESS_ADMINISTRATION'
  | 'SCIENCES'
  | 'EDUCATION'
  | 'ENGINEERING'
  | 'AGRICULTURE_AND_VETERINARY_SCIENCES'
  | 'MEDICAL_SCIENCES'
  | 'DENTISTRY'
  | 'PHARMACY'
  | 'PUBLIC_HEALTH'
  | 'FINE_ARTS_AND_ARCHITECTURE'
  | 'TOURISM_AND_HOSPITALITY_MANAGEMENT'
  | 'SOCIAL_SCIENCES'
  | 'TECHNOLOGY'
  | 'INSTITUTE_OF_PHYSICAL_EDUCATION';

export type Degree = 'BS' | 'MS' | 'PhD' | 'DIPLOMA';

export type VerificationStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export type SubmitVerificationRequestDTO = {
  linkedinUrl: string;
  claimedGradYear: number;
  claimedFaculty: Faculty;
  claimedDegree: Degree;
  claimedMajor: string;
  claimedPosition?: string;
  claimedCompany?: string;
};

export type VerificationResponseDTO = {
  id: number;
  status: VerificationStatus;
  linkedinUrl: string;
  claimedGradYear: number;
  claimedFaculty: Faculty;
  claimedDegree: Degree;
  claimedMajor: string;
  claimedPosition: string | null;
  claimedCompany: string | null;
  submittedAt: string;
  reviewedAt: string | null;
  rejectionReason: string | null;
};

export type AlumniProfileResponseDTO = {
  gradYear: number;
  faculty: Faculty;
  degree: Degree;
  major: string;
  currentPosition: string | null;
  currentCompany: string | null;
  linkedinUrl: string | null;
  isPublic: boolean;
  createdAt: string;
  updatedAt: string;
};

export type UpdateAlumniProfileRequestDTO = {
  currentPosition?: string;
  currentCompany?: string;
  isPublic?: boolean;
};

export type AdminVerificationResponseDTO = {
  id: number;
  userId: number;
  userDisplayName: string;
  userEmail: string;
  linkedinUrl: string;
  claimedGradYear: number;
  claimedFaculty: Faculty;
  claimedDegree: Degree;
  claimedMajor: string;
  claimedPosition: string | null;
  claimedCompany: string | null;
  status: VerificationStatus;
  submittedAt: string;
  reviewedAt: string | null;
  rejectionReason: string | null;
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
  acceptedAnswerId?: number | null;
};

export type PageResponseDTO<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type NotificationType =
  | 'ANSWER_ON_QUESTION'
  | 'UPVOTE'
  | 'MENTION'
  | 'MENTORSHIP_REQUEST'
  | 'MENTORSHIP_ACCEPTED'
  | 'MENTORSHIP_DECLINED'
  | 'NEW_QUESTION_IN_COMMUNITY';

export type NotificationResponseDTO = {
  id: number;
  type: NotificationType;
  message: string;
  actor: PublicAuthorDTO;
  targetType?: 'QUESTION' | 'ANSWER' | null;
  targetId?: number | null;
  questionId?: number | null;
  read: boolean;
  createdAt: string;
};

export type UnreadCountResponseDTO = {
  count: number;
};

export type MentorshipRequestStatus = 'PENDING' | 'ACCEPTED' | 'DECLINED';

export type MentorshipRequestResponseDTO = {
  id: number;
  studentId: number;
  studentDisplayName: string;
  studentAvatarUrl: string | null;
  alumniId: number;
  alumniDisplayName: string;
  alumniAvatarUrl: string | null;
  alumniLinkedinUrl: string | null;
  message: string;
  status: MentorshipRequestStatus;
  createdAt: string;
  respondedAt: string | null;
};

export type MentorshipInboxDTO = {
  incoming: MentorshipRequestResponseDTO[];
  outgoing: MentorshipRequestResponseDTO[];
};

export type MentorshipStatusDTO = {
  canRequest: boolean;
  cannotRequestReason: string | null;
  existing: MentorshipRequestResponseDTO | null;
};

export type CreateMentorshipRequestDTO = {
  alumniUserId: number;
  message: string;
};

export type RegisterStudentRequestDTO = {
  email: string;
  code: string;
  password: string;
  confirmPassword: string;
};

export type LookupStudentResponseDTO = {
  message: string;
};

export type ForgotEmailResponseDTO = {
  message: string;
  maskedEmail?: string | null;
};

export type RegisterStudentResponseDTO = {
  message: string;
  fullName: string;
  enrollmentYear: number;
  faculty: string;
  major: string;
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
  accepted?: boolean;
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
