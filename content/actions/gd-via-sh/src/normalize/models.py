from typing import Dict, List, Any, Optional
from pydantic import BaseModel


class Resources(BaseModel):
    Partition: str = None
    Type: str = None
    Region: str = None
    Id: str = None


class Detail(BaseModel):
    findings: List[Dict[Any, Any]] = None


class GuardDutyAlert(BaseModel):
    Id: str = None
    ProductArn: str = None
    Region: str = None
    AwsAccountId: str = None
    Types: List[str] = []
    ProductFields: Optional[Dict[str, str]] = None
    Resources: Optional[List[Resources]]
    detail: Detail = None


class DirectResource(BaseModel):
    accessKeyDetails: Optional[Dict[str, str]]
    s3BucketDetails: Optional[List[Dict[Any, Any]]]
    instanceDetails: Optional[Dict[Any, Any]]


class GuardDutyDirect(BaseModel):
    accountId: str = None
    region: str = None
    type: str = None
    resource: DirectResource = None
    arn: str = None
    id: str = None

# class DirectGuardDutyAlert(BaseModel):
#     __root__ = Optional[List[GuardDutyDirect]]