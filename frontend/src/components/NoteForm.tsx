import { useEffect, useState, type FormEvent } from "react";
import { fetchCategories, type Category } from "../api/categories";
import type { NoteInput } from "../api/notes";

interface NoteFormProps {
  initial?: NoteInput;
  submitLabel: string;
  onSubmit: (input: NoteInput) => Promise<void>;
  onCancel?: () => void;
}

export function NoteForm({ initial, submitLabel, onSubmit, onCancel }: NoteFormProps) {
  const [categories, setCategories] = useState<Category[]>([]);
  const [category, setCategory] = useState(initial?.category ?? "");
  const [content, setContent] = useState(initial?.content ?? "");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchCategories()
      .then((result) => {
        setCategories(result);
        setCategory((prev) => prev || result[0]?.name || "");
      })
      .catch(() => {
        setCategories([]);
      });
  }, []);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await onSubmit({ category, content });
      if (!initial) {
        setContent("");
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "保存に失敗しました");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form className="note-form" onSubmit={handleSubmit}>
      <div className="note-form-row">
        <select value={category} onChange={(e) => setCategory(e.target.value)} required>
          {categories.map((c) => (
            <option key={c.id} value={c.name}>
              {c.name}
            </option>
          ))}
        </select>
      </div>
      <textarea
        value={content}
        onChange={(e) => setContent(e.target.value)}
        placeholder="伝達事項を入力してください"
        required
      />
      {error && <p className="state-message">{error}</p>}
      <div className="note-form-actions">
        <button type="submit" className="btn" disabled={submitting}>
          {submitting ? "保存中..." : submitLabel}
        </button>
        {onCancel && (
          <button type="button" className="btn btn-ghost" onClick={onCancel} disabled={submitting}>
            キャンセル
          </button>
        )}
      </div>
    </form>
  );
}
