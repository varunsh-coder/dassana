from typing import Dict
import json
from dassana.common.aws_client import LambdaTestContext
import pytest


@pytest.fixture()
def s3_bucket_policy():
    def _create_policy(bucket_name):
        policy = {
            "Version": "2012-10-17",
            "Id": "PutObjPolicy",
            "Statement": [
                {
                    "Sid": "DenyUnEncryptedObjectUploads",
                    "Effect": "Deny",
                    "Principal": "*",
                    "Action": "s3:PutObject",
                    "Resource": "arn:aws:s3:::{bucket_name}/*".format(
                        bucket_name=bucket_name
                    ),
                    "Condition": {
                        "StringNotEquals": {
                            "s3:x-amz-server-side-encryption": "aws:kms"
                        }
                    },
                }
            ],
        }
        return json.dumps(policy)

    return _create_policy


@pytest.fixture()
def s3_bucket_with_policy(s3_client, s3_plain_bucket, s3_bucket_policy):
    bucket_name = s3_plain_bucket
    bucket_policy = s3_bucket_policy(bucket_name)
    s3_client.put_bucket_policy(Bucket=bucket_name, Policy=bucket_policy)
    return bucket_name


@pytest.fixture()
def input_s3_with_policy(s3_bucket_with_policy, region):
    return {
        'bucketName': s3_bucket_with_policy,
        'region': region
    }


@pytest.fixture()
def input_s3_without_policy(s3_plain_bucket, region):
    return {
        'bucketName': s3_plain_bucket,
        'region': region
    }


@pytest.fixture()
def input_s3_bucket_does_not_exist(s3_client, region):
    return {
        'bucketName': 'foobar',
        'region': region
    }


def test_handle_bucket_policy_exists(input_s3_with_policy, s3_bucket_policy):
    from handler_get_bucket_policy import handle
    result: Dict = handle(input_s3_with_policy, LambdaTestContext('tl', env={}, custom={}))
    assert json.dumps(result.get('result')) == s3_bucket_policy(input_s3_with_policy['bucketName'])


def test_handle_bucket_policy_does_not_exist(input_s3_without_policy):
    from handler_get_bucket_policy import handle
    result: Dict = handle(input_s3_without_policy, LambdaTestContext('c9', env={}, custom={}))
    assert result.get('result') == {}


def test_handle_bucket_does_not_exist(input_s3_bucket_does_not_exist):
    from handler_get_bucket_policy import handle
    result: Dict = handle(input_s3_bucket_does_not_exist, LambdaTestContext('100t', env={}, custom={}))
    assert result.get('result') == {}
