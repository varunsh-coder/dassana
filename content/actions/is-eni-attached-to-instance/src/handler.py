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
    client = dassana_aws.create_aws_client(event, context, 'ec2')
    resp = client.describe_instances(
        InstanceIds=[
            event['instanceId']
        ]
    )
    answer = False
    assert len(resp["Reservations"]) == 1
    payload = resp["Reservations"][0]["Instances"]
    answer = 'NetworkInterfaces' in payload and any(list(
        map(lambda x: x['Status'] == 'in-use', payload['NetworkInterfaces'])))
    return {"result": answer}
