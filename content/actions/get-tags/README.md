## Get Tags

This action expects following two inputs-

```json
{
  "arn": "",
  "region": "",
  "service": ""
}
```

and returns tags associated with the resource referenced in the ARN.

For example, given an input-
```json
{
  "region" : "us-east-1",
  "arn": "arn:aws:ec2:us-east-1:123456789012:security-group/sg-foo"
}
```

the output of the action would be list of tags associated with the security group `foo`:

```json
[
  {
    "key": "Name",
    "value": "foo"
  }
]
```
