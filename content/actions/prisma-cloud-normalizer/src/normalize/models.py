from typing import Dict, List, Any

from pydantic import BaseModel


class PrismaResourceData(BaseModel):
    arn: str = None
    tagSets: Dict[str, str] = {}
    tags: List[Dict[str, str]] = []


class PrismaResource(BaseModel):
    rrn: str
    id: str
    cloudType: str
    accountId: str
    region: str
    regionId: str
    resourceType: str = None
    data: PrismaResourceData


class Policy(BaseModel):
    policyId: str = None
    policyType: str = None


class PrismaAlert(BaseModel):
    id: str = None
    status: str = None
    alertId: str = None
    alertStatus: str = None
    policy: Policy = None
    policyId: str = None
    policyType: str = None
    message: Dict[Any, Any] = None
    history: List[Dict[Any, Any]] = None
    resource: PrismaResource = None
    tags: List[Dict[Any, Any]] = None
