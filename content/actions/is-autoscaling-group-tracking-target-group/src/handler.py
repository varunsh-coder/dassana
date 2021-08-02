from json import load
from typing import Dict, Any

from aws_lambda_powertools.utilities.typing import LambdaContext
from aws_lambda_powertools.utilities.validation import validator

from dassana.common.aws_client import DassanaAwsObject

with open('input.json', 'r') as schema:
    schema = load(schema)
    dassana_aws = DassanaAwsObject(
        schema['properties']['awsRegion']['default'])


@validator(inbound_schema=schema)
def handle(event: Dict[str, Any], context: LambdaContext):
    client = dassana_aws.create_aws_client(event, context, 'autoscaling')

    resp = client.describe_auto_scaling_groups(
        AutoScalingGroupNames=[
            event['autoScalingGroupName']
        ]
    )
    assert len(resp['AutoScalingGroups']) == 1
    answer = False
    for asg in resp['AutoScalingGroups']:
        if (len(asg['TargetGroupArns']) > 0):
            answer = True
            break
    return {"result": answer}
