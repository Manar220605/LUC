export type FeedSort = 'NEW' | 'TOP' | 'HOT';

export const FEED_SORTS: FeedSort[] = ['NEW', 'TOP', 'HOT'];

export function parseFeedSort(value: string | undefined): FeedSort {
  if (value === 'TOP' || value === 'HOT') {
    return value;
  }
  return 'NEW';
}

export function buildFeedQuery(options: {
  sort: FeedSort;
  communityPath?: string;
  includeDescendants?: boolean;
  page?: number;
  size?: number;
}): string {
  const params = new URLSearchParams();
  params.set('sort', options.sort);
  params.set('page', String(options.page ?? 0));
  params.set('size', String(options.size ?? 20));
  if (options.communityPath) {
    params.set('community', options.communityPath);
    params.set('includeDescendants', String(options.includeDescendants ?? true));
  }
  return `?${params.toString()}`;
}

export function feedSortLabel(sort: FeedSort): string {
  switch (sort) {
    case 'TOP':
      return 'Top';
    case 'HOT':
      return 'Hot';
    default:
      return 'New';
  }
}
