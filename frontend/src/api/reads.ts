export interface ReadUser {
  userId: number;
  name: string;
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export async function registerRead(noteId: number): Promise<void> {
  const res = await fetch(`${API_BASE_URL}/api/notes/${noteId}/reads`, {
    method: "POST",
    credentials: "include",
  });
  if (!res.ok) {
    throw new Error(`既読登録に失敗しました (status: ${res.status})`);
  }
}

export async function fetchReadUsers(noteId: number): Promise<ReadUser[]> {
  const res = await fetch(`${API_BASE_URL}/api/notes/${noteId}/reads`, { credentials: "include" });
  if (!res.ok) {
    throw new Error(`既読者一覧の取得に失敗しました (status: ${res.status})`);
  }
  return res.json();
}
