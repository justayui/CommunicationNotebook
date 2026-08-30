import { useState } from "react";
import { registerFavorite, unregisterFavorite } from "../api/favorites";

interface FavoriteButtonProps {
  noteId: number;
  favorited: boolean;
  onToggled: (noteId: number, favorited: boolean) => void;
}

export function FavoriteButton({ noteId, favorited, onToggled }: FavoriteButtonProps) {
  const [submitting, setSubmitting] = useState(false);

  async function handleClick() {
    setSubmitting(true);
    try {
      if (favorited) {
        await unregisterFavorite(noteId);
      } else {
        await registerFavorite(noteId);
      }
      onToggled(noteId, !favorited);
    } catch {
      // 失敗時は状態を変更せず、そのまま静観する(次回操作で再試行可能)
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <button
      type="button"
      className="fav-btn"
      aria-pressed={favorited}
      disabled={submitting}
      title="お気に入り"
      onClick={handleClick}
    >
      {favorited ? "★" : "☆"}
    </button>
  );
}
