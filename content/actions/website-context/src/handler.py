from json import load
from typing import Dict, Any, Optional

from aws_lambda_powertools.utilities.typing import LambdaContext
from aws_lambda_powertools.utilities.validation import validator
from botocore.exceptions import ClientError

from dassana.common.aws_client import DassanaAwsObject

with open('input.json', 'r') as schema:
    schema = load(schema)
    dassana_aws = DassanaAwsObject(
        schema['properties']['awsRegion']['default'])


@validator(inbound_schema=schema)
def handle(event: Dict[str, Optional[Any]], context: LambdaContext):
    client = dassana_aws.create_aws_client(event, context, 's3')
    try:
        resp = client.get_bucket_website(
            Bucket=event['bucket'],
        )
        return True
    except ClientError as e:
        return False
