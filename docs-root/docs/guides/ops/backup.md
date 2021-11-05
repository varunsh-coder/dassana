#Backup

Being a mostly stateless system, Dassana doesn't need the usual backup procedures as it is expected that the processed alerts are persisted in some third party system such as SIEM etc
Having said that, the only stateful entity in Dassana are your custom workflows. These workflows are stored in an S3 bucket when Dassana is deployed.

Let's say you deployed Dassana with stack name `foo`. You will find that an s3 bucket with name like `foo-dassanabucket-1g0ea9s9l2wkh` had been created.
In this bucket you will find your custom workflows. We recommend that you backup this s3 bucket.

For more information on how to replicate s3 bucket, refer to [AWS documentation](https://docs.aws.amazon.com/AmazonS3/latest/userguide/replication.html)
