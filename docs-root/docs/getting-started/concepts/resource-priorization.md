> All men are created equal. Cloud resources, not so much.

This workflow's purpose is to figure out how important the resource is. For example, if a resource has a tag of `environment` with value `dev`, the resource isn't as valuable as the one with value `prod`. After all of the steps defined in the workflow have been run, the resource priority is set using the configuration defined in the `risk-config` section of the workflow. Have a look at [AWS tag based resource prioritization workflow](https://github.com/dassana-io/dassana/blob/main/content/workflows/resource-priority/tags.yaml).
