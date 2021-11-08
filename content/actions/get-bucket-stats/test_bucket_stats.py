from unittest.mock import patch

from dassana.common.aws_client import LambdaTestContext

SMALL_BYTES = 2 ** 10  # 1024 BYTES
BIG_BYTES = 11883.0 * (2 ** 30)


@patch('handler_bucket_stats.get_storage_metric', lambda a, b, c, d: BIG_BYTES)
def test_cloudwatch_conversion_large(s3_client, region):
    from handler_bucket_stats import handle
    result = handle({'bucketName': 'foo', 'region': region}, LambdaTestContext('abc', env={}, custom={}))
    assert result.get('result').get('bucketSizeInGB') * (2 ** 10) ** 3 == BIG_BYTES
    assert result.get('result').get('numberOfObjects') == BIG_BYTES


@patch('handler_bucket_stats.get_storage_metric', lambda a, b, c, d: SMALL_BYTES)
def test_cloudwatch_conversion_small(s3_client, region):
    from handler_bucket_stats import handle
    result = handle({'bucketName': 'foo', 'region': region}, LambdaTestContext('abc', env={}, custom={}))
    # Account for loss of precision in small bucket size values (rounded to 6 decimals)
    assert SMALL_BYTES*0.95 <= result.get('result').get('bucketSizeInGB') * (2 ** 10) ** 3 <= SMALL_BYTES*1.05
    assert result.get('result').get('numberOfObjects') == SMALL_BYTES
