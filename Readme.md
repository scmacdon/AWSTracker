#  Creating an AWS Tracking Application using Spring Boot and AWS Services

You can develop an AWS web application that tracks and reports on items by using these Amazon Web Services: 

+ Amazon Relational Database Service (RDS)
+ Amazon Simple Email Service (SES)
+ Amazon DynamoDB
+ Amazon S3
+ AWS Elastic Beanstalk

In addition, the AWS Tracking application uses Spring Boot APIs to build a model, views, and controllers. The AWS Tracking application is a secure web application that uses Spring Boot Security and requires a user to log into the application. For more information, see https://www.tutorialspoint.com/spring_boot/spring_boot_securing_web_applications.htm. 

The following illustration shows the login page. 

![AWS Tracking Application](images/track1.png)

After a user logs into the system, they can perform these operations: 

+ Enter a new item into the system
+ View all active items
+ View archived items that have been completed. 
+ Modify active items
+ Send a report to an email recipient. 

The following illustration shows the new item section of the application. 

![AWS Tracking Application](images/track2.png)

This development document guides you through creating the AWS Tracker application. Once the application is developed, this document teaches you how to deploy it to the AWS Elastic Beanstalk.

The following illustration shows you the structure of the Java project that you create by following this development document.

![AWS Tracking Application](images/track3.png)

To follow along with the document, you require the following:

+ An AWS Account.
+ A Java IDE (for this example, IntelliJ is used).
+ Java 1.8 SDK and Maven.

The following illustration shows the project that is created.

**Cost to Complete**: The AWS Services included in this document are included in the AWS Free Tier.

**Note**: Please be sure to terminate all of the resources created during this document to ensure that you are no longer charged.

## Create an IntelliJ Project named AWSBlog
