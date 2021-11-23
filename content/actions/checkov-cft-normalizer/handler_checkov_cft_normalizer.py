import base64
import time

from json import loads
from typing import Dict, Any, List

from aws_lambda_powertools.utilities.parser import event_parser, parse
from aws_lambda_powertools.utilities.typing import LambdaContext
from pydantic import BaseModel

from dassana.common.aws_client import parse_arn
from dassana.common.models import NormalizedOutput

from cryptography.hazmat.primitives import hashes

class CheckovAlert(BaseModel):
    PhysicalResourceId: str = None
    LogicalResourceId: str = None
    ResourceType: str = None
    Changes: List = None
    CheckId: str = None
    CheckName: str = None
    Account: str = None
    Region: str = None

@event_parser(model=CheckovAlert)
def handle(event: CheckovAlert, context: LambdaContext):
    event_hash = hashes.Hash(hashes.SHA256())
    event_hash.update(str(event).encode('utf-8'))

    event_hash = event_hash.finalize()
    event_hash = f'cft-{base64.urlsafe_b64encode(event_hash).decode("utf-8")}'
    
    output = NormalizedOutput(
        vendorId='checkov',
        alertId=event_hash,
        vendorPolicy=event.CheckId,
        alertTime=int(time.time()),
        csp='aws',
        resourceContainer=event.Account,
        region=event.Region,
        resourceId=event.PhysicalResourceId
    )

    return loads(output.json())
