import type { Metadata } from 'next';
import SessionProvider from '@/components/providers/SessionProvider';
import Header from '@/components/layout/Header';
import './globals.css';

export const metadata: Metadata = {
  title: 'Lebanese University Connect',
  description: 'Q&A platform connecting LU students with alumni',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body className="min-h-screen bg-gray-50 text-gray-900">
        <SessionProvider>
          <Header />
          {children}
        </SessionProvider>
      </body>
    </html>
  );
}
