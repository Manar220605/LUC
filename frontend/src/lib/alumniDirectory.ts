export type AlumniDirectoryQuery = {
  q?: string;
  company?: string;
  gradYear?: string;
  faculty?: string;
  page?: string;
};

export function buildAlumniQuery(query: AlumniDirectoryQuery, page?: number): string {
  const params = new URLSearchParams();
  const q = query.q?.trim();
  const company = query.company?.trim();
  const gradYear = query.gradYear?.trim();
  const faculty = query.faculty?.trim();
  if (q) params.set('q', q);
  if (company) params.set('company', company);
  if (gradYear) params.set('gradYear', gradYear);
  if (faculty) params.set('faculty', faculty);
  const pageNumber = page ?? (query.page ? Number(query.page) : 0);
  if (pageNumber > 0) params.set('page', String(pageNumber));
  const encoded = params.toString();
  return encoded ? `?${encoded}` : '';
}
