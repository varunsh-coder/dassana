from json import load
from unittest import TestCase

from dassana.common.aws_client import LambdaTestContext
from dassana.common.models import NormalizedOutput
from gd_handler import handle


class GuardDutyNormalizerTest(TestCase):

    def test_eb_ec2(self):
        with open('gd-via-sh/tests/examples/eb-ec2.json') as f:
            alert = load(f)
            resp = handle(alert, LambdaTestContext('test-guardduty-normalizer'))
            norm = NormalizedOutput(**resp)
            assert norm.vendorId == 'aws-guardduty'
            assert norm.alertId == '36bc3df32928fe1c98ac2a6d9a48fb56'
            assert norm.vendorPolicy == 'UnauthorizedAccess:EC2/SSHBruteForce'
            assert norm.vendorSeverity == 'low'
            assert norm.csp == 'aws'
            assert norm.resourceId == 'i-12345678909876543'
            assert norm.region == 'us-east-1'
            assert norm.resourceContainer == '363265257036'

    def test_raw_s3_multiple_resource(self):
        with open('gd-via-sh/tests/examples/raw-s3-multiple-resource.json') as f:
            alert = load(f)
            resp = handle(alert, LambdaTestContext('test-guardduty-normalizer'))
            norm = NormalizedOutput(**resp)
            assert norm.vendorId == 'aws-guardduty'
            assert norm.alertId == 'd4bdf5da6f4790309f903c8c303f82af'
            assert norm.vendorPolicy == 'Policy:S3/BucketBlockPublicAccessDisabled'
            assert norm.vendorSeverity == 'low'
            assert norm.csp == 'aws'
            assert norm.resourceId == 'new-fixed-bucket-dassana'
            assert norm.region == 'us-east-1'
            assert norm.resourceContainer == '020747060415'


    def test_raw_ec2_alpha(self):
        with open('gd-via-sh/tests/examples/raw-ec2-alpha.json') as f:
            alert = load(f)
            resp = handle(alert, LambdaTestContext('test-guardduty-normalizer'))
            norm = NormalizedOutput(**resp)
            assert norm.vendorId == 'aws-guardduty'
            assert norm.alertId == 'a0bdfff401df680ce958b027abe1c311'
            assert norm.vendorPolicy == 'UnauthorizedAccess:EC2/SSHBruteForce'
            assert norm.vendorSeverity == 'low'
            assert norm.csp == 'aws'
            assert norm.resourceId == 'i-12345678909876543'
            assert norm.region == 'us-east-1'
            assert norm.resourceContainer == '020747060415'