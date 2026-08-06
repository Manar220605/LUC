export type DashboardMetricsResponseDTO = {
  totalUsers: number;
  totalAlumni: number;
  totalQuestions: number;
  totalAnswers: number;
  pendingVerifications: number;
  openReports: number;
};

export type DashboardMetricCard = {
  key: keyof DashboardMetricsResponseDTO;
  label: string;
  description: string;
};

export const DASHBOARD_METRICS: DashboardMetricCard[] = [
  {
    key: 'totalUsers',
    label: 'Total users',
    description: 'All registered accounts',
  },
  {
    key: 'totalAlumni',
    label: 'Total alumni',
    description: 'Users with the Alumni role',
  },
  {
    key: 'totalQuestions',
    label: 'Total questions',
    description: 'All questions, including soft-deleted',
  },
  {
    key: 'totalAnswers',
    label: 'Total answers',
    description: 'All answers, including soft-deleted',
  },
  {
    key: 'pendingVerifications',
    label: 'Pending verifications',
    description: 'Alumni verification requests awaiting review',
  },
  {
    key: 'openReports',
    label: 'Open reports',
    description: 'Moderation reports still pending resolution',
  },
];
