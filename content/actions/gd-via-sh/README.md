## GuardDuty Normalizer

This Dassana action normalizes GuardDuty Alerts ingested via Security Hub. Here is a sample output of this action -

```json
{
  "csp": "aws",
  "alertClassification": {
    "subclass": "",
    "category": "",
    "subcategory": "",
    "classRh": ""
  },
  "resourceId": "i-0721cd3d48431cf8f",
  "canonicalId": "arn:aws:ec2:us-east-1:1234567890:instance/i-0721cd3d48431cf8f",
  "service": "ec2",
  "vendorPolicy": "UnauthorizedAccess:EC2/SSHBruteForce",
  "vendorId": "aws-guardduty",
  "alertId": "arn:aws:guardduty:us-east-1:1234567890:detector/a2bdf2d15d3f3187077af621af3e234d/finding/92be33c9c933159cc5e8eed7a7d42af7",
  "resourceContainer": "020747060415",
  "region": "us-east-1",
  "resourceType": "instance",
  "tags": []
}
```

