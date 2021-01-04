# Securing a Web Application by using Amazon Cognito

Amazon Cognito lets you add user sign-up, sign-in, and access control to your web applications quickly and easily. Amazon Cognito scales to millions of users and supports sign-in with social identity providers, such as Facebook, Google, and Amazon, and enterprise identity providers such as OAuth2. In this AWS tutorial, OAuth2 and Amazon Cognito are used to secure a web application. This means a user has to log into the application by using credentials of a user defined in an Amazon Cognito User Pool. For example, when a user accesses a web application, they see a web page that lets anonymous users view a log in page, as shown in this illustration.   

![AWS Tracking Application](images/pic1a.png)

When the user clicks the log in button, they are presented with a log in form where they can enter their user credentials.

![AWS Tracking Application](images/pic2.png)

After the user enters their credentials, they can access the web application. 

![AWS Tracking Application](images/pic3.png)

**Note**: The Spring Boot application used in this AWS tutorial is created by following [Creating your first AWS Java web application](https://github.com/awsdocs/aws-doc-sdk-examples/tree/master/javav2/usecases/creating_first_project). Before following along with this AWS tutorial, you must complete that AWS tutorial. 

The following illustration shows the project files created in this tutorial (most of these files were created by following the tutorial referenced in the previous link). The files circled in red are the new files specific to securing the application by using Amazon Cognito. 

![AWS Tracking Application](images/pic4.png)

## Prerequisites

To complete the tutorial, you need the following:

+ An AWS account
+ A Java IDE (this example uses IntelliJ)
+ Java 1.8 SDK and Maven
+ Complete the Creating your first AWS Java web application tutorial. 

**Cost to complete:** The AWS services you'll use in this example are part of the [AWS Free Tier](https://aws.amazon.com/free/?all-free-tier.sort-by=item.additionalFields.SortRank&all-free-tier.sort-order=asc).

**Note:** When you're done developing the application, be sure to terminate all of the resources you created to ensure that you're not charged.

**Topics**

+ Update the POM file
+ Create an Amazon Cognito User Pool
+	Define a client application within the User Pool
+	Configure the client application
+	Configure a domain name
+	Create a user
+	Modify your web application
