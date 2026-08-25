import Link from 'next/link';
import { notFound } from 'next/navigation';
import Avatar from '@/components/user/Avatar';
import AuthorBadge from '@/components/user/AuthorBadge';
import MentorshipRequestPanel from '@/components/mentorship/MentorshipRequestPanel';
import { apiPublicGet } from '@/lib/apiPublic';
import { isApiError } from '@/lib/apiError';
import { degreeLabel, facultyLabel } from '@/lib/alumni';
import { isSafeHttpUrl } from '@/lib/url';
import type { PublicProfileDTO, UserRole } from '@/lib/types';

type Props = {
  params: Promise<{ id: string }>;
};

function roleLabel(role: UserRole): string {
  switch (role) {
    case 'STUDENT':
      return 'Student';
    case 'ALUMNI':
      return 'Alumni';
    case 'ADMIN':
      return 'Admin';
    default:
      return 'Member';
  }
}

export default async function PublicProfilePage({ params }: Props) {
  const { id } = await params;
  if (!/^\d+$/.test(id)) {
    notFound();
  }

  let profile: PublicProfileDTO;
  try {
    profile = await apiPublicGet<PublicProfileDTO>(`/api/users/${id}/public-profile`);
  } catch (err) {
    if (isApiError(err, 404)) {
      notFound();
    }
    throw err;
  }

  const alumni = profile.alumni;
  const jobLine = [alumni?.currentPosition, alumni?.currentCompany].filter(Boolean).join(' at ');
  const studyLine = alumni
    ? [degreeLabel(alumni.degree), alumni.major, facultyLabel(alumni.faculty), alumni.gradYear]
        .filter(Boolean)
        .join(' · ')
    : null;

  return (
    <main className="mx-auto max-w-4xl px-4 py-8">
      <section className="rounded-lg border border-gray-200 bg-white p-6">
        <div className="flex flex-wrap items-start gap-4">
          <Avatar name={profile.displayName} avatarUrl={profile.avatarUrl} />
          <div className="min-w-0 flex-1">
            <div className="flex flex-wrap items-center gap-2">
              <h1 className="text-2xl font-semibold text-gray-900">{profile.displayName}</h1>
              <AuthorBadge
                author={{
                  id: profile.id,
                  displayName: profile.displayName,
                  role: profile.role,
                  gradYear: alumni?.gradYear,
                  currentPosition: alumni?.currentPosition,
                }}
              />
            </div>
            <p className="mt-1 text-sm text-gray-600">
              {roleLabel(profile.role)} · Member since{' '}
              {new Date(profile.createdAt).toLocaleDateString()}
            </p>
            {jobLine && <p className="mt-2 text-sm text-gray-800">{jobLine}</p>}
            {studyLine && <p className="mt-1 text-sm text-gray-600">{studyLine}</p>}
            {alumni && isSafeHttpUrl(alumni.linkedinUrl) && (
              <p className="mt-2">
                <a
                  href={alumni.linkedinUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-sm font-medium text-blue-700 hover:text-blue-800"
                >
                  LinkedIn profile
                </a>
              </p>
            )}
          </div>
        </div>

        {profile.bio && (
          <p className="mt-4 whitespace-pre-wrap text-sm text-gray-800">{profile.bio}</p>
        )}

        <dl className="mt-6 grid grid-cols-3 gap-4 border-t border-gray-100 pt-4 text-center">
          <div>
            <dt className="text-xs font-medium uppercase tracking-wide text-gray-500">Score</dt>
            <dd className="mt-1 text-lg font-semibold text-gray-900">{profile.score}</dd>
          </div>
          <div>
            <dt className="text-xs font-medium uppercase tracking-wide text-gray-500">Questions</dt>
            <dd className="mt-1 text-lg font-semibold text-gray-900">{profile.questionCount}</dd>
          </div>
          <div>
            <dt className="text-xs font-medium uppercase tracking-wide text-gray-500">Answers</dt>
            <dd className="mt-1 text-lg font-semibold text-gray-900">{profile.answerCount}</dd>
          </div>
        </dl>
      </section>

      {profile.role === 'ALUMNI' && (
        <MentorshipRequestPanel alumniUserId={profile.id} alumniName={profile.displayName} />
      )}

      <section className="mt-8">
        <h2 className="text-lg font-semibold text-gray-900">Questions</h2>
        {profile.questions.length === 0 ? (
          <p className="mt-2 text-sm text-gray-500">No public questions yet.</p>
        ) : (
          <ul className="mt-3 divide-y divide-gray-200 rounded-lg border border-gray-200 bg-white">
            {profile.questions.map((question) => (
              <li key={question.id} className="px-4 py-3">
                <Link
                  href={`/questions/${question.id}`}
                  className="font-medium text-gray-900 hover:text-blue-700"
                >
                  {question.title}
                </Link>
                <p className="mt-1 text-xs text-gray-500">
                  {question.score} score · {question.answerCount} answers ·{' '}
                  <Link href={`/c/${question.communityPath}`} className="hover:text-blue-700">
                    {question.communityName}
                  </Link>{' '}
                  · {new Date(question.createdAt).toLocaleDateString()}
                </p>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="mt-8">
        <h2 className="text-lg font-semibold text-gray-900">Answers</h2>
        {profile.answers.length === 0 ? (
          <p className="mt-2 text-sm text-gray-500">No public answers yet.</p>
        ) : (
          <ul className="mt-3 divide-y divide-gray-200 rounded-lg border border-gray-200 bg-white">
            {profile.answers.map((answer) => (
              <li key={answer.id} className="px-4 py-3">
                <Link
                  href={`/questions/${answer.questionId}`}
                  className="font-medium text-gray-900 hover:text-blue-700"
                >
                  {answer.questionTitle}
                </Link>
                {answer.bodyPreview && (
                  <p className="mt-1 line-clamp-2 text-sm text-gray-700">{answer.bodyPreview}</p>
                )}
                <p className="mt-1 text-xs text-gray-500">
                  {answer.score} score · {new Date(answer.createdAt).toLocaleDateString()}
                </p>
              </li>
            ))}
          </ul>
        )}
      </section>
    </main>
  );
}
