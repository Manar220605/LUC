export type ReportReason = 'SPAM' | 'HARASSMENT' | 'OFF_TOPIC' | 'OTHER';

export type ReportTargetType = 'QUESTION' | 'ANSWER' | 'USER';

export type ReportStatus = 'PENDING' | 'RESOLVED';

export type ResolutionAction = 'NONE' | 'DELETE_CONTENT' | 'BAN_USER';

export type CreateReportRequestDTO = {
  targetType: ReportTargetType;
  targetId: number;
  reason: ReportReason;
  details?: string;
};

export type ReportResponseDTO = {
  id: number;
  targetType: ReportTargetType;
  targetId: number;
  reason: ReportReason;
  details: string | null;
  status: ReportStatus;
  createdAt: string;
};

export type ModeratorAuthorDTO = {
  userId: number;
  displayName: string;
  email: string;
  postedAnonymously: boolean;
};

export type AdminReportResponseDTO = {
  id: number;
  reporterId: number;
  reporterDisplayName: string;
  reporterEmail: string;
  targetType: ReportTargetType;
  targetId: number;
  reason: ReportReason;
  details: string | null;
  status: ReportStatus;
  createdAt: string;
  resolvedAt: string | null;
  resolutionNote: string | null;
  resolverDisplayName: string | null;
  targetPreview: string;
  targetQuestionId: number | null;
  targetAuthor: ModeratorAuthorDTO | null;
};

export type ResolveReportRequestDTO = {
  resolutionNote?: string;
  action: ResolutionAction;
};

export const REPORT_REASONS: { value: ReportReason; label: string }[] = [
  { value: 'SPAM', label: 'Spam' },
  { value: 'HARASSMENT', label: 'Harassment' },
  { value: 'OFF_TOPIC', label: 'Off topic' },
  { value: 'OTHER', label: 'Other' },
];

export const REPORT_REASON_LABELS: Record<ReportReason, string> = {
  SPAM: 'Spam',
  HARASSMENT: 'Harassment',
  OFF_TOPIC: 'Off topic',
  OTHER: 'Other',
};

export const REPORT_TARGET_LABELS: Record<ReportTargetType, string> = {
  QUESTION: 'Question',
  ANSWER: 'Answer',
  USER: 'User',
};

export const RESOLUTION_ACTIONS: { value: ResolutionAction; label: string; hint: string }[] = [
  {
    value: 'NONE',
    label: 'Close only',
    hint: 'Mark resolved without changing the target.',
  },
  {
    value: 'DELETE_CONTENT',
    label: 'Delete content',
    hint: 'Soft-delete the reported question or answer.',
  },
  {
    value: 'BAN_USER',
    label: 'Ban user',
    hint: 'Ban the reported user or content author and disable their Keycloak account.',
  },
];
