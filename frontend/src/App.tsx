import "./App.css";
import { NoteList } from "./components/NoteList";
import { LoginForm } from "./components/LoginForm";
import { AuthProvider, useAuth } from "./context/AuthContext";

function AppContent() {
  const { user, loading, logout } = useAuth();

  if (loading) {
    return <p className="state-message">Loading...</p>;
  }

  if (!user) {
    return (
      <main className="app-shell">
        <header className="app-header">
          <h1>連絡ノート</h1>
        </header>
        <LoginForm />
      </main>
    );
  }

  return (
    <main className="app-shell">
      <header className="app-header">
        <h1>連絡ノート</h1>
        <div className="user-bar">
          <span>{user.name}</span>
          <button type="button" onClick={() => logout()}>
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
