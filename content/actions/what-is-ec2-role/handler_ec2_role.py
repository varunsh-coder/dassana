from json import load, loads, dumps
from typing import Dict, Any

from aws_lambda_powertools.utilities.typing import LambdaContext
from aws_lambda_powertools.utilities.validation import validator
from botocore.exceptions import ClientError

from dassana.common.aws_client import DassanaAwsObject

from os.path import dirname

with open('%s/input.json' % dirname(__file__), 'r') as schema:
    schema = load(schema)
    dassana_aws = DassanaAwsObject()


@validator(inbound_schema=schema)
def handle(event: Dict[str, Any], context: LambdaContext):
    instance_id = event.get('instanceId')

    client = dassana_aws.create_aws_client(context=context, service='ec2',
                               region=event.get('region'))
    try:
        result = client.describe_iam_instance_profile_associations(
            Filters=[{
                'Name': 'instance-id',
                'Values': [instance_id]
            }]
        )
    except ClientError as e:
        raise e

    associations = result['IamInstanceProfileAssociations']
    role_arn = ''
    if len(associations) == 1:
        role_arn = associations[0]['IamInstanceProfile']['Arn']

    role_arn = role_arn.replace("instance-profile", "role", 1)

    demarcation = role_arn.find('/')
    if demarcation < 0:
        demarcation = role_arn.rfind(':')

    role_name = role_arn[demarcation + 1:]
    result = dumps({"roleName": role_name, "roleArn": role_arn}, default=str)
    return {"result": loads(result)}
