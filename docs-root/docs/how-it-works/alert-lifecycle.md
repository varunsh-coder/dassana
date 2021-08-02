# The lifecycle of an alert

In the previous section, we got Dassana up and running. In this section, we will cover what is going on behind the scenes. Remember that deployment that we did during the installation? That deployed several different components in your environment.

![Magic](/img/how-it-works/magic.png)

## Inbound queue

This is the entry point for all alerts. In the case of Security Hub, alerts are automatically forwarded to the queue. In the case of other vendors, you can forward alerts to the inbound queue URL. Alerts wait in the queue until they are processed by Dassana ([more on this later](./under-the-hood)).

## Outbound queue

Once alerts are processed, they are sent to the outbound queue. Any valid JSON object sent to the inbound queue will make its way to the outbound queue regardless of whether the context was added or not. Say, for example, you sent an alert whose policy Dassana didn't recognize. This alert would still make it into the outbound queue since it was a valid JSON object. If Dassana does understand the alert in question, the alert will have an additional `dassana` object added to the original JSON containing the context.

## Native 2-way sync

For some vendors we support a 2 way sync. In the case of security hub, we add additional metadata directly to your alerts for your convenience.

## Dead letter queue

If any errors or issues occur when processing an alert, they will be sent here. Errors can occur if a non JSON object is sent to the inbound queue. Additionally, if something is not customized correctly, that too can result in an error. In this queue, you will find the original object along with an error message indicating what may have gone wrong.

## Webhooks

_Coming Soon_

## Communications

_Coming Soon_

## Ticketing

_Coming Soon_

## SOAR

_Coming Soon_

## SIEM

_Coming Soon_
