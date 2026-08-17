import type { Components } from 'react-markdown';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import rehypeSanitize from 'rehype-sanitize';

type Props = {
  content: string;
  className?: string;
};

const markdownComponents: Components = {
  p: ({ children }) => <p className="mb-3 last:mb-0">{children}</p>,
  h1: ({ children }) => <h1 className="mb-3 text-2xl font-bold text-gray-900">{children}</h1>,
  h2: ({ children }) => <h2 className="mb-3 text-xl font-semibold text-gray-900">{children}</h2>,
  h3: ({ children }) => <h3 className="mb-2 text-lg font-semibold text-gray-900">{children}</h3>,
  ul: ({ children }) => <ul className="mb-3 list-disc space-y-1 pl-6">{children}</ul>,
  ol: ({ children }) => <ol className="mb-3 list-decimal space-y-1 pl-6">{children}</ol>,
  li: ({ children }) => <li className="text-gray-800">{children}</li>,
  blockquote: ({ children }) => (
    <blockquote className="mb-3 border-l-4 border-gray-300 pl-4 italic text-gray-700">
      {children}
    </blockquote>
  ),
  a: ({ href, children }) => (
    <a
      href={href}
      target="_blank"
      rel="noopener noreferrer"
      className="font-medium text-blue-600 hover:text-blue-800 hover:underline"
    >
      {children}
    </a>
  ),
  code: ({ className, children }) => {
    const isBlock = Boolean(className);
    if (isBlock) {
      return <code className={`font-mono text-sm ${className}`}>{children}</code>;
    }
    return (
      <code className="rounded bg-gray-100 px-1.5 py-0.5 font-mono text-sm text-gray-900">
        {children}
      </code>
    );
  },
  pre: ({ children }) => (
    <pre className="mb-3 overflow-x-auto rounded-md bg-gray-900 p-3 text-sm text-gray-100">
      {children}
    </pre>
  ),
  hr: () => <hr className="my-4 border-gray-200" />,
  table: ({ children }) => (
    <div className="mb-3 overflow-x-auto">
      <table className="min-w-full border-collapse border border-gray-200 text-sm">{children}</table>
    </div>
  ),
  th: ({ children }) => (
    <th className="border border-gray-200 bg-gray-50 px-3 py-2 text-left font-semibold text-gray-900">
      {children}
    </th>
  ),
  td: ({ children }) => (
    <td className="border border-gray-200 px-3 py-2 text-gray-800">{children}</td>
  ),
};

export default function MarkdownContent({ content, className = '' }: Props) {
  return (
    <div className={`markdown-content text-gray-800 ${className}`}>
      <ReactMarkdown
        remarkPlugins={[remarkGfm]}
        rehypePlugins={[rehypeSanitize]}
        components={markdownComponents}
      >
        {content}
      </ReactMarkdown>
    </div>
  );
}
