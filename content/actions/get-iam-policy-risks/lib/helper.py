from cloudsplaining.output.policy_finding import PolicyFinding
from cloudsplaining.scan.policy_document import PolicyDocument
from cloudsplaining.shared.exclusions import Exclusions


def parse_cloudsplaining(policy_document, exclusions_config=None) -> PolicyFinding:
    if exclusions_config is None:
        exclusions_config = {}
    policy_document = PolicyDocument(policy_document)
    exclusions = Exclusions(exclusions_config)
    return PolicyFinding(policy_document=policy_document, exclusions=exclusions)
