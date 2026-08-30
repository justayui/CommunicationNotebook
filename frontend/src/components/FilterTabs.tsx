export type FilterTab = "all" | "category" | "favorite";

interface FilterTabsProps {
  active: FilterTab;
  onChange: (tab: FilterTab) => void;
}

const TABS: { key: FilterTab; label: string }[] = [
  { key: "all", label: "全体" },
  { key: "category", label: "カテゴリ別" },
  { key: "favorite", label: "お気に入り" },
];

export function FilterTabs({ active, onChange }: FilterTabsProps) {
  return (
    <div className="header-tabs" role="tablist">
      {TABS.map((tab) => (
        <button
          key={tab.key}
          type="button"
          className="header-tab"
          aria-selected={active === tab.key}
          onClick={() => onChange(tab.key)}
        >
          {tab.label}
        </button>
      ))}
    </div>
  );
}
