import Link from 'next/link';
import type { CommunityTreeNodeDTO } from '@/lib/types';

type Props = {
  nodes: CommunityTreeNodeDTO[];
  currentPath?: string;
  depth?: number;
};

export default function CommunityTree({ nodes, currentPath, depth = 0 }: Props) {
  return (
    <ul className={depth === 0 ? 'space-y-1' : 'ml-4 mt-1 space-y-1 border-l border-gray-200 pl-3'}>
      {nodes.map((node) => {
        const isActive = node.path === currentPath;
        return (
          <li key={node.path}>
            <Link
              href={`/c/${node.path.split('/').join('/')}`}
              className={`block rounded px-2 py-1 text-sm ${
                isActive ? 'bg-blue-100 font-medium text-blue-800' : 'text-gray-700 hover:bg-gray-100'
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
