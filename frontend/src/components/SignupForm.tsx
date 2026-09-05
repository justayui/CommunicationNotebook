import { useState, type FormEvent } from "react";
import { useAuth } from "../context/AuthContext";

export function SignupForm({ onSwitchToLogin }: { onSwitchToLogin: () => void }) {
  const { signup } = useAuth();
  const [employeeId, setEmployeeId] = useState("");
  const [name, setName] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await signup(employeeId, name, password);
    } catch (err) {
      setError(err instanceof Error ? err.message : "登録に失敗しました");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form className="login-form" onSubmit={handleSubmit}>
      <h2>新規登録</h2>
      <label>
        職員ID
        <input
          type="text"
          value={employeeId}
          onChange={(e) => setEmployeeId(e.target.value)}
          required
        />
      </label>
      <label>
        氏名
        <input type="text" value={name} onChange={(e) => setName(e.target.value)} required />
      </label>
      <label>
        パスワード
        <input
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
      </label>
      {error && <p className="state-message">{error}</p>}
      <button type="submit" className="btn btn-block" disabled={submitting}>
        {submitting ? "登録中..." : "登録"}
      </button>
      <div className="login-links">
        <button type="button" className="link-action" onClick={onSwitchToLogin}>
          ログイン画面に戻る
        </button>
      </div>
    </form>
  );
}
