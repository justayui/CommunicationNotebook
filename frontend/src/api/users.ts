export interface User {
  id: number;
  employeeId: string;
  name: string;
  admin: boolean;
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export async function fetchUsers(): Promise<User[]> {
  const res = await fetch(`${API_BASE_URL}/api/users`);
  if (!res.ok) {
    throw new Error(`ユーザー一覧の取得に失敗しました (status: ${res.status})`);
  }
  return res.json();
}
