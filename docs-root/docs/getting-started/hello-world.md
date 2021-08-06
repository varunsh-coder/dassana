# Hola World

Let's send an alert to Dassana and see what happens.

But first, the basics. Dassana, being a cloud native product, has an inbuilt integration with SecurityHub and to generically process alerts, it creates three queues -

1. DassanaInboundQueue: This is where to send alerts to for processing.
1. DassanaOutboundQueue: This is where processed alerts are put by Dassana.
1. DassanaDeadLetterQueue: Any unprocessed alert due to any error are sent here.

### Send a test json

To test things out, go to the SQS service in the AWS console and select `DassanaInboundQueue` and then click `Send and receive messages` and put this in the `Message body` textbox:

```json
{
	"foo": "bar"
}
```

and click `Send message`

Give yourself about 30 seconds and head over to `DassanaOutboundQueue` and click on `Send and receive messages`, scroll down and in the `Receive messages` section, click on `Poll for messages` and you should see a message over there. Click on it and you should see something like this in the Body-

```json
{
	"foo": "bar",
	"dassana": {
		"context": {
			"hints": [
				{
					"severity": "WARN",
					"msg": "No contextualization workflows were run, check filter config of the workflow you intended to run"
				}
			]
		},
		"timing": {
			"contextualization": 0,
			"resourcePrioritization": 0,
			"normalize": 0,
			"totalTimeTaken": 661
		},
		"normalize": {
			"hints": [
				{
					"severity": "WARN",
					"msg": "No normalizers were run, ensure that the filter is correctly set in the worklow that you intended ro run"
				}
			]
		}
	}
}
```

:::danger

G to update JSON

:::

What just happened? Why am I seeing these message with warnings?
Well, for one, let's focus on the positive side of things first. You see, whatever you sent as input, `{"foo":"bar"}` is available in output and Dassana has decorated it by adding a new JSON key `dassana`.

As for the the warning messages, did you really expect anything fancier from a foo-bar example json?

### Send a test alert json

Let's send something interesting this time. Head back to the `DassanaInboundQueue` and put this json this time -

```json
{
	"badThingJustHappened": {
		"description": "fubar"
	}
}
```

and you guessed it right, we now go to `DassanaOutboundQueue` and follow the drill of clicking on `Send and receive messages` followed by `Poll for messages` and you shall see a message waiting for you. Open it and it should look like this -

```json
{
	"dassana": {
		"timing": {
			"contextualization": 0,
			"resourcePrioritization": 4326,
			"normalize": 7119,
			"totalTimeTaken": 11531
		},
		"normalize": {
			"csp": "demo-cloud",
			"resourceId": "foo",
			"canonicalId": "universe/earth/demo-cloud/some-region/some-service/foo",
			"service": "some service",
			"vendorPolicy": "yolo",
			"alertId": "yolo",
			"resourceContainer": "some-account-id",
			"post-processor": {},
			"region": "some-region",
			"workflowId": "foo-cloud-normalize",
			"resourceType": "some-resource-type"
		},
		"context": {
			"hints": [
				{
					"severity": "WARN",
					"msg": "No contextualization workflows were run, check filter config of the workflow you intended to run"
				}
			]
		},
		"risk": {
			"resourcePriority": {
				"condition": "$.vendorPolicy is \"yolo\"",
				"riskValue": "low",
				"name": "yolo risk is low"
			}
		},
		"alertKey": "s3://<foo>-kcc9cwbu9dca/2021/7/27/foo-cloud-normalize/yolo"
	},
	"badThingJustHappened": {
		"description": "fubar"
	}
}
```

:::danger

G to update JSON

:::

### Normalization

We see much better result this time! What just happened? For that have a look at [this](https://github.com/dassana-io/dassana/blob/main/content/workflows/vendors/foo-cloud/normalize.yaml) workflow file. Notice this part-

```yaml
filters:
	- match-type: all
      rules:
    	- $.badThingJustHappened.description contains fubar
```

Here, we just told Dassana, that when JSON path `$.badThingJustHappened.description` `contains` `fubar` (We will get to this in a moment), we want the workflow to run. This workflow performs [normalization](../guides/workflow-authoring/normalize) using the `steps`. We have this step -

```yaml
steps:
    - id: demo-resource-info
      uses: DemoCloudNormalizer
```

What is this step doing? Well, it is simply calling a serverless function named `DemoCloudNormalizer` in the [CFT template](https://github.com/dassana-io/dassana/blob/main/content/pkg/template.yaml#L536) we deployed. You can refer to the [source code](https://github.com/dassana-io/dassana/blob/main/content/actions/demo-cloud-normalizer/src/handler.py) here but it doesn't do anything super interesting but does normalize every single alert to same static data.

### General Context

Next, your honor, I present to you [this General Context workflow](https://github.com/dassana-io/dassana/blob/main/content/workflows/csp/demo-cloud/general-context/demo-cloud-service.yaml). This workflow assigns a risk to the resource in question. Notice the following config in the workflow -

```yaml
risk-config:
    default-risk: ''
    rules:
        - name: yolo risk is low
          condition: |-
              $.vendorPolicy is "yolo"
          risk: low
```

:::info

In the workflows of type `general-context`, the `risk-config` tells Dassana the risk of the resource.

:::

Again, the condition here uses JSON rule (yes,yes, we are going to get to it, beer🍺 with for us for a moment please). The condition here is basically telling Dassana that when the normalized vendor policy is `yolo`, we want the resource risk to be low. And that's what causes this output:

```json
"risk": {
  "resourcePriority": {
    "condition": "$.vendorPolicy is \"yolo\"",
    "riskValue": "low",
    "name": "yolo risk is low"
  }
}
```

:::danger

Update JSON

:::

Currently the `demo-cloud` example we use does not have any resource context or policy context workflow so that's why you see this message processed json

```json
"msg": "No contextualization workflows were run, check filter config of the workflow you intended to run"
```

:::danger

Update message

:::

### JSON Rules

JSON rules are the standardized way in Dassana to filter things. We have [open-sourced](https://github.com/dassana-io/rule-engine) our rule engine which takes a JSON input, a JSON rule and returns `true` or `false` based upon the json rule evaluation.

We recommend you checkout [examples](https://github.com/dassana-io/rule-engine/blob/main/README.md#examples) to get a flavor of how these rules work.

## What's next?

Checkout some of the [vendor integrations](../guides/vendor-setup/security-hub/setup.md) to process some real alerts this time. Or, if you hungry, order a pizza 🍕 with pineapple 🍍
