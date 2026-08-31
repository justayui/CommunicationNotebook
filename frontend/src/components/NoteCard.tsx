import { useState } from "react";
import { fetchComments, type Comment } from "../api/comments";
import { deleteNote, updateNote, type Note, type NoteInput } from "../api/notes";
import { formatDateTime } from "../utils/datetime";
import { CommentSection } from "./CommentSection";
import { CommentToggle } from "./CommentToggle";
import { FavoriteButton } from "./FavoriteButton";
import { NoteForm } from "./NoteForm";
import { ReadButton } from "./ReadButton";

interface NoteCardProps {
  note: Note;
  currentUserId: number;
  isAdmin: boolean;
  onFavoriteToggled: (noteId: number, favorited: boolean) => void;
  onRead: (noteId: number) => void;
  onUpdated: (note: Note) => void;
  onDeleted: (noteId: number) => void;
}

export function NoteCard({
  note,
  currentUserId,
  isAdmin,
  onFavoriteToggled,
  onRead,
  onUpdated,
  onDeleted,
}: NoteCardProps) {
  const [editing, setEditing] = useState(false);
  const [deleting, setDeleting] = useState(false);

  const [commentsExpanded, setCommentsExpanded] = useState(false);
  const [comments, setComments] = useState<Comment[] | null>(null);
  const [commentsLoading, setCommentsLoading] = useState(false);
  const [commentsError, setCommentsError] = useState<string | null>(null);

  const isAuthor = note.userId === currentUserId;
  const canEdit = isAuthor;
  const canDelete = isAuthor || isAdmin;
  const commentCount = comments?.length ?? note.commentCount;

  async function handleUpdate(input: NoteInput) {
    const updated = await updateNote(note.id, input);
    onUpdated(updated);
    setEditing(false);
  }

  async function handleDelete() {
    if (!window.confirm("この投稿を削除しますか?")) {
      return;
    }
    setDeleting(true);
    try {
      await deleteNote(note.id);
      onDeleted(note.id);
    } catch (err) {
      window.alert(err instanceof Error ? err.message : "削除に失敗しました");
    } finally {
      setDeleting(false);
    }
  }

  async function handleCommentToggle() {
    const next = !commentsExpanded;
    setCommentsExpanded(next);
    if (next && comments === null) {
      setCommentsLoading(true);
      try {
        setComments(await fetchComments(note.id));
      } catch (err) {
        setCommentsError(err instanceof Error ? err.message : "コメントの取得に失敗しました");
      } finally {
        setCommentsLoading(false);
      }
    }
  }

  function handleCommentAdded(comment: Comment) {
    setComments((prev) => (prev ? [...prev, comment] : [comment]));
  }

  function handleCommentDeleted(commentId: number) {
    setComments((prev) => prev && prev.filter((c) => c.id !== commentId));
  }

  if (editing) {
    return (
      <article className="note-card">
        <NoteForm
          initial={{ category: note.category, content: note.content }}
          submitLabel="保存する"
          onSubmit={handleUpdate}
          onCancel={() => setEditing(false)}
        />
      </article>
    );
  }

  return (
    <article className="note-card">
      <div className="note-top">
        <span className="tag">{note.category}</span>
        <div className="note-meta">
          {note.author} ・ {formatDateTime(note.createdAt)}
        </div>
        {canEdit && (
          <button type="button" className="link-action" onClick={() => setEditing(true)}>
            編集
          </button>
        )}
        {canDelete && (
          <button type="button" className="link-action" onClick={handleDelete} disabled={deleting}>
            削除
          </button>
        )}
        <FavoriteButton noteId={note.id} favorited={note.favorited} onToggled={onFavoriteToggled} />
      </div>
      <div className="note-body">{note.content}</div>
      <div className="read-status">
        <ReadButton noteId={note.id} read={note.read} readCount={note.readCount} onRead={onRead} />
        <CommentToggle count={commentCount} onClick={handleCommentToggle} />
      </div>
      {commentsExpanded && (
        <CommentSection
          noteId={note.id}
          comments={comments}
          loading={commentsLoading}
          error={commentsError}
          currentUserId={currentUserId}
          isAdmin={isAdmin}
          onCommentAdded={handleCommentAdded}
          onCommentDeleted={handleCommentDeleted}
        />
      )}
    </article>
  );
}
