import { useEffect, useState } from "react";
import { fetchNotes, type Note } from "../api/notes";
import { SearchBar } from "./SearchBar";
import { FilterTabs, type FilterTab } from "./FilterTabs";
import { CategoryFilter } from "./CategoryFilter";
import { FavoriteButton } from "./FavoriteButton";

export function NoteList() {
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

  return (
    <div>
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
            <article key={note.id} className="note-card">
              <div className="note-top">
                <span className="tag">{note.category}</span>
                <div className="note-meta">
                  {note.author} ・ {new Date(note.createdAt).toLocaleString()}
                </div>
                <FavoriteButton
                  noteId={note.id}
                  favorited={note.favorited}
                  onToggled={handleFavoriteToggled}
                />
              </div>
              <div className="note-body">{note.content}</div>
            </article>
          ))}
        </div>
      )}
    </div>
  );
}
