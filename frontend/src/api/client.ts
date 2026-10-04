export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

// セッション切れ(401 Unauthorized)を表すエラー
export class SessionExpiredError extends Error {
  constructor() {
    super("セッションの有効期限が切れました");
    this.name = "SessionExpiredError";
  }
}

let sessionExpiredHandler: (() => void) | null = null;

// セッション切れを検知した際に呼び出す処理を登録する(AuthContextから登録する)
export function setSessionExpiredHandler(handler: (() => void) | null) {
  sessionExpiredHandler = handler;
}

export function notifySessionExpired() {
  sessionExpiredHandler?.();
}

// ログイン中の画面から呼び出すAPI用のfetch。401が返った場合はセッション切れとして通知し、SessionExpiredErrorを投げる
export async function apiFetch(path: string, init: RequestInit = {}): Promise<Response> {
  const res = await fetch(`${API_BASE_URL}${path}`, { ...init, credentials: "include" });
  if (res.status === 401) {
    notifySessionExpired();
    throw new SessionExpiredError();
  }
  return res;
}
