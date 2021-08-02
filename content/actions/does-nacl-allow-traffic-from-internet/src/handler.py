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
    resp = client.describe_network_acls(
        NetworkAclIds=[
            event['networkAclId']
        ]
    )
    answer = False
    assert len(resp['NetworkAcls']) == 1
    for entry in resp['NetworkAcls'][0]['Entries']:
        if entry['Egress'] is False and entry['Protocol'] == '-1' and entry[
            'RuleAction'] == 'allow':
            answer = True
            break
    return {"result": answer}
