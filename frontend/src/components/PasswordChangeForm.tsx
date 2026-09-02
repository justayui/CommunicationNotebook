import { useState, type FormEvent } from "react";
import { changePassword } from "../api/auth";

export function PasswordChangeForm() {
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);

    if (newPassword !== confirmPassword) {
      setError("新しいパスワードが一致しません");
      return;
    }

    setSubmitting(true);
    try {
      await changePassword(currentPassword, newPassword);
      setSuccess(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : "パスワードの変更に失敗しました");
    } finally {
      setSubmitting(false);
    }
  }

  if (success) {
    return <p className="state-message">パスワードを変更しました</p>;
  }

  return (
    <form className="login-form" onSubmit={handleSubmit}>
      <label>
        現在のパスワード
        <input
          type="password"
          value={currentPassword}
          onChange={(e) => setCurrentPassword(e.target.value)}
          required
        />
      </label>
      <label>
        新しいパスワード
        <input
          type="password"
          value={newPassword}
          onChange={(e) => setNewPassword(e.target.value)}
          required
        />
      </label>
      <label>
        新しいパスワード(確認)
        <input
          type="password"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
          required
        />
      </label>
      {error && <p className="state-message">{error}</p>}
      <button type="submit" disabled={submitting}>
        {submitting ? "変更中..." : "変更する"}
      </button>
    </form>
  );
}
