from json import load, loads, dumps
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
    client = dassana_aws.create_aws_client(event, context, 'ec2')
    result = client.describe_network_interfaces(Filters=[
        {
            'Name': 'group-id',
            'Values': [
                event.get('groupId')
            ]
        }
    ]
    )
    result = dumps(result.get('NetworkInterfaces'), default=str)
    return {"result": loads(result)}
