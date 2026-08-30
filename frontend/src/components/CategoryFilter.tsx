import { useEffect, useState } from "react";
import { fetchCategories, type Category } from "../api/categories";

interface CategoryFilterProps {
  value: string | null;
  onChange: (category: string) => void;
}

export function CategoryFilter({ value, onChange }: CategoryFilterProps) {
  const [categories, setCategories] = useState<Category[]>([]);

  useEffect(() => {
    fetchCategories()
      .then((result) => {
        setCategories(result);
        if (!value && result.length > 0) {
          onChange(result[0].name);
        }
      })
      .catch(() => {
        setCategories([]);
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className="category-filter">
      <select value={value ?? ""} onChange={(e) => onChange(e.target.value)}>
        {categories.map((category) => (
          <option key={category.id} value={category.name}>
            {category.name}
          </option>
        ))}
      </select>
    </div>
  );
}
