import { useEffect, useState } from "react";
import { createNote, fetchNotes, type Note, type NoteInput } from "../api/notes";
import { SearchBar } from "./SearchBar";
import { FilterTabs, type FilterTab } from "./FilterTabs";
import { CategoryFilter } from "./CategoryFilter";
import { NoteCard } from "./NoteCard";
import { NoteForm } from "./NoteForm";
import { useAuth } from "../context/AuthContext";

export function NoteList() {
  const { user } = useAuth();
  const [notes, setNotes] = useState<Note[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  const [rawKeyword, setRawKeyword] = useState("");
  const [keyword, setKeyword] = useState("");
  const [activeTab, setActiveTab] = useState<FilterTab>("all");
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);

  useEffect(() => {
    const timer = setTimeout(() => setKeyword(rawKeyword), 300);
    return () => clearTimeout(timer);
  }, [rawKeyword]);

  useEffect(() => {
    fetchNotes({
      keyword: keyword || undefined,
      category: activeTab === "category" ? (selectedCategory ?? undefined) : undefined,
      favoriteOnly: activeTab === "favorite",
    })
      .then(setNotes)
      .catch((err: unknown) => {
        setError(err instanceof Error ? err.message : "不明なエラーが発生しました");
      });
  }, [keyword, activeTab, selectedCategory]);

  function handleFavoriteToggled(noteId: number, favorited: boolean) {
    setNotes((prev) => prev && prev.map((note) => (note.id === noteId ? { ...note, favorited } : note)));
  }

  function handleRead(noteId: number) {
    setNotes(
      (prev) =>
        prev &&
        prev.map((note) =>
          note.id === noteId && !note.read ? { ...note, read: true, readCount: note.readCount + 1 } : note,
        ),
    );
  }

  async function handleCreate(input: NoteInput) {
    const created = await createNote(input);
    setNotes((prev) => (prev ? [created, ...prev] : [created]));
  }

  function handleUpdated(updated: Note) {
    setNotes((prev) => prev && prev.map((note) => (note.id === updated.id ? updated : note)));
  }

  function handleDeleted(noteId: number) {
    setNotes((prev) => prev && prev.filter((note) => note.id !== noteId));
  }

  if (!user) {
    return null;
  }

  return (
    <div>
      <NoteForm submitLabel="投稿する" onSubmit={handleCreate} />

      <div className="note-list-controls">
        <SearchBar value={rawKeyword} onChange={setRawKeyword} />
        <FilterTabs active={activeTab} onChange={setActiveTab} />
        {activeTab === "category" && (
          <CategoryFilter value={selectedCategory} onChange={setSelectedCategory} />
        )}
      </div>

      {error && <p className="state-message">{error}</p>}
      {!error && notes === null && <p className="state-message">Loading...</p>}
      {!error && notes !== null && notes.length === 0 && (
        <p className="state-message">投稿が見つかりませんでした。</p>
      )}
      {!error && notes !== null && notes.length > 0 && (
        <div className="note-list">
          {notes.map((note) => (
            <NoteCard
              key={note.id}
              note={note}
              currentUserId={user.id}
              isAdmin={user.admin}
              onFavoriteToggled={handleFavoriteToggled}
              onRead={handleRead}
              onUpdated={handleUpdated}
              onDeleted={handleDeleted}
            />
          ))}
        </div>
      )}
    </div>
  );
}
