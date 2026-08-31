import { useState } from "react";
import { deleteNote, updateNote, type Note, type NoteInput } from "../api/notes";
import { formatDateTime } from "../utils/datetime";
import { CommentSection } from "./CommentSection";
import { FavoriteButton } from "./FavoriteButton";
import { NoteForm } from "./NoteForm";

interface NoteCardProps {
  note: Note;
  currentUserId: number;
  isAdmin: boolean;
  onFavoriteToggled: (noteId: number, favorited: boolean) => void;
  onUpdated: (note: Note) => void;
  onDeleted: (noteId: number) => void;
}

export function NoteCard({ note, currentUserId, isAdmin, onFavoriteToggled, onUpdated, onDeleted }: NoteCardProps) {
  const [editing, setEditing] = useState(false);
  const [deleting, setDeleting] = useState(false);

  const isAuthor = note.userId === currentUserId;
  const canEdit = isAuthor;
  const canDelete = isAuthor || isAdmin;

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
        <FavoriteButton noteId={note.id} favorited={note.favorited} onToggled={onFavoriteToggled} />
      </div>
      <div className="note-body">{note.content}</div>
      <CommentSection
        noteId={note.id}
        initialCount={note.commentCount}
        currentUserId={currentUserId}
        isAdmin={isAdmin}
      />
      {(canEdit || canDelete) && (
        <div className="note-actions">
          {canEdit && (
            <button type="button" onClick={() => setEditing(true)}>
              編集
            </button>
          )}
          {canDelete && (
            <button type="button" onClick={handleDelete} disabled={deleting}>
              削除
            </button>
          )}
        </div>
      )}
    </article>
  );
}
