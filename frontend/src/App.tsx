import { useState } from "react";
import "./App.css";
import { NoteList } from "./components/NoteList";
import { LoginForm } from "./components/LoginForm";
import { SignupForm } from "./components/SignupForm";
import { AuthProvider, useAuth } from "./context/AuthContext";

function AppContent() {
  const { user, loading, logout } = useAuth();
  const [mode, setMode] = useState<"login" | "signup">("login");

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
          <button
            type="button"
            onClick={() => {
              logout();
              setMode("login");
            }}
          >
            ログアウト
          </button>
        </div>
      </header>
      <NoteList />
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
