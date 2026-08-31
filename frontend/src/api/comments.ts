export interface Comment {
  id: number;
  noteId: number;
  userId: number;
  author: string;
  content: string;
  createdAt: string;
}

export interface CommentInput {
  content: string;
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export async function fetchComments(noteId: number): Promise<Comment[]> {
  const res = await fetch(`${API_BASE_URL}/api/notes/${noteId}/comments`, { credentials: "include" });
  if (!res.ok) {
    throw new Error(`コメントの取得に失敗しました (status: ${res.status})`);
  }
  return res.json();
}

export async function createComment(noteId: number, input: CommentInput): Promise<Comment> {
  const res = await fetch(`${API_BASE_URL}/api/notes/${noteId}/comments`, {
    method: "POST",
    credentials: "include",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(input),
  });
  if (!res.ok) {
    throw new Error(`コメントの投稿に失敗しました (status: ${res.status})`);
  }
  return res.json();
}

export async function deleteComment(noteId: number, commentId: number): Promise<void> {
  const res = await fetch(`${API_BASE_URL}/api/notes/${noteId}/comments/${commentId}`, {
    method: "DELETE",
    credentials: "include",
  });
  if (!res.ok) {
    throw new Error(`コメントの削除に失敗しました (status: ${res.status})`);
  }
}
