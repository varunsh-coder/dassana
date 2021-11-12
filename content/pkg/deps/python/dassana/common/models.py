from json import dumps
from typing import Dict, List

from pydantic import Field
from pydantic.main import BaseModel


class AlertClassification(BaseModel):
    classRh: str = Field("", alias='class')
    subclass: str = ""
    category: str = ""
    subcategory: str = ""


class NormalizedOutput(BaseModel):
    vendorId: str = None
    alertId: str
    canonicalId: str = None
    vendorPolicy: str
    vendorSeverity: str = None
    csp: str = None
    resourceContainer: str
    region: str
    alertTime: str = None
    service: str = None
    alertClassification: AlertClassification = AlertClassification()
    resourceType: str = None
    resourceId: str = None
    tags: List[Dict[str, str]] = []


class ArnComponent(BaseModel):
    arn: str = None
    partition: str = 'aws'
    service: str = None
    region: str = None
    account: str = None
    resource: str = None
    resource_type: str = None
