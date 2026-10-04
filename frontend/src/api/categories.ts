import { apiFetch } from "./client";

export interface Category {
  name: string;
}

export async function fetchCategories(): Promise<Category[]> {
  const res = await apiFetch("/api/categories");
  if (!res.ok) {
    throw new Error(`カテゴリ一覧の取得に失敗しました (status: ${res.status})`);
  }
  return res.json();
}
