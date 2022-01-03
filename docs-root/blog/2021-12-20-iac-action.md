---
slug: iac-context
title: Supercharging IaC Scans
author: Kaushik Devireddy
author_title: Intern
author_url: https://github.com/kloading
author_image_url: https://avatars.githubusercontent.com/u/26693584?v=4
tags: [aws, contextualization, checkov, github]
no_comments: false
---

We are excited to launch the [Dassana Infrastructure as Code (IaC) Github Action](https://github.com/marketplace/actions/dassana-iac-github-action) to supercharge your shift-left security strategy. The Github action runs every time you edit your CloudFormation templates, reporting [Checkov’s](https://github.com/bridgecrewio/checkov) static analysis findings enhanced with [Dassana’s dynamic cloud context](https://github.com/dassana-io/dassana). Now, your DevSecOps teams can enjoy contextualized IaC coverage, reducing alert fatigue.

Here’s a quick glance at what the IaC action is capable of:

![Demo](/img/blog/iac-action/dassana-iac-demo.png)
_An edit was made to an S3 bucket in a Cloudformation template, triggering Dassana IaC. The corresponding Checkov alerts, enhanced with the cloud context of the S3 bucket, are automatically posted to the pull-request. In this case, Dassana queried the appropriate AWS APIs and recognized that the S3 bucket is in a dev environment, contains minimal objects, and has static site hosting enabled. The general, resource, and policy risks were set according to this new context._

To understand why we built Dassana IaC, let’s first discuss how shift-left was enabled by IaC. Traditionally, access and provisioning of infrastructure were the major blockers when deploying to production. But cloud, coupled with IaC, largely solved this: developers can now codify infrastructure and spin up massive deployments in seconds, without the approval of IT. A nice side effect of infrastructure being codified is that its security can be determined by analyzing your IaC templates, allowing you to catch and remediate issues far earlier in the life cycle. Open-source tools such as Checkov do this very well. They run static scans on your IaC template and find deviations from best practices (ex. An S3 bucket doesn’t have a block public access property).

![Explainer](/img/blog/iac-action/shift-left-explainer.jpg)

However, as cloud deployments get more complex, these static scans get more noisy. Here’s an example that illustrates this. If I define a security group with several egress rules, a static scan of the IaC file will result in many critical alerts. But what if the security group will be attached to an instance running in a private subnet associated with a network ACL consisting of very tight rules? Now the configuration of the security group isn’t extremely critical. You can start to see how the context of the cloud environment is important when evaluating IaC scans. In fact, this paradigm actually extends beyond security. Let’s say I’m modifying the rules of my deployed security groups to enable connectivity for a new service. In this case, I want to prioritize alerts for security groups that are attached to the most ENIs. Dassana adds this dynamic, cloud context to static IaC scans to help you achieve the perfect balance between infrastructure security and developer velocity.

Here’s how you can deploy Dassana IaC:

1. First, make sure you’ve deployed [Dassana](https://docs.dassana.io/docs/getting-started/installation).
1. Next, head over to our [listing on the Github marketplace](https://github.com/marketplace/actions/dassana-iac-github-action) and click “Launch Stack”. This will create an IAM user for Github Actions to authenticate with your AWS environment and grab dynamic cloud context, as well as an S3 bucket to store your Cloudformation templates.
1. Once the stack is deployed, create the following secrets in your Github repository.

![Integration](/img/blog/iac-action/action-secrets.png)

4. Finally, add the action to your workflow file with your bucket name, stack name, and path to the Cloudformation template. An example workflow file with Dassana IaC is shown below.

```yaml
on:
    pull_request:
        paths:
            - 'cloudformation/template.yaml'

jobs:
    dassana-job:
        runs-on: ubuntu-latest
        name: dassana-action
        steps:
            - name: Checkout Repo
              uses: actions/checkout@v2
            - name: python-test
              uses: actions/setup-python@v2.2.2
              with:
                  python-version: 3.8
            - name: Run Dassana IaC Action
              uses: dassana-io/CloudContext@main
              with:
                  aws_region: 'us-west-2'
                  bucket_name: 'cft-gh'
                  stack_name: 'test-cf-stack'
                  template_file: './cloudformation/template.yaml'
                  github_token: ${{ secrets.GITHUB_TOKEN }}
                  aws_access_key_id: ${{ secrets.AWS_ACCESS_KEY_ID }}
                  aws_secret_access_key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
                  api_gateway_endpoint: ${{ secrets.API_GATEWAY_ENDPOINT }}
                  api_key: ${{ secrets.API_KEY }}
```

Now, whenever someone opens a pull request that edits your CloudFormation template, Dassana IaC will automatically run and post a comment containing Chekov’s findings enhanced with Dassana’s context. Under the hood, Dassana creates a CloudFormation change set in your AWS account and runs Checkov against the proposed CloudFormation template. The Change Set and Checkov scan are then reconciled to filter for relevant alerts. These alerts are finally sent to the Dassana engine for decoration with dynamic cloud context. The result is a table of findings that keeps your DevSecOps teams happy.

Dassana IaC is completely open-source and leverages two great open-source tools - Dassana and Checkov. You can support our mission of building an ecosystem of open-source security tooling by poking around [our code](https://github.com/dassana-io/dassana-iac-action) and contributing! If you have any questions or find any bugs, please visit our [support page](https://docs.dassana.io/docs/support).
