import json
import unittest
from unittest import TestCase

from aws_lambda_powertools.utilities.typing import LambdaContext

import handler


class Test(TestCase):
    # @unittest.SkipTest()
    def test_handle(self):
        input_json = '{ "canonicalId":"arn:aws:ec2:us-east-1:363265257036:security-group/sg-04b9324dd3fd32ffd"}'
        context = LambdaContext()
        context.__setattr__(context.client_context, None)
        result = handler.handle(json.loads(input_json), context)
        print(result)
