This workflow adds resource specific context to an alert. You may have many alerts tied to EC2 instances. Regardless of the what the issue is, it would be nice to have access to the IAM policy attached to every instance. Or more importantly, it would be nice if you could answer the question, is there a path from the internet to my instance?

After all of the steps defined in the workflow has been run, the risk is set using the configuration defined in the `risk-config` section of the workflow.

:::danger

Show example

:::
