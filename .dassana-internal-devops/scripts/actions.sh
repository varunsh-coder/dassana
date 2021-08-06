find content/actions -type d -maxdepth 1 -mindepth 1 -print0 | while read -d $'\0' dir
do
  readme=$(cat "$dir/README.md")
  yq -j eval "$dir/dassana-action.yaml" | jq --arg readme "$readme" --arg dir "$dir"  '. += {"README" : $readme, "rel-path" : $dir}'
done
