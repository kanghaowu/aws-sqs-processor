# aws-sqs-processor

* This is a Java Application.
* Use TimerTask for Long Polling.
* Receive the Message of AWS SQS.
* Use SNS to Notify in Exceptional cases.

## Framework:

* [Spring Framework](https://projects.spring.io/spring-framework/).
* [AWS Java SDK](https://aws.amazon.com/tw/sdk-for-java/).

## Tool:

* [Gradle](https://gradle.org/) Build and Dependency Management.

## Setting:

### for AWS Environment
* AWS.Region (example:us-west-2)
* AWS.SQS.QueueName
* AWS.SNS.TopicName

### for Client Proxy (if necessary)
* ClientConfig.ProxyOn: true/false
* ClientConfig.ProxyHost
* ClientConfig.ProxyPort

## Implement:
* Processor.java
-- TODO: Process Message, That you want to do
-- TODO: Change processResult for Delete Message
