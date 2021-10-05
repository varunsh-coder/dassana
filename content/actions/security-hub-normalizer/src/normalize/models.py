from typing import Dict, List, Any, Optional

from pydantic import BaseModel


class Resources(BaseModel):
    Partition: str = None
    Type: str = None
    Region: str = None
    Id: str = None


class Detail(BaseModel):
    findings: List[Dict[Any, Any]] = None


class SecurityHubAlert(BaseModel):
    Id: str = None
    ProductArn: str = None
    Region: str = None
    AwsAccountId: str = None
    Types: List[str] = []
    ProductFields: Optional[Dict[str, str]] = None
    Resources: Optional[List[Resources]]
    detail: Detail = None
