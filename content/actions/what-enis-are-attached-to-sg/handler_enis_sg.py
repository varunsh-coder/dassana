from json import load, loads, dumps
from typing import Dict, Any

from aws_lambda_powertools.utilities.typing import LambdaContext
from aws_lambda_powertools.utilities.validation import validator
from dassana.common.aws_client import DassanaAwsObject
from os.path import dirname

with open('%s/input.json' % dirname(__file__), 'r') as schema:
    schema = load(schema)
    dassana_aws = DassanaAwsObject()


@validator(inbound_schema=schema)
def handle(event: Dict[str, Any], context: LambdaContext):
    group_id = event.get('groupId')
    client = dassana_aws.create_aws_client(context=context, service='ec2',
                               region=event.get('region'))

    result = client.describe_network_interfaces(Filters=[
        {
            'Name': 'group-id',
            'Values': [group_id]
        }
    ]
    )
    result = dumps(result.get('NetworkInterfaces'), default=str)
    return {"result": loads(result)}
