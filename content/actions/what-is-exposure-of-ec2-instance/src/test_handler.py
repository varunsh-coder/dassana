import unittest
from json import load
from typing import Dict, Any
from unittest import TestCase

from aws_lambda_powertools.utilities.typing import LambdaContext
from aws_lambda_powertools.utilities.validation import validator

from dassana.common.aws_client import DassanaAwsObject, LambdaTestContext
from handler import handle, Exposure


class TestHandler(TestCase):
    def test_is_authenticated_with_conditions(self):
        result = handle(
            {
                "instanceArn": "arn:aws:ec2:us-east-1:123456789012:instance/i-09f3989e7c9b846d8",
                "exceptions": [
                    {
                        "Field": "http-request-method",
                        "HttpRequestMethodConfig": {
                            "Values": [
                                "OPTIONS"
                            ]
                        }
                    }
                ]
            },
            LambdaTestContext("what-is-exposure-of-ec2-instance")
        )

        exposure = Exposure.parse_obj(result)
        self.assertTrue(exposure.appLayer.canReceiveUnauthenticatedTraffic)

    def test_is_authenticated(self):
        result = handle(
            {
                "instanceArn": "arn:aws:ec2:us-east-1:123456789012:instance/i-09f3989e7c9b846d8"
            },
            LambdaTestContext("what-is-exposure-of-ec2-instance")
        )

        exposure = Exposure.parse_obj(result)
        self.assertFalse(exposure.appLayer.canReceiveUnauthenticatedTraffic)
