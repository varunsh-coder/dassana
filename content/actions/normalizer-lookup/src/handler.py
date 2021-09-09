from glob import glob

from aws_lambda_powertools.utilities.parser import event_parser
from aws_lambda_powertools.utilities.typing import LambdaContext
from pydantic import BaseModel
from dassana.common.models import NormalizedOutput, AlertClassification
from yaml import safe_load
from json import loads


class Input(BaseModel):
    input: NormalizedOutput


file_paths = glob('/opt/aws/**/*.yaml', recursive=True)


def find_policy_match(file_path, vendor, csp, policyId):
    with open(file_path, 'r') as f:
        yml = safe_load(f)
        if yml.get('csp') != csp:
            return
        filters = yml.get('filters', [])
        if filters is None:
            return
        for fil in filter(lambda x: x.get('vendor', '') == vendor, filters):
            if policyId in fil.get('policies', []):
                return yml.get('service'), yml.get('resource-type'), yml.get('csp'), AlertClassification(
                    classRh=yml.get('class'),
                    subclass=yml.get('subclass'),
                    category=yml.get('category'),
                    subcategory=yml.get('subcategory', '')
                ), yml.get('class')


@event_parser(model=Input)
def handle(event: Input, context: LambdaContext):
    if event.input.resourceType is not None and event.input.service is not None and event.input.csp is not None:
        return loads(event.input.json())
    service, resource_type, csp, rh, class_ = next(filter(lambda x: x is not None,
                                                          map(find_policy_match, file_paths,
                                                              [event.input.vendorId] * len(file_paths),
                                                              [event.input.csp] * len(file_paths),
                                                              [event.input.vendorPolicy] * len(file_paths))),
                                                   (None, None, None, None, None))
    event.input.service = service if service is not None else event.input.service
    event.input.resourceType = resource_type if service is not None else event.input.service
    event.input.csp = csp if service is not None else event.input.service

    if rh:
        event.input.alertClassification = rh if rh is not None else event.input.alertClassification
        # Pydantic serialization bug on alias "class", so the below line handles for aforementioned case
        event.input.alertClassification.classRh = class_ if class_ is not None \
            else event.input.alertClassification.classRh
    return loads(event.input.json(by_alias=True))
