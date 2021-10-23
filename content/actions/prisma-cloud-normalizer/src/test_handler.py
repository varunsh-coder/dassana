from json import load
from unittest import TestCase

from dassana.common.aws_client import LambdaTestContext
from dassana.common.models import NormalizedOutput
from handler import handle


class PrismaCloudNormalizerTest(TestCase):
    @staticmethod
    def validate_alert_norm(alert):
        resp = handle(alert, LambdaTestContext('test-prisma-normalizer'))
        norm = NormalizedOutput(**resp)
        assert norm.vendorId == 'prisma-cloud'
        assert norm.alertId == 'P-2388888'
        assert norm.vendorPolicy == '34064d53-1fd1-42e6-b075-45dce495caca'
        assert norm.csp == 'aws'
        assert norm.resourceId == 'foobar'
        assert norm.region == 'ap-northeast-1'
        assert norm.resourceContainer == '123456789123'
        assert norm.severity == 'high'

    def test_sqs(self):
        with open('prisma-cloud-normalizer/tests/examples/prisma_sqs.json') as f:
            alert = load(f)
            self.validate_alert_norm(alert)

    def test_prisma_api(self):
        with open('prisma-cloud-normalizer/tests/examples/prisma_api.json') as f:
            alert = load(f)
            self.validate_alert_norm(alert)

    def test_splunk(self):
        with open('prisma-cloud-normalizer/tests/examples/prisma_splunk.json') as f:
            alert = load(f)
            self.validate_alert_norm(alert)
