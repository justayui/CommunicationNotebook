import { createContext, useContext, useEffect, useState, type ReactNode } from "react";
import { fetchCurrentUser, login as loginApi, logout as logoutApi, type User } from "../api/auth";

interface AuthContextValue {
  user: User | null;
  loading: boolean;
  login: (employeeId: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
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

  async function logout() {
    await logoutApi();
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, logout }}>{children}</AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
