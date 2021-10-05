from json import loads, load
from typing import Dict, List, Any, Optional

from aws_lambda_powertools.utilities.parser import event_parser, parse
from aws_lambda_powertools.utilities.typing import LambdaContext

from dassana.common.aws_client import parse_arn
from dassana.common.models import NormalizedOutput
from dassana.common.aws_client import LambdaTestContext

from normalize.models import GuardDutyAlert

# first distinguish alerts [ Direct, EvenBridge, Raw ] -> this can be done via helper
# # EvenBridge and Raw can use the same class, Direct needs its own
# need to set resource type based on policyId(extracted from the type[s])
# set alertId, resourceContainer, VendorPolicy, Csp, VendorId accordingly
# get resource type based on policyId ( this is to be able to get the right resource object)
# set region --> from region tag or resource details (raw doesnt seem to have the resource details)
# set resourceId accordingly --> this is based on the arn if not direct. if Direct need to figure out something else

# if the below way works i can check if the input is dict or list (raw and eventbridge are dict, direct would be list)
# test = GuardDutyAlert


@event_parser(model=SecurityHubAlert)
def handle(event: SecurityHubAlert, context: LambdaContext):
    # only EventBridge Alert has tags .event.detail.findings[]
    if event.detail is not None:
        event = parse(event.detail.findings[0], model=SecurityHubAlert)

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

    output = NormalizedOutput(
        csp='aws',
        resourceContainer=resource_container,
        region=region,
        resourceId=resource_id,
        alertId=event.Id,
        arn=resource_arn,
        vendorPolicy=policy_id,
        vendorId='aws-config'
    )
    #
    # print('+++csp: '+'aws')
    # print("+++resource_container: " + resource_container)
    # print("+++region: " + region)
    # print("+++resourceId: " + resource_id)
    # print("+++alertId: " + event.Id)
    # print("+++arn: " + resource_arn)
    # print("+++vendorPolicy: " + policy_id)
    # print("+++vendorId: " + 'aws-config')

    return loads(output.json())