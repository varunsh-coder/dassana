## Exposure of an EC2 instance to the internet

This action returns the exposure of an EC2 instance to the internet.

On a high level, the action identifies the following questions for an instance:

1) Is the instance attached to an internet-facing Load Balancer as a target?
    1) Does the LoadBalancer have any authentication type rules?
        1) authenticate-cognito / authenticate-oidc
2) Is the instance attached to security group that has ingress rules permitting traffic from the Internet?
3) Does the EC2 instance have a public IP?

### Sample Input:

```json
{
  "instanceArn": "arn:aws:ec2:us-east-1:123456789012:instance/i-09f3989e7c911111",
  "exceptions": [
    {
      "Field": "http-request-method",
      "HttpRequestMethodConfig": {
        "Values": [
          "OPTIONS"
        ]
      }
    }
  ]
}
```

### `appLayer` field contains information about LoadBalancers.
`type`: `str` 

The only permissible type right now is `internet-facing`, meaning internet-facing LoadBalancers.

`canReceiveAuthenticatedTraffic`: `bool`

A boolean that identifies if an EC2 instance is behind a LoadBalancer, that it is configured properly to have requests
authenticated. 

The action checks for rules before authentication. In any event where there are rules before
auth that have the EC2 instance as a target, check if their conditions are
equivalent to the exceptions from the input to the action. This is for special cases
(i.e permitting HTTP(S) OPTION requests). If there are no rules before auth AND if the conditions of the pre-auth
rules are permitted, then `canReceiveAuthenticatedTraffic` will be `True`.

Otherwise, if the LoadBalancer does not have any authentication, `canReceiveAuthenticatedTraffic` will be `False`.

`authConfig`: `List[Dict]`

The rules of the LoadBalancer that are related to the auth configuration (i.e `authenticate-cognito` or
`authenticate-oidc`).

`exceptionMatch: bool`

In the event that there exists rules in the LoadBalancer before authentication,
validate the conditions against input param `exceptions`. If the conditions match, then true; otherwise, false.


### `direct` field contains information about security group(s) and if the EC2 instance has a public IP.

`publicIp`: `str`

Str representing the publicIp of the instance.

`allowedVia`: `List[str]`

A list of security groups that permit ingress traffic from the Internet. 

`isExposed`: `bool`

True if instance has a publicIP and a non-empty allowedVia. Otherwise, False.

### Sample Output:

```json
{
  "appLayer": {
    "type": "internet-facing",
    "canReceiveAuthenticatedTraffic": true,
    "authConfig": {
      "Type": "authenticate-oidc",
      "AuthenticateOidcConfig": {
        "Issuer": "https://accounts.google.com",
        "AuthorizationEndpoint": "https://accounts.google.com/o/oauth2/v2/auth",
        "TokenEndpoint": "https://oauth2.googleapis.com/token",
        "UserInfoEndpoint": "https://openidconnect.googleapis.com/v1/userinfo",
        "ClientId": "XXX.apps.googleusercontent.com",
        "SessionCookieName": "AWSELBAuthSessionCookie",
        "Scope": "openid",
        "SessionTimeout": 604800,
        "OnUnauthenticatedRequest": "authenticate"
      },
      "Order": 1
    }
  },
  "direct": {
    "publicIp": "3.223.222.223",
    "allowedVia": {
      "sg": []
    },
    "isExposed": false
  }
}
```