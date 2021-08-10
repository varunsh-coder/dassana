find content/workflows -name "*.yaml" -print0 | while read -d $'\0' file
do
  # yq -j eval $file | jq '.'
  yq -j eval $file | \
  jq --arg relpath "$file" \
  '{policy: ., "rel-path": $relpath}'
done
