# Build Notes
---

These instructions are for building the entire stack locally and are intended for developers who want to make changes to how Dassana Engine work or make changes to the base cloud formation template that is made available on the [deployment page](https://docs.dassana.io/docs/getting-started/installation)

## Prerequisites

1. [SAM CLI](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-install.html)
 1. git
 2. AWS creds

### Recommended Setup

You don't absolutely need these, but for faster build creation, we recommend them (see more details in the build steps section below)

1. Java 11 and maven
   Ensure that `java` and `mvn` commands are available on PATH
1. Python3.
	  If you are using  MacOS, SAM CLI will look for python3 installation in the `/usr/local/bin/` directory. If you have python3 installed somewhere else, say, `/usr/local/opt/python@3.7/bin/python3.7` you can run the command to create a link  `ln -sf /usr/local/opt/python@3.7/bin/python3.7 /usr/local/bin/`




## Build steps

1. Get code: `git clone git@github.com:dassana-io/dassana.git`
2. Go to `cd content/pkg` directory 
3. run the `./build.sh` file like this- 
	```bash
	./build.sh my-personal-s3-bucket us-east-1 dassana
	``` 
	here,`my-personal-s3-bucket` is an s3 bucket where build artifacts (Dasasna engine, actions, CFT etc) are stored. 
	`us-east-1` is the AWS region
	`dassana` is the name of the stack. You can name it whatever you like

If you are a curious mind, by now you must have opened the `build.sh`. Let's review what it does- 
```
sam build --cached --parallel --region $2 --use-container
```
this line is obvious, it builds the sam app. Notice the useage of `--use-container`. This makes SAM use containers to build functions. If you find yourself building containers a lot locally, you will find that it slows down the build time. In local setup, feel free to remove the `--use-container` from the line. As soon as you do that, you will realize why we said that Java and Python3 are recommended.

Next, we have `mvn clean process-resources -DkskipTests` which simply copies the `content` directory to `engine/src/main/resources`. This is how the engine gets access to default contnet. 

Next few lines are hacky- 
```bash
rm -rf ../content/pkg/.aws-sam/build/DassanaEngine/content
rm -rf ../content/pkg/.aws-sam/build/DassanaEngineApi/content
cp -R ./target/classes/content ../content/pkg/.aws-sam/build/DassanaEngine/content/
cp -R ./target/classes/content ../content/pkg/.aws-sam/build/DassanaEngineApi/content/
```
why do we need them? well, as it turns out when SAM builds the app, it copies source code to some directory and then it runs maven command. This causes the content to be not available to engine. That's why above lines copies the content to SAM's artifacts.

Finally, we have these lines- 
```bash
sam package -t .aws-sam/build/template.yaml --s3-bucket $1 --region $2 --output-template-file uploaded-template.yaml

sam deploy --template-file uploaded-template.yaml --stack-name $3  --capabilities CAPABILITY_NAMED_IAM  CAPABILITY_AUTO_EXPAND --region $2
```
these are easy to follow- the package command uploads the artficats to a s3 bucket and deploy command deploys the stack. 

if you want to make change to CFT, just make the change in `uploaded-template.yaml` file and run the sam deploy command again. 
