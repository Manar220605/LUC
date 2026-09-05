'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import type { ReactNode } from 'react';

const SECTIONS = [
  { href: '/admin/dashboard', label: 'Dashboard' },
  { href: '/admin/users', label: 'Users' },
  { href: '/admin/communities', label: 'Communities' },
  { href: '/admin/announcements', label: 'Announcements' },
  { href: '/admin/reports', label: 'Reports' },
  { href: '/admin/verifications', label: 'Verifications' },
];

export default function AdminLayout({ children }: { children: ReactNode }) {
  const pathname = usePathname();

  return (
    <div>
      <nav className="border-b border-lu/10 bg-white">
        <div className="mx-auto flex max-w-5xl gap-1 overflow-x-auto px-4 sm:px-6">
          {SECTIONS.map((section) => {
            const isActive = pathname === section.href || pathname.startsWith(`${section.href}/`);
            return (
              <Link
                key={section.href}
                href={section.href}
                className={`shrink-0 border-b-2 px-3 py-3 text-sm font-medium transition ${
                  isActive
                    ? 'border-lu text-lu'
                    : 'border-transparent text-muted hover:text-lu-deep'
                }`}
              >
                {section.label}
              </Link>
            );
          })}
        </div>
      </nav>
      {children}
    </div>
  );
}
