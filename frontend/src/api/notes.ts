import { apiFetch } from "./client";

export interface Note {
  id: number;
  userId: number;
  category: string;
  content: string;
  author: string;
  createdAt: string;
  favorited: boolean;
  commentCount: number;
  read: boolean;
  readCount: number;
}

export interface FetchNotesParams {
  keyword?: string;
  category?: string;
  favoriteOnly?: boolean;
}

export interface NoteInput {
  category: string;
  content: string;
}

export async function fetchNotes(params: FetchNotesParams = {}): Promise<Note[]> {
  const query = new URLSearchParams();
  if (params.keyword) query.set("keyword", params.keyword);
  if (params.category) query.set("category", params.category);
  if (params.favoriteOnly) query.set("favoriteOnly", "true");
  const qs = query.toString();

  const res = await apiFetch(`/api/notes${qs ? `?${qs}` : ""}`);
  if (!res.ok) {
    throw new Error(`投稿一覧の取得に失敗しました (status: ${res.status})`);
  }
  return res.json();
}

export async function createNote(input: NoteInput): Promise<Note> {
  const res = await apiFetch("/api/notes", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(input),
  });
  if (!res.ok) {
    throw new Error(`投稿に失敗しました (status: ${res.status})`);
  }
  return res.json();
}

export async function updateNote(id: number, input: NoteInput): Promise<Note> {
  const res = await apiFetch(`/api/notes/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(input),
  });
  if (!res.ok) {
    throw new Error(`更新に失敗しました (status: ${res.status})`);
  }
  return res.json();
}

export async function deleteNote(id: number): Promise<void> {
  const res = await apiFetch(`/api/notes/${id}`, {
    method: "DELETE",
  });
  if (!res.ok) {
    throw new Error(`削除に失敗しました (status: ${res.status})`);
  }
}
