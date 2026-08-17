'use client';

import { FormEvent, useState } from 'react';
import { useRouter } from 'next/navigation';

type Props = {
  initialQuery?: string;
  action?: 'global' | 'community';
  placeholder?: string;
  className?: string;
};

export default function QuestionSearchBox({
  initialQuery = '',
  action = 'global',
  placeholder = 'Search questions…',
  className = '',
}: Props) {
  const router = useRouter();
  const [query, setQuery] = useState(initialQuery);

  function handleSubmit(event: FormEvent) {
    event.preventDefault();
    const trimmed = query.trim();
    if (!trimmed) {
      return;
    }

    if (action === 'global') {
      router.push(`/search?q=${encodeURIComponent(trimmed)}`);
      return;
    }

    const params = new URLSearchParams(window.location.search);
    params.set('search', trimmed);
    params.delete('page');
    router.push(`${window.location.pathname}?${params.toString()}`);
  }

  function handleClear() {
    setQuery('');
    if (action === 'community') {
      const params = new URLSearchParams(window.location.search);
      params.delete('search');
      params.delete('page');
      router.push(`${window.location.pathname}?${params.toString()}`);
    }
  }

  return (
    <form onSubmit={handleSubmit} className={`flex items-center gap-2 ${className}`}>
      <input
        type="search"
        value={query}
        onChange={(event) => setQuery(event.target.value)}
        placeholder={placeholder}
        aria-label="Search questions"
        className="min-w-0 flex-1 rounded-md border border-gray-300 px-3 py-1.5 text-sm text-gray-900 placeholder:text-gray-400 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
      />
      <button
        type="submit"
        className="rounded-md bg-blue-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-blue-700"
      >
        Search
      </button>
      {query && action === 'community' && (
        <button
          type="button"
          onClick={handleClear}
          className="rounded-md border border-gray-300 px-2 py-1.5 text-sm text-gray-600 hover:bg-gray-50"
        >
          Clear
        </button>
      )}
    </form>
  );
}
