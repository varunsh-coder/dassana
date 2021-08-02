## Does SecurityGroup Allow Incoming Connections From Internet

Identifies if a security group has rules that allow incoming connections from Internet.

Expected input:
```json
{
  "groupId": "",
  "awsRegion": ""
}
```

Expected output with a sg that allows incoming connections from Internet:
```json
{
  "result": true
}
```