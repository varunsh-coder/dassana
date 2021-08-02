# Prisma Cloud

In this section, we will cover how to wire up your Prisma Cloud alerts to get supercharged by Dassana!

## Copy inbound queue URL

1. In the AWS console, navigate to the `Simple Queue Service`.

![SQS](/img/guides/vendor-setup/prisma-cloud/sqs.png)

2. Click on the `DassanaInboundQueue`.

![DassanaInboundQueue](/img/guides/vendor-setup/prisma-cloud/inboundQueue.png)

3. Copy the queue URL.

![Copy Queue URL](/img/guides/vendor-setup/prisma-cloud/copyQueueUrl.png)

## Add SQS integration in Prisma Cloud console

1. Navigate to the `Integrations` page in the Prisma Cloud console.

![Integrations](/img/guides/vendor-setup/prisma-cloud/integrationsPage.png)

2. Click the `Add Integration` button.

3. We will add an `SQS` integration. You can add a name of your choosing.

4. Paste the `Queue URL` that we copied from the AWS console.

![Add SQS Integration](/img/guides/vendor-setup/prisma-cloud/addSqsQueue.png)

5. `Test` the integration.

6. Once you see the test success message, click `Save`.

![Test and Save](/img/guides/vendor-setup/prisma-cloud/testAndSave.png)

## Forward alerts to Dassana via alert rules

1. Navigate to the alert rules page.

![Alert Rules](/img/guides/vendor-setup/prisma-cloud/alertRules.png)

2. Select on the alert rule you would like to have send alerts to Dassana.

3. Navigate to the final `Set Alert Notification` step.

4. Enable the `SQS` integration and select the queue that you just created in the previous section.

![Forward alerts to Dassana](/img/guides/vendor-setup/prisma-cloud/wireUpAlertRule.png)
