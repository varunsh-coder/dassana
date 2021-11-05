#Troubleshooting

The alerts are processed either by the `DassanaEngine` or the `DassanaEgnineApi` lambda functions. Depending upon which function is getting invoked (`DassanaEgnineApi` is invoked when you use [Dassana Editor](https://editor.dassana.io) and `DassanaEngine` is invoked when you put alerts in the inbound queue or when SecurityHub alerts are processed), you can review the respective logs in the cloudwatch logs.

Let's say during deployment you used the stack name `dassana`, you can then navigate to CloudWatch log groups and find the logsteam. Here is an example of a logstream name:
`/aws/lambda/dassana-DassanaEngineApi-prhPF5y7TBBN`
