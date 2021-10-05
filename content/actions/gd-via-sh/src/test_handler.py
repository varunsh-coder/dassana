from json import load
from unittest import TestCase

from dassana.common.aws_client import LambdaTestContext
from dassana.common.models import NormalizedOutput
from handler import handle


class GuardDutyNormalizerTest(TestCase):
    @staticmethod
    def validate_alert_norm(alert):
        print("just an empty shell")
        # resp = handle(alert, LambdaTestContext('test-guardduty-normalizer'))
        # norm = NormalizedOutput(**resp)
        # assert norm.vendorId == 'prisma-cloud'
        # assert norm.alertId == 'P-2388888'
        # assert norm.vendorPolicy == '34064d53-1fd1-42e6-b075-45dce495caca'
        # assert norm.csp == 'aws'
        # assert norm.resourceId == 'foobar'
        # assert norm.region == 'ap-northeast-1'
        # assert norm.resourceContainer == '123456789123'

    def print_pretty(norm, input):
        print('')
        print('++++++++++++++++++++++++++')
        print('+++testing input: ' + input)
        print('+++csp: ' + norm.csp)
        print("+++resource_container: " + norm.resourceContainer)
        print("+++region: " + norm.region)
        print("+++resourceId: " + norm.resourceId)
        print("+++alertId: " + norm.alertId)
        print("+++arn: " + norm.arn)
        print("+++vendorPolicy: " + norm.vendorPolicy)
        print("+++vendorId: " + norm.vendorId)

    def test_eventbridge(self):
        with open('tests/examples/sample-gd.json') as f:
            alert = load(f)
            resp = handle(alert, LambdaTestContext('test-guardduty-normalizer'))
            norm = NormalizedOutput(**resp)
            self.print_pretty(norm, 'sample-gd.json')
            # assert norm.vendorId == 'prisma-cloud'
            # assert norm.alertId == 'P-2388888'
            # assert norm.vendorPolicy == '34064d53-1fd1-42e6-b075-45dce495caca'
            # assert norm.csp == 'aws'
            # assert norm.resourceId == 'foobar'
            # assert norm.region == 'ap-northeast-1'
            # assert norm.resourceContainer == '123456789123'

    def test_raw(self):
        with open('tests/examples/gd-raw.json') as f:
            alert = load(f)
            resp = handle(alert, LambdaTestContext('test-guardduty-normalizer'))
            norm = NormalizedOutput(**resp)
            self.print_pretty(norm, 'gd-raw.json')
            # assert norm.vendorId == 'prisma-cloud'
            # assert norm.alertId == 'P-2388888'
            # assert norm.vendorPolicy == '34064d53-1fd1-42e6-b075-45dce495caca'
            # assert norm.csp == 'aws'
            # assert norm.resourceId == 'foobar'
            # assert norm.region == 'ap-northeast-1'
            # assert norm.resourceContainer == '123456789123'

    def test_direct(self):
        with open('tests/examples/gd-direct.json') as f:
            alert = load(f)
            print(alert)
            resp = handle(alert, LambdaTestContext('test-guardduty-normalizer'))
            norm = NormalizedOutput(**resp)
            self.print_pretty(norm, 'gd-direct.json')
            # assert norm.vendorId == 'prisma-cloud'
            # assert norm.alertId == 'P-2388888'
            # assert norm.vendorPolicy == '34064d53-1fd1-42e6-b075-45dce495caca'
            # assert norm.csp == 'aws'
            # assert norm.resourceId == 'foobar'
            # assert norm.region == 'ap-northeast-1'
            # assert norm.resourceContainer == '123456789123'

