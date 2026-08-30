interface SearchBarProps {
  value: string;
  onChange: (value: string) => void;
}

export function SearchBar({ value, onChange }: SearchBarProps) {
  return (
    <div className="search-box">
      <span>🔍</span>
      <input
        type="text"
        placeholder="本文で検索"
        value={value}
        onChange={(e) => onChange(e.target.value)}
      />
    </div>
  );
}
