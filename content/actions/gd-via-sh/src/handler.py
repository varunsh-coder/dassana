from json import loads, load
from typing import Dict, List, Any, Optional

from aws_lambda_powertools.utilities.parser import event_parser, parse
from aws_lambda_powertools.utilities.typing import LambdaContext

from dassana.common.aws_client import parse_arn
from dassana.common.models import NormalizedOutput
from dassana.common.aws_client import LambdaTestContext

from normalize.models import GuardDutyAlert
from normalize.models import GuardDutyDirect


# @event_parser(model=GuardDutyAlert)
# def handle(event: GuardDutyAlert, context: LambdaContext):
def handle(event, context: LambdaContext):
    mode = 'n' # n --> normal - we default to raw and event bridge alert format
    if isinstance(event, list): # direct GuardDuty alert with have [{}] format
        print("it is list")
        event = parse(event[0], model=GuardDutyDirect)
        mode = 'd' # --> since direct alert have different tag names we need flag to distinguish
    elif isinstance(event, dict): # GuardDuty alerts from SecurityHub will have {} format
        print("it is dict")
        event = parse(event, model=GuardDutyAlert)
    else:
        raise TypeError("ERROR: input file is not of type List or Dictionary. Please Check.")

    # only EventBridge Alert has tags .event.detail.findings[]
    if (mode=='n') and (event.detail is not None):
        event = parse(event.detail.findings[0], model=GuardDutyAlert)

    policy_id = event.Types[0] if mode=='n' else event.type
    # if alert is direct we can set the resource since when there is a matching type
    if 'ec2' in policy_id.lower(): # EC2
        rt = 'AwsEc2Instance' if mode=='n' else 'instanceDetails'
        if mode=='d': resource = event.resource.instanceDetails
        service = "ec2"
        resource_type = "instance"
    elif 'iam' in policy_id.lower(): # IAM
        rt = 'AwsIamAccessKey' if mode=='n' else 'accessKeyDetails'
        if mode == 'd': resource = event.resource.accessKeyDetails
        service = "iam"
        resource_type = "user"
    elif 's3' in policy_id.lower(): # S3
        rt = 'AwsS3Bucket' if mode=='n' else 's3BucketDetails'
        if mode == 'd': resource = event.resource.s3BucketDetails
        service = "s3"
        resource_type = "user"
    else:
        rt = None

    # throws an exception if there were no matching resource with event type
    if rt is None:
        raise ValueError("ERROR: There were no matching resource with type " + rt)

    if mode=='n': # resources is List in case of Security Hub,
        for resource in event.Resources:
            if resource.Type == rt:
                break

    # only Alerts from SecurityHub and s3BucketDetails from GuardDuty direct have arn tags
    resource_arn = resource.Id if (mode=='n') else event.resource.s3BucketDetails['arn'] if rt=='s3BucketDetails' else ''

    arn_obj = parse_arn(resource_arn) if not resource_arn=='' else None
    if (arn_obj is not None and arn_obj.resource_type):
        resource_id = arn_obj.resource
    else:
        if mode=='n':
            resource_info = resource_arn.split(":")[5]
            resource_id = (resource_info.split("/")[2]) if ('/' in resource_info) else resource_info
        else: # direct alert has unique tags for each resource type
            if rt=='instanceDetails': resource_id = event.resource.instanceDetails['instanceId']
            elif rt=='accessKeyDetails': resource_id = event.resource.accessKeyDetails['accessKeyId']
            else: resource_id = event.resource.s3BucketDetails['name']

    # many times ARNs do haven't have region e.g. s3 bucket. so we rely on what the finding value is
    if (mode=='n'):
        region = arn_obj.region if arn_obj.region else (resource.Region if resource.Region else None)
    else:
        region = event.region

    alert_id = event.Id if mode=='n' else event.arn
    resource_container = event.AwsAccountId if mode=='n' else event.accountId
    policy_id = policy_id
    csp = "aws"
    vendor_id = "guardduty"
    resource_region = region

    # print('+++csp: ' + csp)
    # print("+++resource_container: " + resource_container)
    # print("+++region: " + resource_region)
    # print("+++resourceId: " + resource_id)
    # print("+++alertId: " + alert_id)
    # if resource_arn is not None: print("+++arn: " + resource_arn)
    # print("+++vendorPolicy: " + policy_id)
    # print("+++vendorId: " + vendor_id)
    # print("+++service: " + service)
    # print("+++resourceType: " + resource_type)
    # print("+++rt: " + rt)

    output = NormalizedOutput(
        csp='aws',
        resourceContainer=resource_container,
        region=region,
        resourceId=resource_id,
        alertId=alert_id,
        arn=resource_arn,
        resourceType=resource_type,
        service=service,
        vendorPolicy=policy_id,
        vendorId='aws-config'
    )

    # print('+++csp: ' + csp)
    # print("+++resource_container: " + resource_container)
    # print("+++region: " + resource_region)
    # print("+++resourceId: " + resource_id)
    # print("+++alertId: " + alert_id)
    # print("+++arn: " + resource_arn)
    # print("+++vendorPolicy: " + policy_id)
    # print("+++vendorId: " + vendor_id)

    return loads(output.json())