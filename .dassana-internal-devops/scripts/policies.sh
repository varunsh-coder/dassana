find content/workflows/csp/aws/service -name "*.yaml" -print0 | while read -d $'\0' file
do
  fileArr=(${file//// })
  cloud=${fileArr[3]}
  service=${fileArr[5]}
  resource=${fileArr[7]}
  # yq -j eval $file | jq '.'
  yq -j eval $file | \
  jq --arg cloud "$cloud" \
     --arg service "$service" \
     --arg resource "$resource" \
     --arg relpath "$file" \
  '{cloud: $cloud, service: $service, resource: $resource, policy: ., "rel-path": $relpath}'
done
