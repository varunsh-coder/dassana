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
    resp = client.describe_security_groups(
        GroupIds=[
            event['groupId']
        ]
    )
    assert len(resp['SecurityGroups']) == 1
    answer = False
    for sg in resp['SecurityGroups'][0]['IpPermissions']:
        if len(list(filter(
            lambda x: x['CidrIp'] == '0.0.0.0/0'
                      or x['CidrIp'] == '::/0', sg['IpRanges']))) > 0:
            answer = True
            break
        elif len(list(filter(
            lambda x: x['CidrIp'] == '0.0.0.0/0' or x['CidrIp'] == '::/0',
            sg['Ipv6Ranges']))) > 0:
            answer = True
            break
    return {"result": answer}
