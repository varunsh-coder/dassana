This workflow adds policy specific context to the alert. Have a look at [SSH from internet workflow](https://github.com/dassana-io/dassana/blob/main/content/workflows/csp/aws/service/ec2/resources/security-group/policy-context/ssh-from-internet.yaml) which sets the risk to low if there are no networking interfaces associated to the security group.

After all of the steps defined in the workflow has been run, the risk is set using the configuration defined in the `risk-config` section of the workflow.
