from json import load
from unittest import TestCase

from dassana.common.aws_client import LambdaTestContext
from dassana.common.models import NormalizedOutput
from handler_prisma_cloud import handle


class PrismaCloudNormalizerTest(TestCase):

    @staticmethod
    # Primsa Splunk Alert does not contain alert time
    def validate_alert_prisma_splunk(alert):
        resp = handle(alert, LambdaTestContext('test-prisma-normalizer'))
        norm = NormalizedOutput(**resp)
        assert norm.vendorId == 'prisma-cloud'
        assert norm.alertId == 'P-2388888'
        assert norm.vendorPolicy == '34064d53-1fd1-42e6-b075-45dce495caca'
        assert norm.csp == 'aws'
        assert norm.resourceId == 'foobar'
        assert norm.region == 'ap-northeast-1'
        assert norm.resourceContainer == '123456789123'
        assert norm.vendorSeverity == 'high'
        assert norm.alertTime == None

    # Prisma API alert does not contain vendor severity
    @staticmethod
    def validate_alert_prisma_api(alert):  
        resp = handle(alert, LambdaTestContext('test-prisma-normalizer'))
        norm = NormalizedOutput(**resp)
        assert norm.vendorId == 'prisma-cloud'
        assert norm.alertId == 'P-2388888'
        assert norm.vendorPolicy == '34064d53-1fd1-42e6-b075-45dce495caca'
        assert norm.csp == 'aws'
        assert norm.resourceId == 'foobar'
        assert norm.region == 'ap-northeast-1'
        assert norm.resourceContainer == '123456789123'
        assert norm.vendorSeverity == None
        assert norm.alertTime == '1615010072772'

    @staticmethod
    def validate_alert_prisma_sqs(alert):  
        resp = handle(alert, LambdaTestContext('test-prisma-normalizer'))
        norm = NormalizedOutput(**resp)
        assert norm.vendorId == 'prisma-cloud'
        assert norm.alertId == 'P-2388888'
        assert norm.vendorPolicy == '34064d53-1fd1-42e6-b075-45dce495caca'
        assert norm.csp == 'aws'
        assert norm.resourceId == 'foobar'
        assert norm.region == 'ap-northeast-1'
        assert norm.resourceContainer == '123456789123'
        assert norm.vendorSeverity == 'high'
        assert norm.alertTime == '1629947769576'

    def test_sqs(self):
        with open('prisma-cloud-normalizer/tests/examples/prisma_sqs.json') as f:
            alert = load(f)
            self.validate_alert_prisma_sqs(alert)

    def test_prisma_api(self):
        with open('prisma-cloud-normalizer/tests/examples/prisma_api.json') as f:
            alert = load(f)
            self.validate_alert_prisma_api(alert)

    def test_splunk(self):
        with open('prisma-cloud-normalizer/tests/examples/prisma_splunk.json') as f:
            alert = load(f)
            self.validate_alert_prisma_splunk(alert)
