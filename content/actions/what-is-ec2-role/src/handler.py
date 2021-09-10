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
    instance_id = event.get('instanceId')
    
    client = dassana_aws.create_aws_client(context, 'ec2', event.get('region'))
    try:
        result = client.describe_instances(
            Filters=[],
            InstanceIds=[
                instance_id,
            ],
            DryRun=False
        )
    except:
        result = dumps({"roleName": "", "roleArn": ""}, default=str)
        return {"result": loads(result)}
        
    result = dumps(result, default=str)
    result = loads(result)
    
    try:
        roleArn = result['Reservations'][0]['Instances'][0]['IamInstanceProfile']['Arn']
    except KeyError:
        roleArn = ""
    
    roleArn = roleArn.replace("instance-profile", "role", 1)
    
    demarcation = roleArn.find('/')
    if demarcation < 0:
        demarcation = roleArn.rfind(':')
        
    roleName = roleArn[demarcation+1:] 
    result = dumps({"roleName": roleName, "roleArn": roleArn}, default=str)
    return {"result": loads(result)}
