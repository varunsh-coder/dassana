from typing import Dict
import json
from dassana.common.aws_client import LambdaTestContext
import pytest

@pytest.fixture()
def checkov_s3_change_alert():
    alert = {
        "Source": "checkov", 
        "PhysicalResourceId": "boss-test-hellobucket-q99jlx0g35p4", 
        "LogicalResourceId": "HelloBucket", 
        "ResourceType": "AWS::S3::Bucket", 
        "Changes": [{
            "Type": "Resource", 
            "ResourceChange": {
                "Action": "Modify", 
                "LogicalResourceId": "HelloBucket", 
                "PhysicalResourceId": "boss-test-hellobucket-q99jlx0g35p4", 
                "ResourceType": "AWS::S3::Bucket", 
                "Replacement": "False", 
                "Scope": ["Properties"], 
                "Details": [{
                    "Target": {
                        "Attribute": "Properties", 
                        "Name": "AccessControl", 
                        "RequiresRecreation": 
                        "Never"
                    }, 
                "Evaluation": "Static", 
                "ChangeSource": "DirectModification"
                }]
            }
        }], 
        "CheckId": "CKV_AWS_56", 
        "CheckName": "Ensure S3 bucket has 'restrict_public_bucket' enabled", 
        "Account": "032584774331", 
        "Region": "us-west-2"
    }

    return alert


def test_handle_bucket_policy_exists(checkov_s3_change_alert):
    from handler_checkov_cft_normalizer import handle
    result: Dict = handle(checkov_s3_change_alert, LambdaTestContext('tl', env={}, custom={}))
    
    assert result.get('vendorId') == 'checkov'
    assert result.get('alertId') == 'cft-1Tzg5QEb6Y-QSoMyKWwiA4rZ00YwU9s2m0Sojr4fSdo='
    assert result.get('vendorPolicy') == 'CKV_AWS_56'
    assert result.get('csp') == 'aws'
    assert result.get('resourceContainer') == '032584774331'
    assert result.get('region') == 'us-west-2'
    

