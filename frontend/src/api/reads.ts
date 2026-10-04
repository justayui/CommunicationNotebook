import { apiFetch } from "./client";

export interface ReadUser {
  userId: number;
  name: string;
}

export async function registerRead(noteId: number): Promise<void> {
  const res = await apiFetch(`/api/notes/${noteId}/reads`, {
    method: "POST",
  });
  if (!res.ok) {
    throw new Error(`既読登録に失敗しました (status: ${res.status})`);
  }
}

export async function fetchReadUsers(noteId: number): Promise<ReadUser[]> {
  const res = await apiFetch(`/api/notes/${noteId}/reads`);
  if (!res.ok) {
    throw new Error(`既読者一覧の取得に失敗しました (status: ${res.status})`);
  }
  return res.json();
}
