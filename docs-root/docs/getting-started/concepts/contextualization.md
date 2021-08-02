> Context is king 👑

Much like resource prioritization workflow, this workflow takes the same input that the normalization workflow outputted and runs a series of steps that add context to the alert. Have a look at [SSH from internet workflow](https://github.com/dassana-io/dassana/blob/main/content/workflows/policies/csp/aws/service/ec2/resources/security-group/ssh-from-internet.yaml) which sets the risk to low if there are no associated networking interfaces to the security group.

After all of the steps defined in the workflow has been run, the contextual risk is set using the configuration defined in the `risk-config` section of the workflow

You may also think of this workflow as normalized "policy workflow" as different policies from vendors are normalized in the filter section.
