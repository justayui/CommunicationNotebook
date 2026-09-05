const TAG_COLOR_CLASSES = ["tag-blue", "tag-green", "tag-purple", "tag-rose", "tag-orange", "tag-teal"];

export function getTagColorClass(category: string): string {
  let hash = 0;
  for (let i = 0; i < category.length; i++) {
    hash = (hash * 31 + category.charCodeAt(i)) % TAG_COLOR_CLASSES.length;
  }
  return TAG_COLOR_CLASSES[hash];
}
