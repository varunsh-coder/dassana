from typing import Dict, Any

from aws_lambda_powertools.utilities.typing import LambdaContext
from boto3 import Session


class DassanaAwsObject(object):
    def __init__(self):
        self._session = Session()

    def create_aws_client(self, event: Dict[str, Any], context, service, region: str):

        if context == None:
            return self._session.client(service, region);
        else:
            accessKey = context.client_context.custom.get('AWS_ACCESS_KEY_ID');
            secretKey = context.client_context.custom.get('AWS_SECRET_ACCESS_KEY');
            sessionToken = context.client_context.custom.get('AWS_SESSION_TOKEN');
            return self._session.client(service, region, None, None, None, None, accessKey, secretKey, sessionToken)
