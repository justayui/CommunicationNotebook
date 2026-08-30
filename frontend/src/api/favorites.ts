const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export async function registerFavorite(noteId: number): Promise<void> {
  const res = await fetch(`${API_BASE_URL}/api/notes/${noteId}/favorites`, {
    method: "POST",
    credentials: "include",
  });
  if (!res.ok) {
    throw new Error(`お気に入り登録に失敗しました (status: ${res.status})`);
  }
}

export async function unregisterFavorite(noteId: number): Promise<void> {
  const res = await fetch(`${API_BASE_URL}/api/notes/${noteId}/favorites`, {
    method: "DELETE",
    credentials: "include",
  });
  if (!res.ok) {
    throw new Error(`お気に入り解除に失敗しました (status: ${res.status})`);
  }
}
