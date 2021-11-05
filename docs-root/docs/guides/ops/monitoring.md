#Monitoring

The most important thing to monitor are dead letter queue messages. Let's say you deployed Dassana using stack name `foo`, you will find that a SQS queue with name `foo-DassanaDeadLetterQueue` had been created. You should review every message in this queue and review logs to see what went wrong.
