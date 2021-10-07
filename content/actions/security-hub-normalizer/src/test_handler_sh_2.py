from json import load
from unittest import TestCase

from dassana.common.aws_client import LambdaTestContext
from dassana.common.models import NormalizedOutput
from handler import handle


class SecurityHubNormalizerTest(TestCase):
    @staticmethod
    def validate_alert_norm(alert):
        print("just an empty shell")

    ### for testing only
    # def print_pretty(self, norm, input):
    #     print('')
    #     print('++++++++++++++++++++++++++++++++++++++++++++++++++++')
    #     print('+++testing input: ' + input)
    #     print('+++csp: ' + norm.csp)
    #     print("+++resource_container: " + norm.resourceContainer)
    #     print("+++region: " + norm.region)
    #     print("+++resourceId: " + norm.resourceId)
    #     print("+++alertId: " + norm.alertId)
    #     print("+++arn: " + norm.arn)
    #     print("+++vendorPolicy: " + norm.vendorPolicy)
    #     print("+++vendorId: " + norm.vendorId)

    def test_eb_api_gateway(self):
        with open('security-hub-normalizer/tests/examples/apigateway_eb.json') as f:
            alert = load(f)
            resp = handle(alert, LambdaTestContext('security-hub-normalizer'))
            norm = NormalizedOutput(**resp)
            # self.print_pretty(norm, 'apigateway_eb.json')
            assert norm.vendorId == 'aws-config'
            assert norm.alertId == 'arn:aws:securityhub:us-east-1:363265257036:subscription/aws-foundational-security-best-practices/v/1.0.0/APIGateway.4/finding/227ca420-48fa-4df8-ad47-f747521d169a'
            assert norm.vendorPolicy == 'api-gw-associated-with-waf'
            assert norm.csp == 'aws'
            assert norm.resourceId == '6u20vtvjpk'
            assert norm.region == 'us-east-1'
            assert norm.resourceContainer == '363265257036'

    def test_eb_ec2(self):
        with open('security-hub-normalizer/tests/examples/ec2_eb.json') as f:
            alert = load(f)
            resp = handle(alert, LambdaTestContext('security-hub-normalizer'))
            norm = NormalizedOutput(**resp)
            # self.print_pretty(norm, 'ec2_eb.json')
            assert norm.vendorId == 'aws-config'
            assert norm.alertId == 'arn:aws:securityhub:us-east-1:363265257036:subscription/aws-foundational-security-best-practices/v/1.0.0/EC2.19/finding/a98a1bc3-2bcd-49c0-b40e-edc09c4a059d'
            assert norm.vendorPolicy == 'vpc-sg-restricted-common-ports'
            assert norm.csp == 'aws'
            assert norm.resourceId == 'sg-061d7bbf4c68da2c7'
            assert norm.region == 'us-east-1'
            assert norm.resourceContainer == '363265257036'

    def test_eb_s3(self):
        with open('security-hub-normalizer/tests/examples/s3_eb.json') as f:
            alert = load(f)
            resp = handle(alert, LambdaTestContext('security-hub-normalizer'))
            norm = NormalizedOutput(**resp)
            # self.print_pretty(norm, 's3_eb.json')
            assert norm.vendorId == 'aws-config'
            assert norm.alertId == 'arn:aws:securityhub:us-east-1:364056642809:subscription/aws-foundational-security-best-practices/v/1.0.0/S3.2/finding/18136b0e-07c7-400a-aaf4-be492baa3bf6'
            assert norm.vendorPolicy == 's3-bucket-public-read-prohibited'
            assert norm.csp == 'aws'
            assert norm.resourceId == 'dassana-public-content'
            assert norm.region == 'us-east-1'
            assert norm.resourceContainer == '364056642809'

    def test_raw_s3(self):
        with open('security-hub-normalizer/tests/examples/s3_raw.json') as f:
            alert = load(f)
            resp = handle(alert, LambdaTestContext('security-hub-normalizer'))
            norm = NormalizedOutput(**resp)
            # self.print_pretty(norm, 's3_raw.json')
            assert norm.vendorId == 'aws-config'
            assert norm.alertId == 'arn:aws:securityhub:us-east-1:020747060415:subscription/aws-foundational-security-best-practices/v/1.0.0/S3.2/finding/d0c74471-d0da-40bf-81cd-d871510d49ea'
            assert norm.vendorPolicy == 's3-bucket-public-read-prohibited'
            assert norm.csp == 'aws'
            assert norm.resourceId == 'new-fixed-bucket-dassana'
            assert norm.region == 'us-east-1'
            assert norm.resourceContainer == '020747060415'

