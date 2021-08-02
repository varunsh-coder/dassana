# Putting it together

Now that you understand how the magic happens. Let's review the terminology and walk through a summary of the whole process.

## Terminology

| Syntax   | Description                                                                                                     |
| -------- | --------------------------------------------------------------------------------------------------------------- |
| Engine   | Responsible for running workflows against an input JSON                                                         |
| Workflow | Responsible for running actions and available in three flavors: normalize, resource priority, and contextualize |
| Action   | Responsible for doing a single thing. Require a specific input and returns some output.                         |

## Summary

Alerts are sent to the inbound queue. Then, the engine processes alerts one at a time. The engine first runs the alert against the normalize workflow. Here, the vendor is identified, and required fields are plucked out into a normalized object. Next, the engine runs the alert against the resource priority workflow. Here, resources are risk ranked based on the tags assigned to them. Finally, the engine runs the alert against the contextualize workflow. Here a security policy determines the contextual risk of the alert.

## Dassana output

Once alerts are processed by the engine, they are sent to the outbound queue. A `dassana` object is added into the original alert JSON containing all the context outputted by the various workflows.

Here an example:

```json
{
	"foo": "bar",
	"dassana": {
		"context": {},
		"resourcePriority": {},
		"risk": {
			"contextualRisk": {
				"condition": ".foo contains bar",
				"riskValue": "critical",
				"name": "does foo contain bar?"
			},
			"resourcePriority": {
				"condition": ".resourceType contains user",
				"riskValue": "high",
				"name": "user resources are sensitive"
			}
		},
		"normalize": {
			"csp": "aws",
			"resourceId": "user_foo",
			"canonicalId": "arn:aws:iam::123456789012:user/user_foo",
			"service": "iam",
			"vendorPolicy": "7ca5af2c-d18d-4004-9ad4-9c1fbfcab218",
			"alertId": "someAlertId",
			"resourceContainer": "123456789012",
			"region": "us-east-1",
			"workflowId": "some-normalizer-workflow-id",
			"resourceType": "user"
		}
	}
}
```
