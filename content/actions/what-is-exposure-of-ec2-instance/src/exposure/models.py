from typing import List, Dict

from pydantic import BaseModel
from pydantic.json import IPv4Address


class Exposure(BaseModel):
    class Direct(BaseModel):
        class AllowedVia(BaseModel):
            sg: List[str]

        publicIp: IPv4Address = None
        allowedVia: AllowedVia = None
        isExposed: bool = None

    class AppLayer(BaseModel):
        type: str = None
        canReceiveUnauthenticatedTraffic: bool = False
        exceptionMatch: bool = False
        authConfig: Dict = None

    appLayer: AppLayer
    direct: Direct
