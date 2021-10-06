from json import loads, load
from typing import Dict, List, Any, Optional
from pydantic import BaseModel

from aws_lambda_powertools.utilities.parser import event_parser, parse
from aws_lambda_powertools.utilities.typing import LambdaContext

from dassana.common.aws_client import parse_arn
from dassana.common.models import NormalizedOutput
from dassana.common.aws_client import LambdaTestContext


class Resources(BaseModel):
    Partition: str = None
    Type: str = None
    Region: str = None
    Id: str = None


class Detail(BaseModel):
    findings: List[Dict[Any, Any]] = None


class GuardDutyAlert(BaseModel):
    Id: str = None
    ProductArn: str = None
    Region: str = None
    AwsAccountId: str = None
    Types: List[str] = []
    ProductFields: Optional[Dict[str, str]] = None
    Resources: Optional[List[Resources]]
    detail: Detail = None


class DirectResource(BaseModel):
    accessKeyDetails: Optional[Dict[str, str]]
    s3BucketDetails: Optional[List[Dict[Any, Any]]]
    instanceDetails: Optional[Dict[Any, Any]]


class GuardDutyDirect(BaseModel):
    accountId: str = None
    region: str = None
    type: str = None
    resource: DirectResource = None
    arn: str = None
    id: str = None


def handle(event, context: LambdaContext):
    mode = 's' # s --> SecurityHub - we default to raw and event bridge alert format
    if isinstance(event, list): # direct GuardDuty alert's has [{}] format
        print("it is list")
        event = parse(event[0], model=GuardDutyDirect)
        mode = 'd' # --> since direct alert have different tag names we need flag to distinguish
    elif isinstance(event, dict): # GuardDuty alerts from SecurityHub will have {} format
        print("it is dict")
        event = parse(event, model=GuardDutyAlert)
    else:
        raise TypeError("ERROR: input file is not of type List or Dictionary. Please Check.")

    # only EventBridge Alert has tags .event.detail.findings[]
    if (mode=='s') and (event.detail is not None):
        event = parse(event.detail.findings[0], model=GuardDutyAlert)

    policy_id = event.Types[0] if mode=='s' else event.type

    if 'ec2' in policy_id.lower(): # EC2
        rt = 'AwsEc2Instance' if mode=='s' else 'instanceDetails'  # direct alerts have different resource name
        if mode=='d': resource = event.resource.instanceDetails  # direct alerts have resource type as a tag
        service = "ec2"  # we don't need to wait for policy context to set this value since we have the policyId
        resource_type = "instance"
    elif 'iam' in policy_id.lower(): # IAM
        rt = 'AwsIamAccessKey' if mode=='s' else 'accessKeyDetails'
        if mode == 'd': resource = event.resource.accessKeyDetails
        service = "iam"
        resource_type = "user"
    elif 's3' in policy_id.lower(): # S3
        rt = 'AwsS3Bucket' if mode=='s' else 's3BucketDetails'
        if mode == 'd': resource = event.resource.s3BucketDetails
        service = "s3"
        resource_type = "bucket"
    else:
        rt = None

    # throws an exception if there were no resource matching with policyId
    if rt is None:
        raise ValueError("ERROR: There were no matching resource with type " + rt)

    if mode=='s': # resources is List in case of Security Hub
        for resource in event.Resources:  # traverse the resources list and match with type from policyId
            if resource.Type == rt:
                break

    # instanceDetails and accessKeyDetails resources from GuardDuty direct do not have arn
    resource_arn = resource.Id if mode=='s' else event.resource.s3BucketDetails['arn'] if rt=='s3BucketDetails' else ''

    arn_obj = parse_arn(resource_arn) if not resource_arn=='' else None
    if (arn_obj is not None and arn_obj.resource_type):
        resource_id = arn_obj.resource
    else:
        if mode=='s':
            resource_info = resource_arn.split(":")[5]
            resource_id = (resource_info.split("/")[2]) if ('/' in resource_info) else resource_info
        else: # direct alert has unique tags for each resource type
            if rt=='instanceDetails': resource_id = event.resource.instanceDetails['instanceId']  # EC2
            elif rt=='accessKeyDetails': resource_id = event.resource.accessKeyDetails['accessKeyId']  # IAM
            else: resource_id = event.resource.s3BucketDetails['name']  # S3

    # many times ARNs do haven't have region e.g. s3 bucket. so we rely on what the finding value is
    if (mode=='s'):  # raw and eventBridge
        region = arn_obj.region if arn_obj.region else (resource.Region if resource.Region else None)
    else:
        region = event.region  # direct

    alert_id = event.Id if mode=='s' else event.arn
    resource_container = event.AwsAccountId if mode=='s' else event.accountId

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
        vendorId='guardduty'
    )

    return loads(output.json())