while read file; do
  while read line; do
    jq -n "$line"
  done < <(yq -j e "$file" | jq -r '.filter.vendors[].rules?[]')
done < <(find "content/workflows/policies/csp" -name "*.yaml")