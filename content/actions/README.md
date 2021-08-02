This repository is an example of how to create a Dassana Action

Pre-requisites:
1. Bucket should exist beforehand
2. SAM
3. Docker


1. docker run --rm -v $(pwd)/code:/foo -w /foo lambci/lambda:build-python3.7 pip install -r requirements.txt -t dependencies/python
2. sam package --template-file template.yaml --output-template-file packaged.yaml --s3-bucket BUCKET_NAME
3. sam deploy --template-file packaged.yaml --stack-name STACK_NAME --capabilities CAPABILITY_IAM

Explicit Constraints:
1. dassana-action.yaml FunctionName should be equivalent to the respective LogicalId specified in SAM template
