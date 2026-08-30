export interface Note {
  id: number;
  category: string;
  content: string;
  author: string;
  createdAt: string;
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export async function fetchNotes(): Promise<Note[]> {
  const res = await fetch(`${API_BASE_URL}/api/notes`, { credentials: "include" });
  if (!res.ok) {
    throw new Error(`投稿一覧の取得に失敗しました (status: ${res.status})`);
  }
  return res.json();
}
