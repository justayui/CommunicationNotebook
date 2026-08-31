import { useState, type FormEvent } from "react";
import { createComment, deleteComment, type Comment } from "../api/comments";
import { formatDateTime } from "../utils/datetime";

interface CommentSectionProps {
  noteId: number;
  comments: Comment[] | null;
  loading: boolean;
  error: string | null;
  currentUserId: number;
  isAdmin: boolean;
  onCommentAdded: (comment: Comment) => void;
  onCommentDeleted: (commentId: number) => void;
}

export function CommentSection({
  noteId,
  comments,
  loading,
  error,
  currentUserId,
  isAdmin,
  onCommentAdded,
  onCommentDeleted,
}: CommentSectionProps) {
  const [content, setContent] = useState("");
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setSubmitError(null);
    setSubmitting(true);
    try {
      const created = await createComment(noteId, { content });
      onCommentAdded(created);
      setContent("");
    } catch (err) {
      setSubmitError(err instanceof Error ? err.message : "コメントの投稿に失敗しました");
    } finally {
      setSubmitting(false);
    }
  }

  async function handleDelete(commentId: number) {
    if (!window.confirm("このコメントを削除しますか?")) {
      return;
    }
    try {
      await deleteComment(noteId, commentId);
      onCommentDeleted(commentId);
    } catch (err) {
      window.alert(err instanceof Error ? err.message : "削除に失敗しました");
    }
  }

  return (
    <div className="comment-section">
      <div className="comment-body">
        {loading && <p className="comment-state">Loading...</p>}
        {!loading && error && <p className="comment-state">{error}</p>}
        {!loading && !error && comments && comments.length > 0 && (
          <ul className="comment-list">
            {comments.map((comment) => (
              <li key={comment.id} className="comment-item">
                <div className="comment-item-header">
                  <div className="comment-meta">
                    {comment.author} ・ {formatDateTime(comment.createdAt)}
                  </div>
                  {(comment.userId === currentUserId || isAdmin) && (
                    <button
                      type="button"
                      className="link-action comment-delete"
                      onClick={() => handleDelete(comment.id)}
                    >
                      削除
                    </button>
                  )}
                </div>
                <div className="comment-content">{comment.content}</div>
              </li>
            ))}
          </ul>
        )}
        <form className="comment-form" onSubmit={handleSubmit}>
          <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder="コメントを入力してください"
            required
          />
          {submitError && <p className="comment-state">{submitError}</p>}
          <button type="submit" disabled={submitting}>
            {submitting ? "投稿中..." : "投稿する"}
          </button>
        </form>
      </div>
    </div>
  );
}
