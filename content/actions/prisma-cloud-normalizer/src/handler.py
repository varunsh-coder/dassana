from json import loads

from aws_lambda_powertools.utilities.parser import event_parser, parse
from aws_lambda_powertools.utilities.typing import LambdaContext
from normalize.models import PrismaAlert

from dassana.common.aws_client import parse_arn
from dassana.common.models import NormalizedOutput


@event_parser(model=PrismaAlert)
def handle(event: PrismaAlert, context: LambdaContext):
    if event.message is not None:  # Splunk Event
        event = parse(event.message, model=PrismaAlert)

    arn_obj = parse_arn(event.resource.data.arn) if event.resource.data.arn else None
    if (event.tags is not None) and len(event.tags) > 0:
        tags = list(map(lambda x: {
            "value": x.get('value'),
            "name": x.get('key')
        }, event.tags))
    else:
        tags = []

    if event.alertId and event.policyId:  # SQS
        alert_id, vendor_policy = event.alertId, event.policyId
    elif event.id and event.policy.policyId:  # Prisma
        alert_id, vendor_policy = event.id, event.policy.policyId
    else:
        alert_id, vendor_policy = None, None

    output = NormalizedOutput(
        vendorId='prisma-cloud',
        alertId=alert_id,
        canonicalId=event.resource.data.arn,
        vendorPolicy=vendor_policy,
        csp=event.resource.cloudType,
        resourceContainer=event.resource.accountId,
        region=event.resource.regionId,
        service=arn_obj.service if arn_obj is not None else None,
        resourceType=arn_obj.resource_type if arn_obj is not None else None,
        resourceId=arn_obj.resource if arn_obj is not None else event.resource.id,
        tags=tags
    )
    return loads(output.json())
