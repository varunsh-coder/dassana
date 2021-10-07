from json import load
from unittest import TestCase

from dassana.common.aws_client import LambdaTestContext
from dassana.common.models import NormalizedOutput
from handler import handle


class GuardDutyNormalizerTest(TestCase):
    @staticmethod
    def validate_alert_norm(alert):
        print("just an empty shell")

    def print_pretty(self, norm, i):
        print('')
        print('++++++++++++++++++++++++++')
        print('+++testing input: ' + i)
        print('+++csp: ' + norm.csp)
        print("+++resource_container: " + norm.resourceContainer)
        print("+++region: " + norm.region)
        print("+++resourceId: " + norm.resourceId)
        print("+++alertId: " + norm.alertId)
        print("+++arn: " + norm.arn)
        print("+++vendorPolicy: " + norm.vendorPolicy)
        print("+++vendorId: " + norm.vendorId)
        print("+++service: " + norm.service)
        print("+++resourceType: " + norm.resourceType)

    def test_eb_ec2(self):
        with open('gd-via-sh/tests/examples/eb-ec2.json') as f:
            alert = load(f)
            resp = handle(alert, LambdaTestContext('test-guardduty-normalizer'))
            norm = NormalizedOutput(**resp)
            self.print_pretty(norm, 'eb-ec2.json')
            assert norm.vendorId == 'guardduty'
            assert norm.alertId == 'arn:aws:guardduty:us-east-1:363265257036:detector/96bad234c3d0033d695e70c93b8741fb/finding/36bc3df32928fe1c98ac2a6d9a48fb56'
            assert norm.vendorPolicy == 'TTPs/Initial Access/UnauthorizedAccess:EC2-SSHBruteForce'
            assert norm.csp == 'aws'
            assert norm.resourceId == 'i-054474987a390f341'
            assert norm.region == 'us-east-1'
            assert norm.resourceContainer == '363265257036'

    def test_raw_s3_multiple_resource(self):
        with open('gd-via-sh/tests/examples/raw-s3-multiple-resource.json') as f:
            alert = load(f)
            resp = handle(alert, LambdaTestContext('test-guardduty-normalizer'))
            norm = NormalizedOutput(**resp)
            self.print_pretty(norm, 'raw-s3-multiple-resource.json')
            assert norm.vendorId == 'guardduty'
            assert norm.alertId == 'arn:aws:guardduty:us-east-1:020747060415:detector/a2bdf2d15d3f3187077af621af3e234d/finding/d4bdf5da6f4790309f903c8c303f82af'
            assert norm.vendorPolicy == 'Effects/Data Exposure/Policy:S3-BucketBlockPublicAccessDisabled'
            assert norm.csp == 'aws'
            assert norm.resourceId == 'new-fixed-bucket-dassana'
            assert norm.region == 'us-east-1'
            assert norm.resourceContainer == '020747060415'


    # def test_direct_ec2_alpha(self):
    #     with open('gd-via-sh/tests/examples/direct-ec2-alpha.json') as f:
    #         alert = load(f)
    #         resp = handle(alert, LambdaTestContext('test-guardduty-normalizer'))
    #         norm = NormalizedOutput(**resp)
    #         self.print_pretty(norm, 'direct-ec2-alpha.json')
    #         # assert norm.vendorId == 'guardduty'
    #         # assert norm.alertId == 'P-2388888'
    #         # assert norm.vendorPolicy == '34064d53-1fd1-42e6-b075-45dce495caca'
    #         # assert norm.csp == 'aws'
    #         # assert norm.resourceId == 'foobar'
    #         # assert norm.region == 'ap-northeast-1'
    #         # assert norm.resourceContainer == '123456789123'

    def test_raw_ec2_alpha(self):
        with open('gd-via-sh/tests/examples/raw-ec2-alpha.json') as f:
            alert = load(f)
            resp = handle(alert, LambdaTestContext('test-guardduty-normalizer'))
            norm = NormalizedOutput(**resp)
            self.print_pretty(norm, 'raw-ec2-alpha.json')
            assert norm.vendorId == 'guardduty'
            assert norm.alertId == 'arn:aws:guardduty:us-east-1:020747060415:detector/a2bdf2d15d3f3187077af621af3e234d/finding/a0bdfff401df680ce958b027abe1c311'
            assert norm.vendorPolicy == 'TTPs/Initial Access/UnauthorizedAccess:EC2-SSHBruteForce'
            assert norm.csp == 'aws'
            assert norm.resourceId == 'i-0bc95ef8e97a7d331'
            assert norm.region == 'us-east-1'
            assert norm.resourceContainer == '020747060415'