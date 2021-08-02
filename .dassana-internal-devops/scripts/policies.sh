find content/workflows/policies -name "*.yaml" -print0 | while read -d $'\0' file
do
  fileArr=(${file//// })
  cloud=${fileArr[4]}
  service=${fileArr[6]}
  resource=${fileArr[8]}
  # yq -j eval $file | jq '.'
  yq -j eval $file | \
  jq --arg cloud "$cloud" \
     --arg service "$service" \
     --arg resource "$resource" \
     --arg relpath "$file" \
  '{cloud: $cloud, service: $service, resource: $resource, policy: ., "rel-path": $relpath}'
done
