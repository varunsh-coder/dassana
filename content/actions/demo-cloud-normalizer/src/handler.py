

def handle(event, context):
    return {
        'alertId' : 'someAlertId',    
        'canonicalId' : 'universe/earth/demo-cloud/some-region/some-service/foo',
        'vendorPolicy': 'yolo',
        'csp': 'demo-cloud',
        'resourceContainer': 'some-account-id',
        'region' : 'some-region',
        'service': 'some service',
        'resourceType': 'some-resource-type',
        'resourceId': 'foo'

    }
