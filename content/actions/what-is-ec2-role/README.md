## What is EC2 Role

Returns the AWS IAM Role associated with the ec2 instance. 

### Sample Input:
```json
{
  "instanceId": "i-05094ccff7acfd312",
  "region": "us-east-1"
}
```

### Sample Output:
```json
{
  "result": 
    {
      "roleArn": "arn:aws:iam::123456789012:instance-profile/Webserver",
      "roleName":"WebServer"
    }
}
```
