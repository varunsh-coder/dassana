from json import load
from typing import Dict, Any

from aws_lambda_powertools.utilities.typing import LambdaContext
from aws_lambda_powertools.utilities.validation import validator
from iteration_utilities import deepflatten

from dassana.common.aws_client import DassanaAwsObject

with open('input.json', 'r') as schema:
    schema = load(schema)
    dassana_aws = DassanaAwsObject()


@validator(inbound_schema=schema)
def handle(event: Dict[str, Any], context: LambdaContext):
    arn = event.get('securityGroupArn')
    group_id = arn.get('resource')
    region = arn.get('region')

    ec2_client = dassana_aws.create_aws_client(context, 'ec2', region=region)

    filters = [
        [{
            'Name': 'ip-permission.cidr',
            'Values': [
                '0.0.0.0/0',
                '::/0',
            ]
        }],
        [{
            'Name': 'ip-permission.ipv6-cidr',
            'Values': [
                '0.0.0.0/0',
                '::/0',
            ]
        }]
    ]

    open_groups = set(
        map(lambda sg: sg['GroupId'],
            deepflatten(list(map(lambda filter_sg: ec2_client.describe_security_groups(
                GroupIds=[group_id],
                Filters=filter_sg
            )['SecurityGroups'], filters)), types=list)))

    return {"result": len(open_groups) > 0}
