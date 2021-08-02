from json import loads

from aws_lambda_powertools.utilities.parser import event_parser
from aws_lambda_powertools.utilities.typing import LambdaContext
from pydantic.main import BaseModel

from dassana.common.models import NormalizedOutput


class PrismaPolicy(BaseModel):
    policyId: str
    policyType: str
    systemDefault: bool
    remediable: bool


class PrismaResourceData(BaseModel):
    arn: str


class PrismaResource(BaseModel):
    rrn: str
    cloudType: str
    accountId: str
    region: str
    regionId: str
    resourceType: str
    data: PrismaResourceData


class PrismaAlert(BaseModel):
    id: str
    status: str
    policy: PrismaPolicy
    resource: PrismaResource


class Arn(BaseModel):
    arn: str
    partition: str
    service: str
    region: str
    account: str
    resource: str
    resource_type: str


def parse_arn(arn) -> Arn:
    # https://gist.github.com/gene1wood/5299969edc4ef21d8efcfea52158dd40
    # http://docs.aws.amazon.com/general/latest/gr/aws-arns-and-namespaces.html
    elements = arn.split(':', 5)
    arn = elements[0],
    partition = elements[1],
    service = elements[2],
    region = elements[3],
    account = elements[4],
    resource = elements[5],
    resource_type = None

    if '/' in resource[0]:
        resource_type, resource = resource[0].split('/', 1)
    elif ':' in resource[0]:
        resource_type, resource = resource[0].split(':', 1)
    result = Arn(
        arn=arn[0],
        partition=partition[0],
        service=service[0],
        region=region[0],
        account=account[0],
        resource=resource,
        resource_type=resource_type
    )
    return result


@event_parser(model=PrismaAlert)
def handle(event: PrismaAlert, context: LambdaContext):
    arn_obj = parse_arn(event.resource.data.arn)
    return loads(NormalizedOutput(
        alertId=event.id,
        canonicalId=event.resource.data.arn,
        vendorPolicy=event.policy.policyId,
        csp=event.resource.cloudType,
        resourceContainer=event.resource.accountId,
        region=event.resource.region,
        service=arn_obj.service,
        resourceType=arn_obj.resource_type,
        resourceId=arn_obj.resource
    ).toJSON())
