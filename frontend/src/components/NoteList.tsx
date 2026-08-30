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
    return <p>{error}</p>;
  }

  if (notes === null) {
    return <p>Loading...</p>;
  }

  if (notes.length === 0) {
    return <p>投稿が見つかりませんでした。</p>;
  }

  return (
    <table>
      <thead>
        <tr>
          <th>category</th>
          <th>content</th>
          <th>author</th>
          <th>createdAt</th>
        </tr>
      </thead>
      <tbody>
        {notes.map((note) => (
          <tr key={note.id}>
            <td>{note.category}</td>
            <td>{note.content}</td>
            <td>{note.author}</td>
            <td>{new Date(note.createdAt).toLocaleString()}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
