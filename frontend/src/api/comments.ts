import { apiFetch } from "./client";

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

export async function fetchComments(noteId: number): Promise<Comment[]> {
  const res = await apiFetch(`/api/notes/${noteId}/comments`);
  if (!res.ok) {
    throw new Error(`コメントの取得に失敗しました (status: ${res.status})`);
  }
  return res.json();
}

export async function createComment(noteId: number, input: CommentInput): Promise<Comment> {
  const res = await apiFetch(`/api/notes/${noteId}/comments`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(input),
  });
  if (!res.ok) {
    throw new Error(`コメントの投稿に失敗しました (status: ${res.status})`);
  }
  return res.json();
}

export async function deleteComment(noteId: number, commentId: number): Promise<void> {
  const res = await apiFetch(`/api/notes/${noteId}/comments/${commentId}`, {
    method: "DELETE",
  });
  if (!res.ok) {
    throw new Error(`コメントの削除に失敗しました (status: ${res.status})`);
  }
}
