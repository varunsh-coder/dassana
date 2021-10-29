from unittest import mock

from dassana.common.aws_client import LambdaTestContext
import pytest


@pytest.fixture()
def example():
    return


@pytest.fixture()
def ec2_instance_without_role(ec2_client, networking):
    sg_resp = ec2_client.create_security_group(GroupName='foobar', VpcId=networking.get('vpc').get('id'),
                                               Description='testing')
    ec2_client.authorize_security_group_ingress(GroupId=sg_resp.get('GroupId'),
                                                IpPermissions=[
                                                    {
                                                        'IpProtocol': 'tcp',
                                                        'FromPort': 22,
                                                        'ToPort': 22,
                                                        'IpRanges': [{'CidrIp': '0.0.0.0/0'}]
                                                    }
                                                ])
    ec2_client.run_instances(ImageId='ami-1234-foobar',
                             MinCount=1,
                             MaxCount=1,
                             SecurityGroups=[sg_resp.get('GroupId')])
    instances = ec2_client.describe_instances(
        Filters=[
            {
                'Name': 'image-id',
                'Values': ['ami-1234-foobar']
            }
        ]
    )['Reservations'][0]['Instances']
    return instances[0]['InstanceId'], instances[0]['PublicIpAddress'], sg_resp.get('GroupId')


@pytest.fixture()
def internal_load_balancer(elb_client, networking, ec2_instance_without_role):
    ec2_instance_without_role = ec2_instance_without_role[0]
    lb_resp = elb_client.create_load_balancer(Name='foobar', Type='application', Scheme='internal',
                                              Subnets=[networking.get('subnet').get('id')])
    listener_resp = elb_client.create_listener(LoadBalancerArn=lb_resp.get('LoadBalancers')[0].get('LoadBalancerArn'),
                                               DefaultActions=[])
    target_group_resp = elb_client.create_target_group(Name='foobar', Protocol='HTTPS', TargetType='instance',
                                                       Port=22)
    elb_client.register_targets(TargetGroupArn=target_group_resp.get('TargetGroups')[0].get('TargetGroupArn'),
                                Targets=[
                                    {
                                        'Id': ec2_instance_without_role,
                                        'Port': 22,
                                        'AvailabilityZone': 'all'
                                    }
                                ])
    elb_client.create_rule(ListenerArn=listener_resp.get('Listeners')[0].get('ListenerArn'),
                           Conditions=[
                               {
                                   'Field': 'path-pattern',
                                   'Values': ['/']
                               }
                           ],
                           Actions=[
                               {
                                   "Type": "authenticate-cognito",
                                   "Order": 1,
                                   "AuthenticateCognitoConfig": {
                                       "UserPoolArn": "?1",
                                       "UserPoolClientId": "?2",
                                       "UserPoolDomain": "?2",
                                       "SessionCookieName": "AWSELBAuthSessionCookie",
                                       "Scope": "openid",
                                       "SessionTimeout": 604800,
                                       "OnUnauthenticatedRequest": "authenticate",
                                   },
                               },
                               {
                                   "Type": "forward",
                                   "TargetGroupArn": target_group_resp.get('TargetGroups')[0].get('TargetGroupArn'),
                                   "Order": 2
                               }
                           ],
                           Priority=123)
    return


# Filters for the following implementations have not yet been implemented in moto
@mock.patch('handler_ec2_exposure.INSTANCE_FILTER', [])
@mock.patch('handler_ec2_exposure.SG_FILTER', [[{
    'Name': 'ip-permission.cidr',
    'Values': [
        '0.0.0.0/0'
    ]
}]])
# This basic unit test only tests some business logic
def test_ec2_exposure(internal_load_balancer, ec2_instance_without_role, region):
    from handler_ec2_exposure import handle, Exposure
    resp = handle({'instanceId': ec2_instance_without_role[0], 'region': region}, LambdaTestContext('foobar', env={},
                                                                                                    custom={}))
    exp = Exposure(**resp)
    assert exp.direct.isExposed
    assert str(exp.direct.publicIp) == ec2_instance_without_role[1]
    assert ec2_instance_without_role[2] in exp.direct.allowedVia.sg

    # Moto does not replicate integration scenario for LB accurately enough
    # Testing behavior for appLayer boolean will be shifted towards integration / E2E tests
    assert isinstance(exp.appLayer.canReceiveUnauthenticatedTraffic, bool)
    assert isinstance(exp.appLayer.exceptionMatch, bool)
    assert isinstance(exp.appLayer.behindLoadBalancer, bool)
