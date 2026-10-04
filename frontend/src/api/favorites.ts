import { apiFetch } from "./client";

export async function registerFavorite(noteId: number): Promise<void> {
  const res = await apiFetch(`/api/notes/${noteId}/favorites`, {
    method: "POST",
  });
  if (!res.ok) {
    throw new Error(`お気に入り登録に失敗しました (status: ${res.status})`);
  }
}

export async function unregisterFavorite(noteId: number): Promise<void> {
  const res = await apiFetch(`/api/notes/${noteId}/favorites`, {
    method: "DELETE",
  });
  if (!res.ok) {
    throw new Error(`お気に入り解除に失敗しました (status: ${res.status})`);
  }
}
