from json import load, loads, dumps
from typing import Dict, Any

from aws_lambda_powertools.utilities.typing import LambdaContext
from aws_lambda_powertools.utilities.validation import validator
from dassana.common.aws_client import DassanaAwsObject

with open('input.json', 'r') as schema:
    schema = load(schema)
    dassana_aws = DassanaAwsObject()


def parse_arn(arn):
    elements = arn.split(':', 5)
    result = {
        'arn': elements[0],
        'partition': elements[1],
        'service': elements[2],
        'region': elements[3],
        'account': elements[4],
        'resource': elements[5],
        'resource_type': None
    }
    if '/' in result['resource']:
        result['resource_type'], result['resource'] = result['resource'].split('/', 1)
    elif ':' in result['resource']:
        result['resource_type'], result['resource'] = result['resource'].split(':', 1)
    return result


@validator(inbound_schema=schema)
def handle(event: Dict[str, Any], context: LambdaContext):
    arn = parse_arn(event.get('canonicalId'))
    client = dassana_aws.create_aws_client(event, context, 'ec2', arn.get('region'))

    result = client.describe_network_interfaces(Filters=[
        {
            'Name': 'group-id',
            'Values': [arn.get('resource')]
        }
    ]
    )
    result = dumps(result.get('NetworkInterfaces'), default=str)
    return {"result": loads(result)}
