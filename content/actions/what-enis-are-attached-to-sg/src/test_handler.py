import json
import unittest
from unittest import TestCase

from dassana.common.aws_client import LambdaTestContext

import handler


class Test(TestCase):
    # enable it by commenting the line below2 for local testing only
    @unittest.SkipTest
    def test_handle(self):
        input_json = '{"canonicalId": "arn:aws:ec2:us-east-1:363265257036:security-group/sg-0c78a9d8a495828ad"} '
        result = handler.handle(json.loads(input_json), LambdaTestContext("foobar"))
        print(result)
