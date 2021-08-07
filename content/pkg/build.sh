set -eu
sam build --cached --parallel --region $2
cd ../../engine
mvn process-resources -DkskipTests
cp -R ./target/classes/workflows ../content/pkg/.aws-sam/build/DassanaEngine/
cp -R ./target/classes/workflows ../content/pkg/.aws-sam/build/DassanaEngineApi/

cd ../content/pkg
sam package -t .aws-sam/build/template.yaml --s3-bucket $1 --region $2 --output-template-file uploaded-template.yaml
sam deploy --template-file uploaded-template.yaml --stack-name $3  --capabilities CAPABILITY_NAMED_IAM  CAPABILITY_AUTO_EXPAND --region $2
