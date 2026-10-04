import { createContext, useContext, useEffect, useState, type ReactNode } from "react";
import {
  fetchCurrentUser,
  login as loginApi,
  logout as logoutApi,
  signup as signupApi,
  type User,
} from "../api/auth";
import { setSessionExpiredHandler } from "../api/client";

interface AuthContextValue {
  user: User | null;
  loading: boolean;
  // 無操作によるセッション切れでログアウトした直後かどうか(ログイン画面での案内表示に使用)
  sessionExpired: boolean;
  login: (employeeId: string, password: string) => Promise<void>;
  signup: (employeeId: string, name: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  refreshUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [sessionExpired, setSessionExpired] = useState(false);

  useEffect(() => {
    fetchCurrentUser()
      .then(setUser)
      .finally(() => setLoading(false));
  }, []);

  // API呼び出しでセッション切れ(401)を検知した場合は、ログイン状態を解除してログイン画面へ戻す
  useEffect(() => {
    setSessionExpiredHandler(() => {
      setUser(null);
      setSessionExpired(true);
    });
    return () => setSessionExpiredHandler(null);
  }, []);

  async function login(employeeId: string, password: string) {
    const loggedInUser = await loginApi(employeeId, password);
    setUser(loggedInUser);
    setSessionExpired(false);
  }

  async function signup(employeeId: string, name: string, password: string) {
    const signedUpUser = await signupApi(employeeId, name, password);
    setUser(signedUpUser);
    setSessionExpired(false);
  }

  async function logout() {
    await logoutApi();
    setUser(null);
  }

  // ログイン中のユーザー情報を再取得する(取得に失敗した場合は現在の表示を維持する)
  async function refreshUser() {
    try {
      setUser(await fetchCurrentUser());
    } catch {
      // 表示の更新に失敗しても操作自体は完了しているため、エラーにはしない
    }
  }

  return (
    <AuthContext.Provider value={{ user, loading, sessionExpired, login, signup, logout, refreshUser }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
