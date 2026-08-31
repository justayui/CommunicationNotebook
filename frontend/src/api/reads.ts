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
