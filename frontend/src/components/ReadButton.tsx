import { useState } from "react";
import { registerRead } from "../api/reads";

interface ReadButtonProps {
  noteId: number;
  read: boolean;
  readCount: number;
  onRead: (noteId: number) => void;
}

export function ReadButton({ noteId, read, readCount, onRead }: ReadButtonProps) {
  const [submitting, setSubmitting] = useState(false);

  async function handleClick() {
    setSubmitting(true);
    try {
      await registerRead(noteId);
      onRead(noteId);
    } catch {
      // 失敗時は状態を変更せず、そのまま静観する(次回操作で再試行可能)
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="read-status">
      <button type="button" className="read-btn" disabled={read || submitting} onClick={handleClick}>
        {read ? "確認済み" : "確認"}
      </button>
      <span className="read-count">既読 {readCount}人</span>
    </div>
  );
}
