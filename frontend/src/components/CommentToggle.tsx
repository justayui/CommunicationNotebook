interface CommentToggleProps {
  count: number;
  onClick: () => void;
}

export function CommentToggle({ count, onClick }: CommentToggleProps) {
  return (
    <button type="button" className="comment-toggle" onClick={onClick}>
      コメント ({count})
    </button>
  );
}
