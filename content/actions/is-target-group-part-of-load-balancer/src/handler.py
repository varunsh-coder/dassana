from functools import reduce
from json import load
from operator import add
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
    client = dassana_aws.create_aws_client(event, context, 'elbv2')
    resp = client.describe_target_groups(
        TargetGroupArns=[
            event['targetGroupArn']
        ],
    )

    return reduce(
        add,
        map(lambda x: len(x['LoadBalancerArns']), resp['TargetGroups'])
    ) > 0
