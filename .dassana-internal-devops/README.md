 | Validation Job                                                                      |                           Short Description                           |                                                                                                  Relevant Directories |
| :---------------------------------------------------------------------------------- | :-------------------------------------------------------------------: | --------------------------------------------------------------------------------------------------------------------: |
| [validate-schema](#validate-schema)                                                 |       Ensures schemas are enforced for all relevant YAML content      | [content/workflows](../content/workflows) [content/actions](../content/actions) [content/schemas](../content/schemas) |
| [validate-content-dir-and-file-structure](#validate-content-dir-and-file-structure) |        Ensures syntax conventions throughout content directory        |                                                                                                [content/](../content) |
| [validate-grammar](#validate-grammar)                                               |    Ensures JSON rules under workflows are permitted by the grammar    |                                                                             [content/workflows](../content/workflows) |
| [validate-actions-deployment](#validate-actions-deployment)                         | Ensures content/actions are ALL referenced in SAM deployment template |                       [content/pkg/template.yaml](../content/pkg/template.yaml) [content/actions](../content/actions) |
| [validate-workflow-steps](#validate-workflow-steps)                                 |         Ensures conventions surrounding steps under workflows         |                                                                             [content/workflows](../content/workflows) |



### validate-schema
This job uses [YAMALE](https://github.com/23andMe/Yamale) to validate schemas across all Dassana workflows and actions.
A small wrapper is built on top as part of the job in the Github Action which does the following: 
find and replace relevant DASSANA_XYZ variables within schema files, 
which are wired as env variables as part of `dassana-variables` step in the validate-schema job.

The following variables have been wired:
1. DASSANA_ACTIONS
ALl function-names fetched from action.yamls under content/actions
### validate-content-dir-and-file-structure
This job uses [ls-lint](https://ls-lint.org/) to ensure directories and files follow the proper conventions which
are defined in .dassana-internal-devops/.ls-lint.yml
### validate-grammar
This job uses [this script](scripts/validate-rule-grammar.py) to validate that the rules listed under
workflow filters are syntactically legitimate based off the [rules-engine](https://) endpoint.

### validate-actions-deployment
This job uses [this script](scripts/validate-actions-deployment.py) to validate that all actions listed under [actions](../content/actions)
are part of the [SAM template](../content/pkg/template.yaml) for deployment as a serverless function.
### validate-workflow-steps
This job uses [this script](scripts/validate-workflow-steps.py) to validate that all workflows follow some conventions:
1. IDs for steps must be unique