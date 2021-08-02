# AWS Multi Account Setup
In a multi-account setup, you typically have a central security account that receives alerts from other AWS accounts. This central account acts as "Delegated Administrator." Please refer to [AWS documentation](https://docs.aws.amazon.com/organizations/latest/userguide/orgs_integrate_services_list.html) for services that support this kind of setup.

When Dassana receives an alert, it automatically finds out if the alert belongs to a resource in a different AWS account. To access such resources, Dassana Engine can automatically ["assume role"](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_roles_common-scenarios_aws-accounts.html) into the target account

If such a role already exists, Dassana can use it. This role must have [SecurityAudit](https://console.aws.amazon.com/iam/home#policies/arn:aws:iam::aws:policy/SecurityAudit) policy attached to it. The name of this role is what you provide in the `CrossAccountRoleName` during deployment.

If you don't have such role already available, you can deploy [this CFT](https://github.com/dassana-io/dassana/blob/main/content/pkg/cross-account-role.yaml) using [StackSets in your AWS organization](https://aws.amazon.com/blogs/aws/new-use-aws-cloudformation-stacksets-for-multiple-accounts-in-an-aws-organization/).


:::warning

Do **not** provide ARN value (`arn:aws:iam::1234567891012:role/MyCrossAccountRoleName`) of the role, just the role name (`MyCrossAccountRoleName`).

:::

Refer to the following diagram:

![Deployment Strategies](/img/getting-started/deploymentStrategy.png)

Do you see that "Access Role"? That's the role Dassana Engine will assume. And yes, that means that Dassana Engine, does have `sts:AssumeRole` permission but don't worry, it is restricted to the role name you provide.
