## Local Dev Setup Requirements

Make sure to deploy Dassana CFT template in your account first.

Set the following env vars before running `app.dassana.core.launch.App`.

```bash 
AWS_REGION=<region>;
dassanaBucket=<refer to the cloudformation resources in AWS console and figure out which s3 bucket was created by 
CFT deployment>;
LOCAL_DEV_LOGGING=;
DASSANA_STACK_NAME=<name of the stack>
```
