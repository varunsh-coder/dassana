from json import dumps
from typing import Dict, List

from pydantic import Field
from pydantic.main import BaseModel


class ResourceHierarchy(BaseModel):
    classRh: str = Field(None, alias='class')
    subclass: str = None
    category: str = None
    subcategory: str = None


class NormalizedOutput(BaseModel):
    vendorId: str = None
    alertId: str
    canonicalId: str = None
    vendorPolicy: str
    csp: str = None
    resourceContainer: str
    region: str
    service: str = None
    resourceHierarchy: ResourceHierarchy = None
    resourceType: str = None
    resourceId: str = None
    tags: List[Dict[str, str]] = []


class ArnComponent(BaseModel):
    arn: str
    partition: str
    service: str
    region: str
    account: str
    resource: str
    resource_type: str
