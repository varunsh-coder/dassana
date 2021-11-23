# Checkov CFT Normalizer

This action extracts and normalizes alerts from Checkov's Cloudformation Template scanner into a Dassana-esque format. Here is a sample output of this action -

```json
{
	"csp": "aws",
	"resourceId": "boss-test-hellobucket-q99jlx0g35p4",
	"vendorPolicy": "CKV_AWS_19",
	"vendorId": "checkov",
	"alertId": "cft-TWS1DlVGcNSHANEkSFZFZkhRpHFIM2RxacOdqw6zVoA=",
	"resourceContainer": "032584774331",
	"region": "us-west-2",
	"alertTime": "1637608708"
}
```
