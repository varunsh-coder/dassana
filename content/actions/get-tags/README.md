## Get Tags

This action expects following two inputs-

```json
{
  "arn": ""
}
```

and returns tags associated with the resource referenced in the ARN.

For example, given an input-
```json
{
  "arn": "arn:aws:ec2:us-east-1:123456789012:security-group/sg-foo"
}
```

the output of the action would be list of tags associated with the security group `foo`:

```json
[
  {
    "name": "Name",
    "value": "foo"
  }
]
```
