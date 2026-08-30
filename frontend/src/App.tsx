import "./App.css";
import { NoteList } from "./components/NoteList";

function App() {
  return (
    <main className="app-shell">
      <header className="app-header">
        <h1>連絡ノート</h1>
      </header>
      <NoteList />
    </main>
  );
}

export default App;
