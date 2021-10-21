from typing import Dict
from dassana.common.aws_client import LambdaTestContext
from models import BUCKET_WEBSITE_URL
import pytest


@pytest.fixture()
def input_s3_with_website(s3_public_bucket_with_website, region):
    return {
        'bucketName': s3_public_bucket_with_website,
        'region': region
    }


@pytest.fixture()
def input_s3_without_website(s3_private_bucket, region):
    return {
        'bucketName': s3_private_bucket,
        'region': region
    }


@pytest.fixture()
def input_s3_bucket_does_not_exist(s3_private_bucket, region):
    return {
        'bucketName': 'foobar',
        'region': region
    }


def test_handle_website_context_exists(input_s3_with_website):
    from handler import handle
    result: Dict = handle(input_s3_with_website, LambdaTestContext('tl', env={}, custom={}))

    assert result.get(BUCKET_WEBSITE_URL) == str.format('{bucketName}.s3-website-{region}.amazonaws.com',
                                                        **input_s3_with_website)


def test_handle_website_context_does_not_exist(input_s3_without_website):
    from handler import handle
    result: Dict = handle(input_s3_without_website, LambdaTestContext('c9', env={}, custom={}))
    assert len(result.get(BUCKET_WEBSITE_URL)) == 0


def test_handle_bucket_does_not_exist(input_s3_bucket_does_not_exist):
    from handler import handle
    result: Dict = handle(input_s3_bucket_does_not_exist, LambdaTestContext('100t', env={}, custom={}))
    assert len(result.get(BUCKET_WEBSITE_URL)) == 0
