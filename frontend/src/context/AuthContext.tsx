import { createContext, useContext, useEffect, useState, type ReactNode } from "react";
import {
  fetchCurrentUser,
  login as loginApi,
  logout as logoutApi,
  signup as signupApi,
  type User,
} from "../api/auth";

interface AuthContextValue {
  user: User | null;
  loading: boolean;
  login: (employeeId: string, password: string) => Promise<void>;
  signup: (employeeId: string, name: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  refreshUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchCurrentUser()
      .then(setUser)
      .finally(() => setLoading(false));
  }, []);

  async function login(employeeId: string, password: string) {
    const loggedInUser = await loginApi(employeeId, password);
    setUser(loggedInUser);
  }

  async function signup(employeeId: string, name: string, password: string) {
    const signedUpUser = await signupApi(employeeId, name, password);
    setUser(signedUpUser);
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
    <AuthContext.Provider value={{ user, loading, login, signup, logout, refreshUser }}>
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
