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
    client = dassana_aws.create_aws_client(event, context, 'elbv2')
    lb_paginator = client.get_paginator('describe_load_balancers')
    tg_paginator = client.get_paginator('describe_target_groups')
    page_iterator = lb_paginator.paginate()
    for lb_page in page_iterator:
        for lb_arn in list(map(lambda x: x['LoadBalancerArn'],
                               lb_page['LoadBalancers'])):
            for tg_page in tg_paginator.paginate(LoadBalancerArn=lb_arn):
                target_groups = list(
                    filter(lambda x: x['TargetType'] == 'instance',
                           tg_page['TargetGroups']))
                for tg in target_groups:
                    resp = client.describe_target_health(
                        TargetGroupArn=tg['TargetGroupArn'])
                    if any(list(
                        map(lambda x: x['Target']['Id'] == event['instanceId'],
                            resp['TargetHealthDescriptions']))):
                        return True
    return False
