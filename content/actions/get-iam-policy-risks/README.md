## Determine the granted permissions and respective least privilege violations for a role, user, or policy

Returns the policies attached to, or inlined in, a given IAM arn, along with [cloudsplaining](https://github.com/salesforce/cloudsplaining) findings. 

### Sample Input:
```json
{
  "iamArn": "arn:aws:iam::032584774331:role/dassana-DassanaEngineRole"
}
```

### Sample Output:
```json
{
  "result": {
    "PolicyFindings": {
      "ServiceWildcard": [
        "s3"
      ],
      "ServicesAffected": [
        "logs",
        "lambda"
      ],
      "PrivilegeEscalation": [],
      "ResourceExposure": [],
      "DataExfiltration": [],
      "CredentialsExposure": [],
      "InfrastructureModification": [
        "logs:PutLogEvents",
        "lambda:InvokeFunction",
        "logs:CreateLogGroup",
        "logs:CreateLogStream"
      ]
    }
  }
}
```
