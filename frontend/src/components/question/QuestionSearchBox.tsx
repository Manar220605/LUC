'use client';

import { FormEvent, useState } from 'react';
import { useRouter } from 'next/navigation';

type Props = {
  initialQuery?: string;
  action?: 'global' | 'community';
  placeholder?: string;
  className?: string;
  variant?: 'default' | 'onBrand';
};

export default function QuestionSearchBox({
  initialQuery = '',
  action = 'global',
  placeholder = 'Search questions…',
  className = '',
  variant = 'default',
}: Props) {
  const router = useRouter();
  const [query, setQuery] = useState(initialQuery);
  const onBrand = variant === 'onBrand';

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
        className={
          onBrand
            ? 'min-w-0 flex-1 rounded-lg border-0 bg-white px-3 py-1.5 text-sm text-ink placeholder:text-muted/70 outline-none ring-0 focus:ring-2 focus:ring-white/70'
            : 'min-w-0 flex-1 rounded-lg border border-lu/20 bg-white px-3 py-1.5 text-sm text-ink placeholder:text-muted/60 outline-none focus:border-lu focus:ring-2 focus:ring-lu/20'
        }
      />
      <button
        type="submit"
        className={
          onBrand
            ? 'rounded-lg bg-white px-3 py-1.5 text-sm font-semibold text-lu hover:bg-lu-soft'
            : 'rounded-lg bg-lu px-3 py-1.5 text-sm font-semibold text-white hover:bg-lu-dark'
        }
      >
        Search
      </button>
      {query && action === 'community' && (
        <button
          type="button"
          onClick={handleClear}
          className="rounded-lg border border-lu/20 px-2 py-1.5 text-sm text-lu-dark hover:bg-lu-soft"
        >
          Clear
        </button>
      )}
    </form>
  );
}
