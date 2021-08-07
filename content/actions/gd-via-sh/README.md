## GuardDuty Normalizer

This Dassana action normalizes GuardDuty Alerts ingested via Security Hub. Here is a sample output of this action -

```json
{
  "alertId": "arn:aws:guardduty:us-east-1:1234567891012:detector/96bad234c3d0033d695e70c93b8741fb/finding/36bc3df32928fe1c98ac2a6d9a48fb56",
  "arn": "arn:aws:ec2:us-east-1:1234567891012:instance/i-054474987a390f341",
  "policyId": "TTPs/Initial Access/UnauthorizedAccess:EC2-SSHBruteForce",
  "csp": "aws",
  "resourceContainer": "1234567891012",
  "region": "us-east-1",
  "service": "ec2",
  "resourceType": "instance",
  "resourceId": "i-054474987a390f341"
}
```

Note there doesn't seem to a obvious `policyId` that we can point to so we use our best judgement and point to the TTP value.
