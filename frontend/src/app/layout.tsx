import type { Metadata } from 'next';

export const metadata: Metadata = {
  title: 'Lebanese University Connect',
  description: 'Q&A platform connecting LU students with alumni',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
