from json import load, loads
from unittest import TestCase

import handler
from dassana.common.aws_client import LambdaTestContext
from dassana.common.models import NormalizedOutput


class PrismaCloudNormalizerTest(TestCase):
    @staticmethod
    def validate_alert_norm(alert):
        resp = handler.handle(alert, LambdaTestContext('prisma-normalizer'))
        norm = NormalizedOutput(**resp)
        assert norm.vendorId == 'prisma-cloud'
        assert norm.alertId == 'P-2388888'
        assert norm.vendorPolicy == '34064d53-1fd1-42e6-b075-45dce495caca'
        assert norm.csp == 'aws'
        assert norm.resourceId == 'foobar'
        assert norm.region == 'ap-northeast-1'
        assert norm.resourceContainer == '123456789123'

    def test_sqs(self):
        with open('data/prisma_sqs.json') as f:
            alert = load(f)
            self.validate_alert_norm(alert)

    def test_prisma_api(self):
        with open('data/prisma_api.json') as f:
            alert = load(f)
            self.validate_alert_norm(alert)

    def test_splunk(self):
        with open('data/prisma_splunk.json') as f:
            alert = load(f)
            self.validate_alert_norm(alert)
