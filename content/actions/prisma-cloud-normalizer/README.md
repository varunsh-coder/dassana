# Prisma Cloud Normalizer

This action extracts and normalizes alerts from Prisma Cloud into Dassana-esque format. Currently, only config type alerts for AWS are normalized. Here is a sample output of this action -

```json
{
  "vendorId": "prisma-cloud",
  "alertId": "P-2388888",
  "canonicalId": null,
  "vendorPolicy": "34064d53-1fd1-42e6-b075-45dce495caca",
  "vendorSeverity": "high",
  "csp": "aws",
  "resourceContainer": "123456789123",
  "region": "ap-northeast-1",
  "service": null,
  "alertClassification": {
    "classRh": "",
    "subclass": "",
    "category": "",
    "subcategory": ""
  },
  "resourceType": null,
  "resourceId": "foobar",
  "tags": []
}
```

Note that alerts from Prisma API do not include a vendor severity.