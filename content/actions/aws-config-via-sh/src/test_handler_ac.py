from json import load
from unittest import TestCase

from dassana.common.aws_client import LambdaTestContext
from dassana.common.models import NormalizedOutput
from ac_handler import handle


class AWSConfigNormalizerTest(TestCase):

    def test_eb_api_gateway(self):
        with open('aws-config-via-sh/tests/examples/apigateway_eb.json') as f:
            alert = load(f)
            resp = handle(alert, LambdaTestContext('aws-config-via-sh'))
            norm = NormalizedOutput(**resp)
            assert norm.vendorId == 'aws-config'
            assert norm.alertId == '227ca420-48fa-4df8-ad47-f747521d169a'
            assert norm.alertTime == '1628295027'
            assert norm.vendorPolicy == 'api-gw-associated-with-waf'
            assert norm.vendorSeverity == 'medium'
            assert norm.csp == 'aws'
            assert norm.resourceId == '6u20vtvjpk'
            assert norm.region == 'us-east-1'
            assert norm.resourceContainer == '363265257036'

    def test_eb_ec2(self):
        with open('aws-config-via-sh/tests/examples/ec2_eb.json') as f:
            alert = load(f)
            resp = handle(alert, LambdaTestContext('aws-config-via-sh'))
            norm = NormalizedOutput(**resp)
            assert norm.vendorId == 'aws-config'
            assert norm.alertId == 'a98a1bc3-2bcd-49c0-b40e-edc09c4a059d'
            assert norm.alertTime == '1628447490'
            assert norm.vendorPolicy == 'vpc-sg-restricted-common-ports'
            assert norm.vendorSeverity == 'medium'
            assert norm.csp == 'aws'
            assert norm.resourceId == 'sg-061d7bbf4c68da2c7'
            assert norm.region == 'us-east-1'
            assert norm.resourceContainer == '363265257036'

    def test_eb_s3(self):
        with open('aws-config-via-sh/tests/examples/s3_eb.json') as f:
            alert = load(f)
            resp = handle(alert, LambdaTestContext('aws-config-via-sh'))
            norm = NormalizedOutput(**resp)
            assert norm.vendorId == 'aws-config'
            assert norm.alertId == '18136b0e-07c7-400a-aaf4-be492baa3bf6'
            assert norm.alertTime == '1623705652'
            assert norm.vendorPolicy == 's3-bucket-public-read-prohibited'
            assert norm.vendorSeverity == 'critical'
            assert norm.csp == 'aws'
            assert norm.resourceId == 'dassana-public-content'
            assert norm.region == 'us-east-1'
            assert norm.resourceContainer == '364056642809'

    def test_raw_s3(self):
        with open('aws-config-via-sh/tests/examples/s3_raw.json') as f:
            alert = load(f)
            resp = handle(alert, LambdaTestContext('aws-config-via-sh'))
            norm = NormalizedOutput(**resp)
            assert norm.vendorId == 'aws-config'
            assert norm.alertId == 'd0c74471-d0da-40bf-81cd-d871510d49ea'
            assert norm.alertTime == '1631765378'
            assert norm.vendorPolicy == 's3-bucket-public-read-prohibited'
            assert norm.vendorSeverity == 'critical'
            assert norm.csp == 'aws'
            assert norm.resourceId == 'new-fixed-bucket-dassana'
            assert norm.region == 'us-east-1'
            assert norm.resourceContainer == '020747060415'