## GuardDuty Normalizer

This Dassana action normalizes GuardDuty Alerts ingested via Security Hub. Here is a sample output of this action -

```json
{
  "vendorId": "aws-guardduty",
  "alertId": "a0bdfff401df680ce958b027abe1c311",
  "canonicalId": "arn:aws:ec2:us-east-1:020747060415:instance/i-12345678909876543",
  "vendorPolicy": "UnauthorizedAccess:EC2/SSHBruteForce",
  "vendorSeverity": "low",
  "csp": "aws",
  "resourceContainer": "020747060415",
  "region": "us-east-1",
  "alertTime": "1616985961",
  "alertState": "ACTIVE",
  "service": "ec2",
  "alertClassification": {
    "classRh": "",
    "subclass": "",
    "category": "",
    "subcategory": ""
  },
  "resourceType": "instance",
  "resourceId": "i-12345678909876543",
  "tags": []
}
```

