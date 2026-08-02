import type { CommunityTreeNodeDTO } from '@/lib/types';

type Props = {
  nodes: CommunityTreeNodeDTO[];
  value: string;
  onChange: (path: string) => void;
};

function flattenTree(nodes: CommunityTreeNodeDTO[]): CommunityTreeNodeDTO[] {
  const result: CommunityTreeNodeDTO[] = [];
  for (const node of nodes) {
    result.push(node);
    result.push(...flattenTree(node.children));
  }
  return result;
}

export default function CommunityPicker({ nodes, value, onChange }: Props) {
  const options = flattenTree(nodes).sort((a, b) => a.path.localeCompare(b.path));

  return (
    <select
      value={value}
      onChange={(event) => onChange(event.target.value)}
      className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
      required
    >
      {options.map((option) => (
        <option key={option.path} value={option.path}>
          {option.path} — {option.name}
        </option>
      ))}
    </select>
  );
}
