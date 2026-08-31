import { useState, type FormEvent } from "react";
import { createComment, deleteComment, fetchComments, type Comment } from "../api/comments";

interface CommentSectionProps {
  noteId: number;
  initialCount: number;
  currentUserId: number;
  isAdmin: boolean;
}

export function CommentSection({ noteId, initialCount, currentUserId, isAdmin }: CommentSectionProps) {
  const [expanded, setExpanded] = useState(false);
  const [comments, setComments] = useState<Comment[] | null>(null);
  const [loading, setLoading] = useState(false);
  const [content, setContent] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const count = comments?.length ?? initialCount;

  async function handleToggle() {
    const next = !expanded;
    setExpanded(next);
    if (next && comments === null) {
      setLoading(true);
      try {
        setComments(await fetchComments(noteId));
      } catch (err) {
        setError(err instanceof Error ? err.message : "コメントの取得に失敗しました");
      } finally {
        setLoading(false);
      }
    }
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const created = await createComment(noteId, { content });
      setComments((prev) => (prev ? [...prev, created] : [created]));
      setContent("");
    } catch (err) {
      setError(err instanceof Error ? err.message : "コメントの投稿に失敗しました");
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
      setComments((prev) => prev && prev.filter((c) => c.id !== commentId));
    } catch (err) {
      window.alert(err instanceof Error ? err.message : "削除に失敗しました");
    }
  }

  return (
    <div className="comment-section">
      <button type="button" className="comment-toggle" onClick={handleToggle}>
        コメント ({count})
      </button>
      {expanded && (
        <div className="comment-body">
          {loading && <p className="comment-state">Loading...</p>}
          {!loading && comments && comments.length > 0 && (
            <ul className="comment-list">
              {comments.map((comment) => (
                <li key={comment.id} className="comment-item">
                  <div className="comment-meta">
                    {comment.author} ・ {new Date(comment.createdAt).toLocaleString()}
                  </div>
                  <div className="comment-content">{comment.content}</div>
                  {(comment.userId === currentUserId || isAdmin) && (
                    <button type="button" onClick={() => handleDelete(comment.id)}>
                      削除
                    </button>
                  )}
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
            {error && <p className="comment-state">{error}</p>}
            <button type="submit" disabled={submitting}>
              {submitting ? "投稿中..." : "投稿する"}
            </button>
          </form>
        </div>
      )}
    </div>
  );
}
