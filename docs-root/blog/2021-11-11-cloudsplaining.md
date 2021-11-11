---
slug: cloudsplaining
title: Prioritizing alerts based on risky IAM permissions
author: Parth Shah
author_title: Co-Founder & Head of Product and Engineering
author_url: https://github.com/parth-dassana
author_image_url: https://avatars.githubusercontent.com/u/68707443?v=4
tags: [aws, contextualization, iam, permissions]
no_comments: false
---

We are excited to announce support for [cloudsplaining](https://github.com/salesforce/cloudsplaining). Cloudsplaining is a powerful tool that analyzes AWS IAM policies and identifies risky permissions related to privilege escalation, credentials exposure, data exfiltration, resource exposure, and infrastructure modification. Moving forward, whenever you receive an alert for an EC2 instance or an IAM user, rest assured, you will get context about risky IAM permissions.

Interested? Let's dive deeper.

<!--truncate-->

Before we jump into the how, let's do a quick refresher on how Dassana works.

As soon as an alert is received, it gets normalized (regardless of the vendor). Normalization is our way of thinking about security from first principles. Here is sample output from our normalizer:

```yaml
{
    'csp': 'aws',
    'resourceId': 'i-xxxxxxxxxx',
    'canonicalId': 'arn:aws:ec2:us-east-1:yyyyyyyyyyyy:instance/i-xxxxxxxxxx',
    'vendorSeverity': 'high',
    'vendorId': 'aws-config',
    'tags': [{ 'name': 'env', 'value': 'prod' }],
    'alertClassification':
        {
            'subclass': 'config',
            'category': 'networking',
            'subcategory': 'firewall',
            'class': 'risk'
        },
    'service': 'ec2',
    'vendorPolicy': 'ec2-instance-no-public-ip',
    'alertId': 'zzzzzzzzzz',
    'resourceContainer': 'yyyyyyyyyyyy',
    'region': 'us-east-1',
    'resourceType': 'instance'
}
```

The normalization output is fed to the following three workflow types where each can provide a risk value (critical, high, medium, low, '', accepted):

1. General context - Environment (dev/prod etc.) or category-based (IAM, Auth, Encryption, etc.) based alert prioritization happens here. This is covered in-depth in our last [Prisma Cloud blog entry](https://docs.dassana.io/blog/prisma-cloud).
2. Resource context - Resource type (ec2/s3 etc.) based alert prioritization happens here. More on this below!
3. Policy context - Anything related to the policy which generated the alert goes here. This is covered in-depth in our previous [static s3 bucket blog entry](https://docs.dassana.io/blog/static-s3-bucket).

These contextualization workflows run in parallel. Think of them as "dimensions" of risk ([learn more](https://docs.dassana.io/docs/how-it-works/low-level/)). For this demo, I'm only going to use the resource context workflow.

Here is my resource context workflow for EC2 instances. Don't worry about the file's contents just yet. I will help break it down, so you understand how the magic happens.

```yaml
schema: 1
type: resource-context

id: ec2-instance-context
name: EC2 instance resource context

csp: aws
service: ec2
resource-type: instance

filters:
    - match-type: any
      rules:
          - .resourceType == "instance" and .csp =="aws"

steps:
    - id: iamRole
      uses: GetEc2InstanceIAMrole
      with:
          - name: instanceId
            value: .resourceId
          - name: region
            value: .region
    - id: riskyPermissions
      uses: GetIAMPolicyRisks
      with:
          - name: iamArn
            value: .steps.iamRole.result.roleArn
          - name: region
            value: .region

risk-config:
    default-risk: ''
    rules:
        - id: risky-permissions
          condition: .riskyPermissions | length > 0
          risk: ''
          subrules:
              - id: instance-has-permissions-that-allow-privilege-escalation
                condition: .riskyPermissions.result.PrivilegeEscalation | length > 0
                risk: high

              - id: instance-has-permissions-that-allow-credentials-exposure
                condition: .riskyPermissions.result.CredentialsExposure | length > 0
                risk: high

              - id: instance-has-permissions-that-allow-data-exfiltration
                condition: .riskyPermissions.result.DataExfiltration | length > 0
                risk: medium

              - id: instance-has-permissions-that-allow-infrastructure-modification
                condition: .riskyPermissions.result.InfrastructureModification | length > 0
                risk: low

              - id: instance-has-permissions-that-allow-resource-exposure
                condition: .riskyPermissions.result.ResourceExposure | length > 0
                risk: low
```

First and foremost, I mentioned that this resource context workflow is for EC2 instances only. Why is that? Take a look at the filter condition:

```yaml
filters:
    - match-type: any
      rules:
          - .resourceType == "instance" and .csp =="aws"
```

Because every alert is normalized, we can target a specific `resourceType`. In this case, it's AWS EC2 instances.

Next, let's take a look at the `steps`:

```yaml
steps:
    - id: iamRole
      uses: GetEc2InstanceIAMrole
      with:
          - name: instanceId
            value: .resourceId
          - name: region
            value: .region
    - id: riskyPermissions
      uses: GetIAMPolicyRisks
      with:
          - name: iamArn
            value: .steps.iamRole.result.roleArn
          - name: region
            value: .region
```

Each step is essentially just running a lambda function (aka action) behind the scenes to grab some context. In the case of EC2 instances, we are running two actions:

1. `GetEc2InstanceIAMrole` - This action gets the IAM role attached to the EC2 instance. Here's the [code](https://github.com/dassana-io/dassana/tree/main/content/actions/what-is-ec2-role) in case you want to geek out.

The output of this lambda function looks like the following:

```json
{
	"result": {
		"roleArn": "arn:aws:iam::yyyyyyyyyy:role/my-overly-permissive-role",
		"roleName": "my-overly-permissive-role"
	}
}
```

2. `GetIAMPolicyRisks` - This action runs cloudsplaining on the `roleArn` (which we received thanks to the `GetEc2InstanceIAMrole` action) to help identify risky permissions. Here's the [code](https://github.com/dassana-io/dassana/tree/main/content/actions/get-iam-policy-risks).

The output of this action looks like the following (truncated for readability):

```json
{
  "result": {
    "ResourceExposure": [
      "iam:DeleteUserPolicy",
      "apigateway:UpdateRestApiPolicy",
      "iam:DeleteGroupPolicy",
      "iam:DeletePolicy",
      ...
    ],
    "PrivilegeEscalation": [
      {
        "type": "CreateAccessKey",
        "actions": ["iam:createaccesskey"]
      },
      {
        "type": "CreateLoginProfile",
        "actions": ["iam:createloginprofile"]
      },,
      ...
    ],
    "DataExfiltration": [
      "ssm:GetParameter",
      "s3:GetObject",
      "ssm:GetParameters",
      "secretsmanager:GetSecretValue",
      "ssm:GetParametersByPath",
      ...
    ],
    "CredentialsExposure": [
      "redshift:GetClusterCredentials",
      "sts:AssumeRole",
      "iam:UpdateAccessKey",
      ...
    ],
    "InfrastructureModification": [...],
    "ServicesAffected": [...],
    "ServiceWildcard": [...]
  }
}
```

Now that we have all of this juicy context in hand, we can write rules to assess the risk of the EC2 instance better:

```yaml
risk-config:
    default-risk: '' # Empty string as risk means unknown / unset
    rules:
        - id: risky-permissions
          condition: .riskyPermissions | length > 0
          risk: ''
          subrules:
              - id: instance-has-permissions-that-allow-privilege-escalation
                condition: .riskyPermissions.result.PrivilegeEscalation | length > 0
                risk: high

              - id: instance-has-permissions-that-allow-credentials-exposure
                condition: .riskyPermissions.result.CredentialsExposure | length > 0
                risk: high

              - id: instance-has-permissions-that-allow-data-exfiltration
                condition: .riskyPermissions.result.DataExfiltration | length > 0
                risk: medium

              - id: instance-has-permissions-that-allow-infrastructure-modification
                condition: .riskyPermissions.result.InfrastructureModification | length > 0
                risk: low

              - id: instance-has-permissions-that-allow-resource-exposure
                condition: .riskyPermissions.result.ResourceExposure | length > 0
                risk: low
```

In my case, I'm simply checking to see if any risky IAM actions were identified by cloudsplaining. Based on the type of risky permissions identified (ie privilege escalation, credentials exposure, etc.), I'm setting the resource risk accordingly. What I'm showing is barely the tip of the iceberg. You can be surgical and write rules that target specific permissions.

In case you missed it earlier, everything we just talked about applies to any IAM user-related alert as well, thanks to the [IAM user resource context workflow](https://github.com/dassana-io/dassana/blob/main/content/workflows/csp/aws/service/iam/resources/user/resource-context/user-context.yaml).

That's it for today. Hope you enjoyed learning about the simplicity and versatility of resource risk workflows coupled with lightweight and powerful context-adding actions like cloudsplaining. Until next time.
