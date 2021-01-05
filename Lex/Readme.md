# Using Amazon Lex to engage users within a web application

You can create an Amazon Lex bot within a web application to engage your web site visitors. An Amazon Lex bot is functionality that performs on-line chat conversation with users without providing direct contact with a person. For example, the following illustration shows an Amazon Lex bot that engages a user about a hotel room. 

![AWS Video Analyzer](images/pic1.png)

This AWS tutorial guides you through creating an Amazon Lex box and integrating it into a Spring Boot web application. 

**Cost to complete:** The AWS services included in this document are included in the [AWS Free Tier](https://aws.amazon.com/free/?all-free-tier.sort-by=item.additionalFields.SortRank&all-free-tier.sort-order=asc).

**Note:** Be sure to terminate all of the resources you create while going through this tutorial to ensure that you’re no longer charged for them.

#### Topics

+ Prerequisites
+ Create an IntelliJ project named Greetings
+ Add the Spring POM dependencies to your project
+	Set up the Java packages in your project
+	Create the Java logic for the main Boot class
+	Create the HTML files
+	Package the Greetings application into a JAR file


## Prerequisites

To complete the tutorial, you need the following:

+ An AWS account
+ A Java IDE (this tutorial uses the IntelliJ IDE)
+ Java JDK 1.8
+ Maven 3.6 or later

## Create an Amazon Lex bot

The first step is to create an Amazon Lex bot by using the AWS Management Console. In this example, the Amazon Lex **BookTrip** example is used.

1. Sign in to the AWS Management Console and open the Amazon Lex console at https://console.aws.amazon.com/lex/.

2. On the Bots page, choose **Create**.

3. Choose **BookTrip** blueprint (leave the default bot name **BookTrip**).

![AWS Video Analyzer](images/pic2.png)


