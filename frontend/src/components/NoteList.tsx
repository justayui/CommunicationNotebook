import { useEffect, useState } from "react";
import { fetchNotes, type Note } from "../api/notes";

export function NoteList() {
  const [notes, setNotes] = useState<Note[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchNotes()
      .then(setNotes)
      .catch((err: unknown) => {
        setError(err instanceof Error ? err.message : "不明なエラーが発生しました");
      });
  }, []);

  if (error) {
    return <p className="state-message">{error}</p>;
  }

  if (notes === null) {
    return <p className="state-message">Loading...</p>;
  }

  if (notes.length === 0) {
    return <p className="state-message">投稿が見つかりませんでした。</p>;
  }

  return (
    <div className="note-list">
      {notes.map((note) => (
        <article key={note.id} className="note-card">
          <div className="note-top">
            <span className="tag">{note.category}</span>
            <div className="note-meta">
              {note.author} ・ {new Date(note.createdAt).toLocaleString()}
            </div>
          </div>
          <div className="note-body">{note.content}</div>
        </article>
      ))}
    </div>
  );
}
