# Prisma Cloud Normalizer

This action extracts and normalizes alerts from Prisma Cloud into Dassana-esque format. Currently, only config type alerts for AWS are normalized. Here is a sample output of this action -

```json
{
	"csp": "aws",
	"resourceId": "dassana-deploy-bucket",
	"alertClassification": {
		"subclass": "config",
		"category": "iam",
		"subcategory": "public-access",
		"class": "risk"
	},
	"vendorSeverity": "high",
	"service": "s3",
	"vendorPolicy": "34064d53-1fd1-42e6-b075-45dce495caca",
	"vendorId": "prisma-cloud",
	"alertId": "P-2388888",
    "alertTime": "1615010072772",
	"resourceContainer": "032584774331",
	"region": "us-east-2",
	"resourceType": "bucket",
	"tags": []
}