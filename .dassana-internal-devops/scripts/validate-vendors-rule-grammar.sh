while read file; do
  while read line; do
    jq -n "$line"
  done < <(yq -j e "$file" | jq -r '.filter.rules?[]')
done < <(find "content/workflows/vendors" -name "*.yaml")