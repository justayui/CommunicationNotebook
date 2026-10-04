import { API_BASE_URL, notifySessionExpired, SessionExpiredError } from "./client";

// 認証APIは401を「認証失敗」として個別に扱うため、セッション切れの共通処理(apiFetch)は使用しない

export interface User {
  id: number;
  employeeId: string;
  name: string;
  admin: boolean;
}

export async function login(employeeId: string, password: string): Promise<User> {
  const res = await fetch(`${API_BASE_URL}/api/auth/login`, {
    method: "POST",
    credentials: "include",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ employeeId, password }),
  });
  if (!res.ok) {
    throw new Error("職員IDまたはパスワードが正しくありません");
  }
  return res.json();
}

export async function signup(employeeId: string, name: string, password: string): Promise<User> {
  const res = await fetch(`${API_BASE_URL}/api/auth/signup`, {
    method: "POST",
    credentials: "include",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ employeeId, name, password }),
  });
  if (!res.ok) {
    if (res.status === 409) {
      throw new Error("職員IDは既に使用されています");
    }
    throw new Error("入力内容を確認してください");
  }
  return res.json();
}

export async function changePassword(currentPassword: string, newPassword: string): Promise<void> {
  const res = await fetch(`${API_BASE_URL}/api/auth/password`, {
    method: "PUT",
    credentials: "include",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ currentPassword, newPassword }),
  });
  if (!res.ok) {
    if (res.status === 401) {
      // 現在のパスワード不一致とセッション切れはどちらも401のため、ログイン状態を確認して区別する
      if ((await fetchCurrentUser()) === null) {
        notifySessionExpired();
        throw new SessionExpiredError();
      }
      throw new Error("現在のパスワードが正しくありません");
    }
    throw new Error("入力内容を確認してください");
  }
}

export async function logout(): Promise<void> {
  const res = await fetch(`${API_BASE_URL}/api/auth/logout`, {
    method: "POST",
    credentials: "include",
  });
  if (!res.ok) {
    throw new Error(`ログアウトに失敗しました (status: ${res.status})`);
  }
}

export async function fetchCurrentUser(): Promise<User | null> {
  const res = await fetch(`${API_BASE_URL}/api/auth/me`, {
    credentials: "include",
  });
  if (res.status === 401) {
    return null;
  }
  if (!res.ok) {
    throw new Error(`ログイン状態の確認に失敗しました (status: ${res.status})`);
  }
  return res.json();
}
