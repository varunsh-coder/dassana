from json import load
from unittest import TestCase

from dassana.common.aws_client import LambdaTestContext
from dassana.common.models import NormalizedOutput
from handler import handle


class SecurityHubNormalizerTest(TestCase):
    @staticmethod
    def validate_alert_norm(alert):
        print("just an empty shell")

    def print_pretty(self, norm, input):
        print('')
        print('++++++++++++++++++++++++++++++++++++++++++++++++++++')
        print('+++testing input: ' + input)
        print('+++csp: ' + norm.csp)
        print("+++resource_container: " + norm.resourceContainer)
        print("+++region: " + norm.region)
        print("+++resourceId: " + norm.resourceId)
        print("+++alertId: " + norm.alertId)
        print("+++arn: " + norm.arn)
        print("+++vendorPolicy: " + norm.vendorPolicy)
        print("+++vendorId: " + norm.vendorId)

    def test_eb_api_gateway(self):
        with open('security-hub-normalizer/tests/examples/example1.json') as f:
            alert = load(f)
            resp = handle(alert, LambdaTestContext('security-hub-normalizer'))
            norm = NormalizedOutput(**resp)
            self.print_pretty(norm, 'example1.json')
            # assert norm.vendorId == 'prisma-cloud'
            # assert norm.alertId == 'P-2388888'
            # assert norm.vendorPolicy == '34064d53-1fd1-42e6-b075-45dce495caca'
            # assert norm.csp == 'aws'
            # assert norm.resourceId == 'foobar'
            # assert norm.region == 'ap-northeast-1'
            # assert norm.resourceContainer == '123456789123'

    # def test_eb_ec2(self):
    #     with open('../tests/examples/example2.json') as f:
    #         alert = load(f)
    #         resp = handle(alert, LambdaTestContext('security-hub-normalizer'))
    #         norm = NormalizedOutput(**resp)
    #         self.print_pretty(norm, 'example2.json')
            # assert norm.vendorId == 'prisma-cloud'
            # assert norm.alertId == 'P-2388888'
            # assert norm.vendorPolicy == '34064d53-1fd1-42e6-b075-45dce495caca'
            # assert norm.csp == 'aws'
            # assert norm.resourceId == 'foobar'
            # assert norm.region == 'ap-northeast-1'
            # assert norm.resourceContainer == '123456789123'

    # def test_eb_s3(self):
    #     with open('../tests/examples/example3.json') as f:
    #         alert = load(f)
    #         resp = handle(alert, LambdaTestContext('security-hub-normalizer'))
    #         norm = NormalizedOutput(**resp)
    #         self.print_pretty(norm, 'example3.json')
            # assert norm.vendorId == 'prisma-cloud'
            # assert norm.alertId == 'P-2388888'
            # assert norm.vendorPolicy == '34064d53-1fd1-42e6-b075-45dce495caca'
            # assert norm.csp == 'aws'
            # assert norm.resourceId == 'foobar'
            # assert norm.region == 'ap-northeast-1'
            # assert norm.resourceContainer == '123456789123'

    # def test_raw_s3(self):
    #     with open('../tests/examples/example3.json') as f:
    #         alert = load(f)
    #         resp = handle(alert, LambdaTestContext('security-hub-normalizer'))
    #         norm = NormalizedOutput(**resp)
    #         self.print_pretty(norm, 'example4.json')
            # assert norm.vendorId == 'prisma-cloud'
            # assert norm.alertId == 'P-2388888'
            # assert norm.vendorPolicy == '34064d53-1fd1-42e6-b075-45dce495caca'
            # assert norm.csp == 'aws'
            # assert norm.resourceId == 'foobar'
            # assert norm.region == 'ap-northeast-1'
            # assert norm.resourceContainer == '123456789123'

