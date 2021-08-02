from json import dumps

from pydantic.main import BaseModel


class NormalizedOutput(BaseModel):
    alertId: str
    canonicalId: str
    vendorPolicy: str
    csp: str
    resourceContainer: str
    region: str
    service: str
    resourceType: str
    resourceId: str

    # @validator('csp')
    # def
    def toJSON(self):
        return dumps(self, default=lambda o: o.__dict__,
                     sort_keys=True, indent=4)
