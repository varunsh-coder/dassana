from enum import Enum
from json import loads
from typing import Dict, List, Any, Optional

from pydantic import BaseModel
from aws_lambda_powertools.utilities.parser import event_parser, parse
from aws_lambda_powertools.utilities.typing import LambdaContext

from dassana.common.aws_client import parse_arn
from dassana.common.models import NormalizedOutput
from dassana.common.aws_client import LambdaTestContext
from dassana.common.timing import get_unix_time


class Resources(BaseModel):
    Partition: str = None
    Type: str = None
    Region: str = None
    Id: str = None


class Detail(BaseModel):
    findings: List[Dict[Any, Any]] = None

class AWSConfigAlert(BaseModel):
    Id: str = None
    ProductArn: str = None
    Region: str = None
    AwsAccountId: str = None
    Types: List[str] = []
    FirstObservedAt: str = None
    ProductFields: Optional[Dict[str, str]] = None
    FindingProviderFields: Dict[str, Any] = None
    Severity: Dict[str, Any] = None
    RecordState: str = None
    Resources: Optional[List[Resources]]
    detail: Detail = None


@event_parser(model=AWSConfigAlert)
def handle(event: AWSConfigAlert, context: LambdaContext):
    # only EventBridge Alert has tags .event.detail.findings[]
    if event.detail is not None:
        event = parse(event.detail.findings[0], model=AWSConfigAlert)

    # not sure if i need this if statement --> java code does not check if it exist. doesnt even use opt...
    if event.ProductFields:
        # defaults policy id to StandardsControlArn
        policy_id = ((event.ProductFields['StandardsControlArn'].split(":"))[5])

        # if event.ProductFields.RelatedAWSResources:0/name exist update the policyId
        if 'RelatedAWSResources:0/name' in event.ProductFields:
            possible_policy_id = event.ProductFields['RelatedAWSResources:0/name']
            first_index_of = possible_policy_id.find('-')
            last_index_of = possible_policy_id.rfind('-')
            policy_id = possible_policy_id[first_index_of+1:last_index_of]

        resource_arn = event.ProductFields['Resources:0/Id']

    # Changing alert time to Unix
    alert_time = get_unix_time(event.FirstObservedAt)

    # Severity is held inside this field in some alerts
    if event.FindingProviderFields:
        vendor_severity = event.FindingProviderFields["Severity"]["Label"].lower()
    else:
        vendor_severity = event.Severity["Label"].lower()
    
    record_state = event.RecordState

    arn_obj = parse_arn(resource_arn) if resource_arn else None
    if arn_obj.resource_type:
        resource_id = arn_obj.resource
    else:
        resource_info = resource_arn.split(":")[5]
        resource_id = (resource_info.split("/")[2]) if ('/' in resource_info) else resource_info

    # many a times ARNs do haven't have region e.g. s3 bucket resources etc so we rely on what the finding value is
    region = arn_obj.region if arn_obj.region else (event.Resources[0].Region if event.Resources else None)

    arn_account_id = arn_obj.account if arn_obj.account else None
    aws_account_id = event.AwsAccountId if event.AwsAccountId else None
    resource_container = arn_account_id if arn_account_id else aws_account_id

    # The actual alert ID is just the last part of event.Id
    alertId = event.Id.split('/')[-1]

    output = NormalizedOutput(
        csp='aws',
        resourceContainer=resource_container,
        region=region,
        resourceId=resource_id,
        alertId=alertId,
        alertTime=alert_time,
        canonicalId=resource_arn,
        vendorPolicy=policy_id,
        vendorSeverity=vendor_severity,
        alertState=record_state,
        vendorId='aws-config'
    )

    return loads(output.json())
