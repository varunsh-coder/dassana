from json import load, loads, dumps
from typing import Dict, Any

from aws_lambda_powertools.utilities.typing import LambdaContext
from aws_lambda_powertools.utilities.validation import validator
from dassana.common.aws_client import DassanaAwsObject, parse_arn

with open('input.json', 'r') as schema:
    schema = load(schema)
    dassana_aws = DassanaAwsObject()

@validator(inbound_schema=schema)
def handle(event: Dict[str, Any], context: LambdaContext):
    arn = parse_arn(event.get('canonicalId'))
    client = dassana_aws.create_aws_client(context, 'ec2', arn.get('region'))

    result = client.describe_network_interfaces(Filters=[
        {
            'Name': 'group-id',
            'Values': [arn.get('resource')]
        }
    ]
    )
    result = dumps(result.get('NetworkInterfaces'), default=str)
    return {"result": loads(result)}
