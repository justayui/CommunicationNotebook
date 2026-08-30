export interface Category {
  id: number;
  name: string;
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export async function fetchCategories(): Promise<Category[]> {
  const res = await fetch(`${API_BASE_URL}/api/categories`, { credentials: "include" });
  if (!res.ok) {
    throw new Error(`カテゴリ一覧の取得に失敗しました (status: ${res.status})`);
  }
  return res.json();
}
