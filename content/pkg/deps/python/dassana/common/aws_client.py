from typing import Dict, Any

from aws_lambda_powertools.utilities.typing import LambdaContext
from boto3 import Session


class DassanaAwsObject(object):
    def __init__(self, default_region):
        self._session = Session()
        self._default_region = default_region

    def create_aws_client(self, event: Dict[str, Any], context: LambdaContext,
        service):
        return self._session.client(service,
                                    aws_access_key_id=context.client_context.custom.get(
                                        "AWS_ACCESS_KEY_ID",
                                        None
                                    )
                                    if not context
                                    else None,
                                    aws_secret_access_key=context.client_context.custom.get(
                                        "AWS_SECRET_ACCESS_KEY",
                                        None
                                    )
                                    if not context
                                    else None,
                                    aws_session_token=context.client_context.custom.get(
                                        "AWS_SESSION_TOKEN",
                                        None
                                    )
                                    if not context
                                    else None,
                                    region_name=event.get('awsRegion',
                                                          self._default_region)
                                    )
