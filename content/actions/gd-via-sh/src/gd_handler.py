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
    Severity: Dict[str, Any] = None
    ProductFields: Optional[Dict[str, str]] = None
    Resources: Optional[List[Resources]]
    detail: Detail = None


## changes the formatting of the policyId to be same across guard duty
## ex -> 'TTPs/Initial Access/UnauthorizedAccess:EC2-SSHBruteForce' to 'UnauthorizedAccess:EC2/SSHBruteForce'
def normalize_policyId(policyId, resource):
    # we want to only extract the last part if there are multiple forward slashes
    if not policyId.rfind('/') == policyId.find('/'):
        policyId_extracted = policyId.split('/')[-1]
    else:
        policyId_extracted = policyId
    resource_index = policyId_extracted.lower().rfind(resource) # starting index of the resource
    hyphen_index = resource_index + len(resource) # index of the hyphen after resource
    dash_index = policyId_extracted.rfind('-') # index of the last hyphen
    if (dash_index == hyphen_index): # we want to only change the hyphen after resource
        s = list(policyId_extracted)
        s[dash_index] = '/'
        policy_Id = "".join(s)
    else:
        policy_Id = policyId

    return policy_Id


@event_parser(model=GuardDutyAlert)
def handle(event: GuardDutyAlert, context: LambdaContext):
    # only EventBridge Alerts through SecurityHub have the tags .event.detail.findings[]
    if event.detail is not None:
        event = parse(event.detail.findings[0], model=GuardDutyAlert)

    if event.Types:
        policy_id = event.Types[0]

    # Utilizing PolicyId to find the resource connected to this Alert
    if 'ec2' in policy_id.lower(): # EC2
        rt = 'AwsEc2Instance' # resource names are static and will always be the same based on policyId
        service = "ec2"  # we don't need to wait for policy context step to set this value since we have the policyId
        resource_type = "instance"
        policy_id = normalize_policyId(policy_id, 'ec2')
    elif 'iam' in policy_id.lower(): # IAM
        rt = 'AwsIamAccessKey'
        service = "iam"
        resource_type = "user"
        policy_id = normalize_policyId(policy_id, 'iamuser')
    elif 's3' in policy_id.lower(): # S3
        rt = 'AwsS3Bucket'
        service = "s3"
        resource_type = "bucket"
        policy_id = normalize_policyId(policy_id, 's3')
    else:
        rt = None

    # throws an exception if there were no resources matching with policyId
    if rt is None:
        raise ValueError("ERROR: There were no matching resource with type ")

    for resource in event.Resources:  # traverse the resources list and match with type from policyId
        if resource.Type == rt:
            break

    resource_arn = resource.Id if resource.Id else ""
    vendor_severity = event.Severity["Label"].lower()
    arn_obj = parse_arn(resource_arn) if not resource_arn=='' else None
    if (arn_obj is not None and arn_obj.resource_type):
        resource_id = arn_obj.resource
    else:
        resource_info = resource_arn.split(":")[5]
        resource_id = (resource_info.split("/")[2]) if ('/' in resource_info) else resource_info

    # many times ARNs do haven't have region e.g. s3 bucket. so we rely on what the finding value is
    region = arn_obj.region if arn_obj.region else (resource.Region if resource.Region else None)

    # The actual alert ID is just the last part of event.Id
    alertId = event.Id.split('/')[-1]

    output = NormalizedOutput(
        csp='aws',
        resourceContainer=event.AwsAccountId,
        region=region,
        resourceId=resource_id,
        alertId=alertId,
        canonicalId=resource_arn,
        resourceType=resource_type,
        service=service,
        vendorPolicy=policy_id,
        vendorSeverity=vendor_severity,
        vendorId='aws-guardduty'
    )

    return loads(output.json())
