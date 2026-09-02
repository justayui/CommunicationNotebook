export interface UserListItem {
  id: number;
  employeeId: string;
  name: string;
  admin: boolean;
}

export interface PasswordResetResult {
  name: string;
  temporaryPassword: string;
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

function forbiddenOrGenericError(status: number): Error {
  if (status === 403) {
    return new Error("管理者のみ実行できます");
  }
  return new Error("処理に失敗しました");
}

export async function fetchUsers(): Promise<UserListItem[]> {
  const res = await fetch(`${API_BASE_URL}/api/users`, {
    credentials: "include",
  });
  if (!res.ok) {
    throw new Error(`ユーザー一覧の取得に失敗しました (status: ${res.status})`);
  }
  return res.json();
}

export async function updateUserName(userId: number, name: string): Promise<UserListItem> {
  const res = await fetch(`${API_BASE_URL}/api/users/${userId}`, {
    method: "PUT",
    credentials: "include",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name }),
  });
  if (!res.ok) {
    throw forbiddenOrGenericError(res.status);
  }
  return res.json();
}

export async function deleteUser(userId: number): Promise<void> {
  const res = await fetch(`${API_BASE_URL}/api/users/${userId}`, {
    method: "DELETE",
    credentials: "include",
  });
  if (!res.ok) {
    throw forbiddenOrGenericError(res.status);
  }
}

export async function resetPassword(userId: number): Promise<PasswordResetResult> {
  const res = await fetch(`${API_BASE_URL}/api/users/${userId}/password-reset`, {
    method: "POST",
    credentials: "include",
  });
  if (!res.ok) {
    throw forbiddenOrGenericError(res.status);
  }
  return res.json();
}
