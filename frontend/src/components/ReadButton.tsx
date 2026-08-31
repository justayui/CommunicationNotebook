import { useState } from "react";
import { fetchReadUsers, registerRead, type ReadUser } from "../api/reads";
import { Modal } from "./Modal";

interface ReadButtonProps {
  noteId: number;
  read: boolean;
  readCount: number;
  onRead: (noteId: number) => void;
}

export function ReadButton({ noteId, read, readCount, onRead }: ReadButtonProps) {
  const [submitting, setSubmitting] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [readers, setReaders] = useState<ReadUser[] | null>(null);
  const [loadingReaders, setLoadingReaders] = useState(false);
  const [error, setError] = useState<string | null>(null);

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

  async function handleOpenModal() {
    setModalOpen(true);
    setError(null);
    setLoadingReaders(true);
    try {
      setReaders(await fetchReadUsers(noteId));
    } catch (err) {
      setError(err instanceof Error ? err.message : "既読者一覧の取得に失敗しました");
    } finally {
      setLoadingReaders(false);
    }
  }

  return (
    <div className="read-status">
      <button type="button" className="read-btn" disabled={read || submitting} onClick={handleClick}>
        {read ? "確認済み" : "確認"}
      </button>
      <button type="button" className="read-toggle" onClick={handleOpenModal}>
        既読 {readCount}人
      </button>
      {modalOpen && (
        <Modal title="既読者一覧" onClose={() => setModalOpen(false)}>
          {loadingReaders && <p className="comment-state">Loading...</p>}
          {!loadingReaders && error && <p className="comment-state">{error}</p>}
          {!loadingReaders && !error && readers && readers.length === 0 && (
            <p className="comment-state">まだ既読者はいません。</p>
          )}
          {!loadingReaders && !error && readers && readers.length > 0 && (
            <ul className="reader-list">
              {readers.map((reader) => (
                <li key={reader.userId} className="reader-item">
                  {reader.name}
                </li>
              ))}
            </ul>
          )}
        </Modal>
      )}
    </div>
  );
}
