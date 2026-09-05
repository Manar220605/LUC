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
  h1: ({ children }) => <h1 className="mb-3 text-2xl font-bold text-lu-deep">{children}</h1>,
  h2: ({ children }) => <h2 className="mb-3 text-xl font-semibold text-lu-deep">{children}</h2>,
  h3: ({ children }) => <h3 className="mb-2 text-lg font-semibold text-lu-deep">{children}</h3>,
  ul: ({ children }) => <ul className="mb-3 list-disc space-y-1 pl-6">{children}</ul>,
  ol: ({ children }) => <ol className="mb-3 list-decimal space-y-1 pl-6">{children}</ol>,
  li: ({ children }) => <li className="text-ink">{children}</li>,
  blockquote: ({ children }) => (
    <blockquote className="mb-3 border-l-4 border-lu/20 pl-4 italic text-ink">
      {children}
    </blockquote>
  ),
  a: ({ href, children }) => (
    <a
      href={href}
      target="_blank"
      rel="noopener noreferrer"
      className="font-medium text-lu hover:text-lu-dark hover:underline"
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
      <code className="rounded bg-lu-soft px-1.5 py-0.5 font-mono text-sm text-lu-deep">
        {children}
      </code>
    );
  },
  pre: ({ children }) => (
    <pre className="mb-3 overflow-x-auto rounded-md bg-gray-900 p-3 text-sm text-gray-100">
      {children}
    </pre>
  ),
  hr: () => <hr className="my-4 border-lu/10" />,
  table: ({ children }) => (
    <div className="mb-3 overflow-x-auto">
      <table className="min-w-full border-collapse border border-lu/10 text-sm">{children}</table>
    </div>
  ),
  th: ({ children }) => (
    <th className="border border-lu/10 bg-lu-mist px-3 py-2 text-left font-semibold text-lu-deep">
      {children}
    </th>
  ),
  td: ({ children }) => (
    <td className="border border-lu/10 px-3 py-2 text-ink">{children}</td>
  ),
};

export default function MarkdownContent({ content, className = '' }: Props) {
  return (
    <div className={`markdown-content text-ink ${className}`}>
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
