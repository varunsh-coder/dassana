## IDE Setup

Follow these steps to run Dassana Engine in your IDE. The engine will execute Dassana Actions (functions) so make sure
to deploy Dassana CFT template in your account first. Refer to
instructions [here](https://docs.dassana.io/docs/getting-started/installation)

Set the following env vars before running `app.dassana.core.runmanager.launch.App` (which invokes the Dassana Engine api) :

```bash 
AWS_REGION=<region>;
dassanaBucket=<refer to the cloudformation resources in AWS console and figure out which s3 bucket was created by 
CFT deployment>;
LOCAL_DEV_LOGGING=;
DASSANA_STACK_NAME=<name of the stack>
```

## Important content related note

The content that the engine runs is located at the sibling directory `content`, as such the `pom.xml` file has
`copy-resources` maven goal to copy the content from content directory to the `/target/classes/workflows` directory.

When you run `mvn install` etc, that goal is automatically executed but if you are running the Engine in your IDE, make
sure that you run `mvn clean process-resources` command first. You **must** run this command if you make any changes to
the content otherwise the changes won't be picked up by your IDE. 

