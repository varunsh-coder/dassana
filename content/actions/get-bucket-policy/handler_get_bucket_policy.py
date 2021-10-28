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
    client = dassana_aws.create_aws_client(context=context, service='s3',
                                           region=event.get('region'))

    bucket_name = event.get('bucketName')

    try:
        result = client.get_bucket_policy(Bucket=bucket_name)
        bucket_policy = result['Policy']
    except ClientError as e:
        if e.response.get('Error').get('Code') in ['NoSuchBucket', 'NoSuchBucketPolicy']:
            # The bucket doesn't exist or doesn't have an associated bucket policy
            bucket_policy = dumps({})
        else:
            raise ClientError

    return {"result": loads(bucket_policy)}
