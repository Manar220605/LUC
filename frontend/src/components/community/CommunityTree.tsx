import Link from 'next/link';
import type { CommunityTreeNodeDTO } from '@/lib/types';

type Props = {
  nodes: CommunityTreeNodeDTO[];
  currentPath?: string;
  depth?: number;
};

export default function CommunityTree({ nodes, currentPath, depth = 0 }: Props) {
  return (
    <ul className={depth === 0 ? 'space-y-1' : 'ml-4 mt-1 space-y-1 border-l border-lu/10 pl-3'}>
      {nodes.map((node) => {
        const isActive = node.path === currentPath;
        return (
          <li key={node.path}>
            <Link
              href={`/c/${node.path.split('/').join('/')}`}
              className={`block rounded px-2 py-1 text-sm ${
                isActive ? 'bg-lu-soft font-medium text-lu-dark' : 'text-ink hover:bg-lu-soft'
              }`}
            >
              {node.name}
            </Link>
            {node.children.length > 0 && (
              <CommunityTree nodes={node.children} currentPath={currentPath} depth={depth + 1} />
            )}
          </li>
        );
      })}
    </ul>
  );
}
