import { useState } from "react";
import "./App.css";
import { NoteList } from "./components/NoteList";
import { LoginForm } from "./components/LoginForm";
import { SignupForm } from "./components/SignupForm";
import { PasswordChangeForm } from "./components/PasswordChangeForm";
import { UserManagementPage } from "./components/UserManagementPage";
import { Modal } from "./components/Modal";
import { AuthProvider, useAuth } from "./context/AuthContext";
import type { User } from "./api/auth";

// ログイン画面・メイン画面は別コンポーネントとし、ログイン状態が切り替わるたびに画面内の状態(表示中のタブ・モーダル等)を初期化する
function LoginScreen() {
  const [mode, setMode] = useState<"login" | "signup">("login");

  return (
    <main className="app-shell">
      <div className="login-wrap">
        <div className="login-card">
          <div className="login-brand">
            <h1>連絡ノート</h1>
            <p>組織内の連絡・共有をひとつのノートに</p>
          </div>
          {mode === "login" ? (
            <LoginForm onSwitchToSignup={() => setMode("signup")} />
          ) : (
            <SignupForm onSwitchToLogin={() => setMode("login")} />
          )}
        </div>
      </div>
    </main>
  );
}

function MainScreen({ user }: { user: User }) {
  const { logout } = useAuth();
  const [view, setView] = useState<"notes" | "users">("notes");
  const [passwordModalOpen, setPasswordModalOpen] = useState(false);

  return (
    <main className="app-shell">
      <header className="app-header">
        <div className="app-header-top">
          <h1>連絡ノート</h1>
          <div className="user-bar">
            <span>{user.name}</span>
            {user.admin && (
              <button
                type="button"
                className="mini-btn"
                onClick={() => setView(view === "users" ? "notes" : "users")}
              >
                {view === "users" ? "連絡ノートに戻る" : "ユーザー管理"}
              </button>
            )}
            {view !== "users" && (
              <>
                <button type="button" className="mini-btn" onClick={() => setPasswordModalOpen(true)}>
                  パスワード変更
                </button>
                <button type="button" className="mini-btn" onClick={logout}>
                  ログアウト
                </button>
              </>
            )}
          </div>
        </div>
      </header>
      {user.admin && view === "users" ? <UserManagementPage /> : <NoteList />}
      {passwordModalOpen && (
        <Modal title="パスワード変更" onClose={() => setPasswordModalOpen(false)}>
          <PasswordChangeForm />
        </Modal>
      )}
    </main>
  );
}

function AppContent() {
  const { user, loading } = useAuth();

  if (loading) {
    return <p className="state-message">Loading...</p>;
  }

  return user ? <MainScreen user={user} /> : <LoginScreen />;
}

function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}

export default App;
