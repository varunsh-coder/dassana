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
    client = dassana_aws.create_aws_client(event, context, 'elbv2')
    resp = client.describe_load_balancers(
        LoadBalancerArns=[
            event['loadBalancerArn']
        ]
    )
    assert len(resp['LoadBalancers']) == 1
    payload = resp['LoadBalancers'][0]
    return list(map(lambda x: x['SubnetId'], payload['AvailabilityZones']))
