import { useEffect, useState } from "react";
import {
  deleteUser,
  fetchUsers,
  resetPassword,
  updateUserName,
  type PasswordResetResult,
  type UserListItem,
} from "../api/users";
import { Modal } from "./Modal";

export function UserManagementPage() {
  const [users, setUsers] = useState<UserListItem[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  const [editingId, setEditingId] = useState<number | null>(null);
  const [editingName, setEditingName] = useState("");

  const [busyId, setBusyId] = useState<number | null>(null);
  const [resetResult, setResetResult] = useState<PasswordResetResult | null>(null);
  const [copied, setCopied] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<UserListItem | null>(null);
  const [deletedUserName, setDeletedUserName] = useState<string | null>(null);

  useEffect(() => {
    fetchUsers()
      .then(setUsers)
      .catch((err: unknown) => {
        setError(err instanceof Error ? err.message : "ユーザー一覧の取得に失敗しました");
      });
  }, []);

  function startEdit(user: UserListItem) {
    setEditingId(user.id);
    setEditingName(user.name);
    setError(null);
  }

  function cancelEdit() {
    setEditingId(null);
    setEditingName("");
  }

  async function saveEdit(userId: number) {
    setBusyId(userId);
    setError(null);
    try {
      const updated = await updateUserName(userId, editingName);
      setUsers((prev) => prev && prev.map((u) => (u.id === userId ? updated : u)));
      setEditingId(null);
      setEditingName("");
    } catch (err) {
      setError(err instanceof Error ? err.message : "氏名の更新に失敗しました");
    } finally {
      setBusyId(null);
    }
  }

  async function confirmDelete() {
    if (!deleteTarget) {
      return;
    }
    const user = deleteTarget;
    setBusyId(user.id);
    setError(null);
    try {
      await deleteUser(user.id);
      setUsers((prev) => prev && prev.filter((u) => u.id !== user.id));
      setDeleteTarget(null);
      setDeletedUserName(user.name);
    } catch (err) {
      setError(err instanceof Error ? err.message : "削除に失敗しました");
    } finally {
      setBusyId(null);
    }
  }

  async function handleResetPassword(user: UserListItem) {
    setBusyId(user.id);
    setError(null);
    try {
      const result = await resetPassword(user.id);
      setResetResult(result);
      setCopied(false);
    } catch (err) {
      setError(err instanceof Error ? err.message : "パスワード初期化に失敗しました");
    } finally {
      setBusyId(null);
    }
  }

  async function handleCopy() {
    if (!resetResult) {
      return;
    }
    try {
      await navigator.clipboard.writeText(resetResult.temporaryPassword);
      setCopied(true);
    } catch {
      setCopied(false);
    }
  }

  return (
    <div className="user-management">
      {error && <p className="state-message">{error}</p>}
      {!error && users === null && <p className="state-message">Loading...</p>}
      {users !== null && (
        <table className="user-table">
          <thead>
            <tr>
              <th>氏名</th>
              <th>職員ID</th>
              <th>権限</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr key={user.id}>
                <td>
                  {editingId === user.id ? (
                    <input
                      type="text"
                      value={editingName}
                      onChange={(e) => setEditingName(e.target.value)}
                    />
                  ) : (
                    user.name
                  )}
                </td>
                <td>{user.employeeId}</td>
                <td>{user.admin ? "管理者" : "一般"}</td>
                <td className="user-table-actions">
                  {editingId === user.id ? (
                    <>
                      <button
                        type="button"
                        className="mini-btn"
                        onClick={() => saveEdit(user.id)}
                        disabled={busyId === user.id}
                      >
                        保存
                      </button>
                      <button
                        type="button"
                        className="mini-btn"
                        onClick={cancelEdit}
                        disabled={busyId === user.id}
                      >
                        キャンセル
                      </button>
                    </>
                  ) : (
                    <>
                      <button
                        type="button"
                        className="mini-btn"
                        onClick={() => startEdit(user)}
                        disabled={busyId === user.id}
                      >
                        編集
                      </button>
                      <button
                        type="button"
                        className="mini-btn"
                        onClick={() => handleResetPassword(user)}
                        disabled={busyId === user.id}
                      >
                        パスワード初期化
                      </button>
                      <button
                        type="button"
                        className="mini-btn danger"
                        onClick={() => setDeleteTarget(user)}
                        disabled={busyId === user.id}
                      >
                        削除
                      </button>
                    </>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      {resetResult && (
        <Modal title="パスワード初期化" onClose={() => setResetResult(null)}>
          <p>対象ユーザー: {resetResult.name}</p>
          <p>一時パスワード: {resetResult.temporaryPassword}</p>
          <button type="button" className="btn" onClick={handleCopy}>
            {copied ? "コピーしました" : "コピー"}
          </button>
        </Modal>
      )}
      {deleteTarget && (
        <Modal title="ユーザー削除の確認" onClose={() => setDeleteTarget(null)}>
          <p>
            {deleteTarget.name}({deleteTarget.employeeId})を本当に削除しますか?
          </p>
          <div className="user-table-actions">
            <button type="button" className="btn" onClick={confirmDelete} disabled={busyId === deleteTarget.id}>
              削除する
            </button>
            <button
              type="button"
              className="btn btn-ghost"
              onClick={() => setDeleteTarget(null)}
              disabled={busyId === deleteTarget.id}
            >
              キャンセル
            </button>
          </div>
        </Modal>
      )}
      {deletedUserName && (
        <Modal title="削除完了" onClose={() => setDeletedUserName(null)}>
          <p>{deletedUserName}を削除しました</p>
          <button type="button" className="btn btn-ghost" onClick={() => setDeletedUserName(null)}>
            閉じる
          </button>
        </Modal>
      )}
    </div>
  );
}
