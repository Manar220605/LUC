import type { Degree, Faculty } from '@/lib/types';

export const FACULTIES: { value: Faculty; label: string }[] = [
  { value: 'LITERATURE_AND_HUMAN_SCIENCES', label: 'Literature and Human Sciences' },
  { value: 'INFORMATION_AND_DOCUMENTATION', label: 'Information and Documentation' },
  {
    value: 'LAW_POLITICAL_AND_ADMINISTRATIVE_SCIENCES',
    label: 'Law, Political and Administrative Sciences',
  },
  {
    value: 'ECONOMIC_SCIENCES_AND_BUSINESS_ADMINISTRATION',
    label: 'Economic Sciences and Business Administration',
  },
  { value: 'SCIENCES', label: 'Sciences' },
  { value: 'EDUCATION', label: 'Education' },
  { value: 'ENGINEERING', label: 'Engineering' },
  { value: 'AGRICULTURE_AND_VETERINARY_SCIENCES', label: 'Agriculture and Veterinary Sciences' },
  { value: 'MEDICAL_SCIENCES', label: 'Medical Sciences' },
  { value: 'DENTISTRY', label: 'Dentistry' },
  { value: 'PHARMACY', label: 'Pharmacy' },
  { value: 'PUBLIC_HEALTH', label: 'Public Health' },
  { value: 'FINE_ARTS_AND_ARCHITECTURE', label: 'Fine Arts and Architecture' },
  {
    value: 'TOURISM_AND_HOSPITALITY_MANAGEMENT',
    label: 'Tourism and Hospitality Management',
  },
  { value: 'SOCIAL_SCIENCES', label: 'Social Sciences' },
  { value: 'TECHNOLOGY', label: 'Technology' },
  { value: 'INSTITUTE_OF_PHYSICAL_EDUCATION', label: 'Institute of Physical Education' },
];

export const DEGREES: { value: Degree; label: string }[] = [
  { value: 'BS', label: 'Bachelor (BS)' },
  { value: 'MS', label: 'Master (MS)' },
  { value: 'PhD', label: 'Doctorate (PhD)' },
  { value: 'DIPLOMA', label: 'Diploma' },
];

export function facultyLabel(faculty: Faculty): string {
  return FACULTIES.find((entry) => entry.value === faculty)?.label ?? faculty;
}

export function degreeLabel(degree: Degree): string {
  return DEGREES.find((entry) => entry.value === degree)?.label ?? degree;
}
