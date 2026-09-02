import { useState } from "react";
import "./App.css";
import { NoteList } from "./components/NoteList";
import { LoginForm } from "./components/LoginForm";
import { SignupForm } from "./components/SignupForm";
import { PasswordChangeForm } from "./components/PasswordChangeForm";
import { UserManagementPage } from "./components/UserManagementPage";
import { Modal } from "./components/Modal";
import { AuthProvider, useAuth } from "./context/AuthContext";

function AppContent() {
  const { user, loading, logout } = useAuth();
  const [mode, setMode] = useState<"login" | "signup">("login");
  const [view, setView] = useState<"notes" | "users">("notes");
  const [passwordModalOpen, setPasswordModalOpen] = useState(false);

  if (loading) {
    return <p className="state-message">Loading...</p>;
  }

  if (!user) {
    return (
      <main className="app-shell">
        <header className="app-header">
          <h1>連絡ノート</h1>
        </header>
        {mode === "login" ? (
          <LoginForm onSwitchToSignup={() => setMode("signup")} />
        ) : (
          <SignupForm onSwitchToLogin={() => setMode("login")} />
        )}
      </main>
    );
  }

  return (
    <main className="app-shell">
      <header className="app-header">
        <h1>連絡ノート</h1>
        <div className="user-bar">
          <span>{user.name}</span>
          {user.admin && (
            <button type="button" onClick={() => setView(view === "users" ? "notes" : "users")}>
              {view === "users" ? "連絡ノートに戻る" : "ユーザー管理"}
            </button>
          )}
          <button type="button" onClick={() => setPasswordModalOpen(true)}>
            パスワード変更
          </button>
          <button
            type="button"
            onClick={() => {
              logout();
              setMode("login");
              setView("notes");
            }}
          >
            ログアウト
          </button>
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

function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}

export default App;
