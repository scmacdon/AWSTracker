#  Building an AWS Workflow that sends notifications over multiple channels by using the AWS SDK for Java

You can use Amazon Web Services to create a workflow that sends notifications over multiple channels. There are many practical business needs for this type of functionality. For example, assume your organization is a weather agency that needs to warn many people of detrimental weather approaching. Or your organization needs to send out alerts when kids are missing. 

The use case that this AWS tutorial addresses is assume you work at a school and you need to alert parents when a student skips school. Do you send an email message, do you phone the parents, or do you send a text message to a mobile device? The AWS workflow created in this tutorial sends messages over multiple channels, including email, as shown in this illustration. 

![AWS Tracking Application](images/message.png)

In this AWS tutorial, you create an AWS serverless workflow by using the AWS SDK for Java and AWS Step Functions. Each workflow step is implemented by using an AWS Lambda function. Lambda is a compute service that enables you to run code without provisioning or managing servers. For more information about Lambda, see
[What is AWS Lambda](https://docs.aws.amazon.com/lambda/latest/dg/welcome.html).

To send notifications over multiple channels, you can use these AWS Services:

+ Amazon Pinpoint service
+ Amazon Simple Notification Service (SNS)
+ Amazon Simple Email Service (SES)


**Cost to complete:** The AWS services included in this document are included in the [AWS Free Tier](https://aws.amazon.com/free/?all-free-tier.sort-by=item.additionalFields.SortRank&all-free-tier.sort-order=asc).

**Note:** Be sure to terminate all of the resources you create while going through this tutorial to ensure that you’re no longer charged.

#### Topics

+ Prerequisites
+ Understand the workflow
+ Create an IAM role that is used to execute Lambda functions
+ Create a workflow by using AWS Step functions
+ Create an IntelliJ project named LambdaFunctions
+ Add the POM dependencies to your project
+ Create Lambda functions by using the Lambda API in the AWS SDK for Java
+ Package the project that contains Lambda functions
+ Deploy Lambda functions
+ Add Lambda functions to workflows
+ Invoke the workflow from the AWS Console

## Prerequisites
To follow along with the tutorial, you need the following:
+ An AWS Account.
+ A Java IDE (for this tutorial, the IntelliJ IDE is used).
+ Java 1.8 JDK.
+ Maven 3.6 or higher.

## Understand the workflow

The following figure shows the workflow you'll create with this tutorial that is able to send out multiple messages over multiple channels. 

![AWS Tracking Application](images/workflowmodelA.png)

The following is what happens at each step in the workflow:
+ **Start** -  Initiates the workflow.
+ **Determines the missing students** – Determines the students that are absent for that given day. For this AWS tutorial, a MySQL database is queried to track the students that are absent. This workflow step then creates XML that is passed to the next step. This example shows how a Lambda function can query data from an Amazon RDS table.
+ **Send all notifications** – Parses the XML that contains all absent students. For each student, this step invokes the Amazon Simple Notification Service (SNS) to send a mobile text message, the Pinpoint Service to send a voice message, and the Amazon Simple Email Service (SES) to send an email message. This example shows how a single Lambda function can invoke multiple AWS Services. 
+ **End** - Stops the workflow.

In this AWS tutorial, an Amazon RDS MySQL database is used to track the students who are absent. The MySQL table is named **students** and contains these fields:

+ **idstudents** - An int value that represents the PK.
+ **date** - A date value that specifies the date when the student was absent.
+ **first** - A VARCHAR(45) value that specifies the students first name.
+ **last** - A VARCHAR(45) value that specifies the students last name.
+  **mobile** - A VARCHAR(45) value that specifies the mobile number.
+ **phone** - A VARCHAR(45) value that specifies the home phone number.
+ **email** - A VARCHAR(45) value that specifies the email address.

The workflow starts by determining the absent students for the given day by querying the **students** table. Then the workflow dynamically creates XML that contains the absent students.  

      <?xml version="1.0" encoding="UTF-8"?>
	<Students>
         <Student>
          <Name>Sam</Name>
          <Mobile>15558397418</Mobile>
          <Phone>155538397418</Phone>
          <Email>scmacdon@noserver.com</Email>
        </Student>
        <Student>
         <Name>Laurie</Name>
          <Mobile>15554621058</Mobile>
          <Phone>155558397418</Phone>
         <Email>lmccue@cnoserver.com</Email>
        </Student>
      </Students>

The second workflow step parses the XML and for each student invokes multiple AWS services to send messages over different channels.   

## Create an IAM role that's used to execute Lambda functions

Create the following two IAM roles:
+ **lambda-support** - Used to invoke Lamdba functions.
+ **workflow-support** - Used to enable AWS Step Functions to invoke the workflow.

This tutorial uses the Amazon SNS, Amazon SES, and Amazon Pinpoint to send messages. The **lambda-support** role has to have policies that enable it to invoke these services from a Lambda function.

#### To create an IAM role

1. Open the AWS Management Console. When the page loads, enter **IAM** in the search box, and then choose **IAM** to open the IAM console.

2. In the navigation pane, choose **Roles**, and on the **Roles** page, choose **Create Role**.

3. Choose **AWS service**, and then choose **Lambda**.

![AWS Tracking Application](images/Lambda1.png)

4. Choose **Permissions**.

5. Search for **AWSLambdaBasicExecutionRole**.

6. Choose **Next Tags**.

7. Choose **Review**.

8. Name the role **lambda-support**.

![AWS Tracking Application](images/LambdaName.png)

9. Choose **Create role**.

10. Choose **lambda-support** to view the overview page.

11. Choose **Attach Policies**.

12. Search for **AmazonSESFullAccess**, and then choose **Attach policy**.

13. Search for **AmazonSNSFullAccess**, and then choose **Attach policy**. When you're done, you can see the permissions.

![AWS Tracking Application](images/Policies.png)

**Note**: Repeat this process to create **workflow-support**. For step three, instead of choosing **Lambda**, choose **Step Functions**. You don't need to perform steps 11-13.  

### Create a custom policy for Pinpoint voice

Because the Lambda function invokes the Pinpoint service’s **sendVoiceMessage** method, the **lambda-support** role needs permission to invoke this operation. To perform this task, you need to create a custom policy using this JSON.

     {
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "FullAccess",
            "Effect": "Allow",
            "Action": [
                "sms-voice:*"
            ],
            "Resource": "*"
        }
     ]
    } 

**Note**: To create a custom policy, see [Policies and permissions in IAM ](https://docs.aws.amazon.com/IAM/latest/UserGuide/access_policies.html).

## Create a serverless workflow by using Step functions

To define a workflow that sends notifications over multiple channels by using AWS Step Functions, you create an Amazon States Language (JSON-based) document to define your state machine. An Amazon States Language document describes each step. After you define the document, AWS Step Functions provides a visual representation of the workflow. The following figure shows a visual representation of the workflow.

![AWS Tracking Application](images/workflowmodelA.png)

Workflows can pass data between steps. For example, the **Determine the missing students** step queries the **students** table, dynamically create XML that specifies all of the absentstudents based on the date, and passes the XML to the **Send All Notifications** step. 

**Note**: Later in this tutorial, you'll create application logic in the Lambda function to read and process the data values.  

#### To create a workflow

1. Open the AWS Step Functions console at https://us-west-2.console.aws.amazon.com/states/home.

2. Choose **Create State Machine**.

3. Choose **Author with code snippets**. In the **Type** area, choose **Standard**.

![AWS Tracking Application](images/StepFunctions.png)

4. Specify the Amazon States Language document by entering the following code.

        {
        "Comment": "A simple AWS Step Functions state machine that sends mass notifications over multiple channels.",
        "StartAt": "Determine the missing students",
        "States": {
         "Determine the missing students": {
         "Type": "Task",
         "Resource": " arn:aws:lambda:REGION:ACCOUNT_ID:function:FUNCTION_NAME ",
         "Next": "Send All Notifications"
         },
        "Send All Notifications": {
         "Type": "Task",
         "Resource": " arn:aws:lambda:REGION:ACCOUNT_ID:function:FUNCTION_NAME ",
         "End": true
         }
        }
       }

**Note:** Don't worry about the errors related to the Lambda resource values. You'll update these values later in this tutorial.

5. Choose **Next**.

6. In the name field, enter **SupportStateMachine**.

7. In the **Permission** section, choose **Choose an existing role**.  

8. Choose **workflow-support** (the IAM role that you created).

![AWS Tracking Application](images/workflowuser.png)

9. Choose **Create state machine**. A message appears that states the state machine was successfully created.

## Create an IntelliJ project named LambdaFunctions

1. In the IntelliJ IDE, choose **File**, **New**, **Project**.

2. In the **New Project** dialog box, choose **Maven**, and then choose **Next**.

3. For **GroupId**, enter **LambdaNotifications**.

4. For **ArtifactId**, enter **LambdaNotifications**.

5. Choose **Next**.

6. Choose **Finish**.

## Add the POM dependencies to your project

At this point, you have a new project named **LambdaNotifications**.

![AWS Tracking Application](images/Projet.png)

The pom.xml file looks like the following.

      <?xml version="1.0" encoding="UTF-8"?>
      <project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
      <modelVersion>4.0.0</modelVersion>
     <groupId>LambdaNotifications</groupId>
     <artifactId>LambdaNotifications</artifactId>
     <version>1.0-SNAPSHOT</version>
     <packaging>jar</packaging>
     <name>java-basic-function</name>
     <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
     </properties>
     <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>software.amazon.awssdk</groupId>
                <artifactId>bom</artifactId>
                <version>2.15.66</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
     </dependencyManagement>
     <dependencies>
        <dependency>
            <groupId>com.amazonaws</groupId>
            <artifactId>aws-lambda-java-core</artifactId>
            <version>1.2.1</version>
        </dependency>
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>pinpointsmsvoice</artifactId>
        </dependency>
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>sns</artifactId>
        </dependency>
        <dependency>
            <groupId>javax.mail</groupId>
            <artifactId>javax.mail-api</artifactId>
            <version>1.5.5</version>
        </dependency>
        <dependency>
            <groupId>com.sun.mail</groupId>
            <artifactId>javax.mail</artifactId>
            <version>1.5.5</version>
        </dependency>
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>s3</artifactId>
        </dependency>
         <dependency>
            <groupId>org.jdom</groupId>
            <artifactId>jdom</artifactId>
            <version>2.0.2</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-log4j12</artifactId>
            <version>1.7.25</version>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>5.1.41</version>
        </dependency>
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>ses</artifactId>
        </dependency>
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.8.6</version>
        </dependency>
        <!-- https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-api -->
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-api</artifactId>
            <version>2.10.0</version>
        </dependency>
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-core</artifactId>
            <version>2.13.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-slf4j18-impl</artifactId>
            <version>2.13.3</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-api</artifactId>
            <version>5.6.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-engine</artifactId>
            <version>5.6.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.googlecode.json-simple</groupId>
            <artifactId>json-simple</artifactId>
            <version>1.1.1</version>
        </dependency>
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>dynamodb-enhanced</artifactId>
            <version>2.11.4-PREVIEW</version>
        </dependency>
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>dynamodb</artifactId>
            <version>2.10.41</version>
        </dependency>
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>ses</artifactId>
            <version>2.10.41</version>
        </dependency>
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>pinpoint</artifactId>
        </dependency>
        <dependency>
            <groupId>javax.mail</groupId>
            <artifactId>javax.mail-api</artifactId>
            <version>1.5.5</version>
        </dependency>
        <dependency>
            <groupId>com.sun.mail</groupId>
            <artifactId>javax.mail</artifactId>
            <version>1.5.5</version>
        </dependency>
     </dependencies>
     <build>
        <plugins>
            <plugin>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>2.22.2</version>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.2.2</version>
                <configuration>
                    <createDependencyReducedPom>false</createDependencyReducedPom>
                </configuration>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                </configuration>
            </plugin>
         </plugins>
        </build>
       </project>

## Create Lambda functions by using the AWS SDK for Java Lambda API

Use the Lambda runtime API to create the Java classes that define the Lamdba functions. In this example, there are two workflow steps that each correspond to a Java class. There are also extra classes that invoke the AWS services.  

The following figure shows the Java classes in the project. Notice that all Java classes are located in a package named **com.example.messages**.

![AWS Tracking Application](images/ProjectJava.png)

To create a Lambda function by using the Lambda runtime API, you implement **com.amazonaws.services.lambda.runtime.RequestHandler**. The application logic that's executed when the workflow step is invoked is located in the **handleRequest** method. The return value of this method is passed to the next step in a workflow.

Create these Java classes, which are described in the following sections:
+ **ConnectionHelper** - Used to connect to the Amazon RDS instance.  
+ **Handler** - Used as the first step in the workflow. This class queries data from the Amazon RDS instance. 
+ **HandlerVoiceNot** - Used as the second step in the workflow that sends out messages over multiple channels.
+ **RDSGetStudents** - Queries data from the student table using the JDBC API. 
+ **SendNotifications** - Uses the AWS SDK for Java V2 to invoke the SNS, Pinpoint, and SES services.
+ **Student** - A Java class that defines data members to store student data. 

### ConnectionHelper class

The following Java code represents the **ConnectionHelper** class.

     package com.example.messages;

     import java.sql.Connection;
     import java.sql.DriverManager;
     import java.sql.SQLException;

    public class ConnectionHelper {

     private String url;

     private static ConnectionHelper instance;

     private ConnectionHelper() {
        url = "jdbc:mysql://localhost:3306/mydb?useSSL=false";
    }

    public static Connection getConnection() throws SQLException {
        if (instance == null) {
            instance = new ConnectionHelper();
        }
        try {

            Class.forName("com.mysql.jdbc.Driver").newInstance();
            return DriverManager.getConnection(instance.url, "root","root1234");
        } catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.getStackTrace();
        }
        return null;
    }
    public static void close(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
       }
      }

**Note**: The URL value is **localhost:3306**. This value is modified after the RDS instance is created. The Lambda function uses this URL to communicate with the database. You must also ensure that you specify the user name and password for your RDS instance.

### Handler class

This Java code represents the **Handler** class. The class creates a Lamdba function that reads the passed in date value and queries the student table using the date value.  The **handleRequest** method returns XML document that specifies all of the absent students. This XML is passed to the second step in the workflow.

     package example;

     import com.amazonaws.services.lambda.runtime.Context;
     import com.amazonaws.services.lambda.runtime.RequestHandler;
     import com.amazonaws.services.lambda.runtime.LambdaLogger;
     import com.google.gson.Gson;
     import com.google.gson.GsonBuilder;
     import java.util.Map;

     // Handler value: example.Handler
     public class Handler implements RequestHandler<Map<String,String>, String>{

    @Override
    public String handleRequest(Map<String,String> event, Context context)
       {
        LambdaLogger logger = context.getLogger();
        String date = event.get("date");
        logger.log("DATE: " + date);

        // Get the XML from the S3 bucket
        RDSGetStudents students = new RDSGetStudents();
        String xml = students.getStudentsRDS(date);
        logger.log("XML: " + xml);
        return xml;
     }
    }

### HandlerVoiceNot class

The **HandlerVoiceNot** class is the second step in the workflow. It creates a **SendNotifications** object and invokes the following methods and passes the XML to each method: 

+ **handleTextMessage** 
+ **handleVoiceMessage**
+ **handleEmailMessage**

The following code represents the **HandlerVoiceNot** method. In this example, the XML that is passed to the Lambda function is stored in the **xml** variable. 

      package com.example.messages;

      import com.amazonaws.services.lambda.runtime.Context;
      import com.amazonaws.services.lambda.runtime.RequestHandler;
      import com.amazonaws.services.lambda.runtime.LambdaLogger;
      import org.jdom2.JDOMException;
      import javax.mail.MessagingException;
      import java.io.IOException;

      public class HandlerVoiceNot  implements RequestHandler<String, String>{

     @Override
     public String handleRequest(String event, Context context) {

        LambdaLogger logger = context.getLogger();
        String xml = event ;
        String num = "" ;
        SendNotifications sn = new SendNotifications();
        try {

            sn.handleTextMessage(xml);
            sn.handleVoiceMessage(xml);
            num =  sn.handleEmailMessage(xml);
           logger.log("NUMBER: " + num);
        } catch (JDOMException | IOException | MessagingException e) {
            e.printStackTrace();
        }
        return num;
       }
      }

### RDSGetStudents class

The **RDSGetStudents** class uses the JDBC API to query data from the Amazon RDS instance. The result set is stored in XML which is passed to the second step in the worlkflow . 

       package com.example.messages;

       import org.w3c.dom.Document;
       import org.w3c.dom.Element;
       import javax.xml.parsers.DocumentBuilder;
       import javax.xml.parsers.DocumentBuilderFactory;
       import javax.xml.parsers.ParserConfigurationException;
       import javax.xml.transform.Transformer;
       import javax.xml.transform.TransformerException;
       import javax.xml.transform.TransformerFactory;
       import javax.xml.transform.dom.DOMSource;
       import javax.xml.transform.stream.StreamResult;
       import java.io.*;
       import java.sql.*;
       import java.util.ArrayList;
       import java.util.List;

       public class RDSGetStudents {

        public String getStudentsRDS(String date ) {
        Connection c = null;
        String query = "";

        try {

            c = ConnectionHelper.getConnection();
            ResultSet rs = null;
         
            // Use prepared statements
            PreparedStatement pstmt = null;
            PreparedStatement ps = null;

            // Specify the SQL Statement to query data
            query = "Select first, phone, mobile, email FROM students where date = '" +date +"'";
            pstmt = c.prepareStatement(query);
            rs = pstmt.executeQuery();

            List<Student> studentList = new ArrayList<>();
            while (rs.next()) {

                Student student = new Student();

                String name = rs.getString(1);
                String phone = rs.getString(2);
                String mobile = rs.getString(3);
                String email = rs.getString(4);

                student.setFirstName(name);
                student.setMobileNumber(mobile);
                student.setPhoneNunber(phone);
                student.setEmail(email);

                // Push the Student object to the list.
                studentList.add(student);
            }

                return convertToString(toXml(studentList));

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionHelper.close(c);
        }
        return null;
    }

    private String convertToString(Document xml) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(xml);
            transformer.transform(source, result);
            return result.getWriter().toString();

        } catch(TransformerException ex) {
            ex.printStackTrace();
        }
        return null;
    }


    // Convert Work item data retrieved from MySQL
    private Document toXml(List<Student> itemList) {

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            // Start building the XML
            Element root = doc.createElement( "Students" );
            doc.appendChild( root );

            // Get the elements from the collection
            int studentCount = itemList.size();

            // Iterate through the collection
            for ( int index=0; index < studentCount; index++) {

                // Get the WorkItem object from the collection
                Student myStudent = itemList.get(index);

                Element item = doc.createElement( "Student" );
                root.appendChild( item );

                // Set Name
                Element name = doc.createElement( "Name" );
                name.appendChild( doc.createTextNode(myStudent.getFirstName()) );
                item.appendChild( name );

                // Set Mobile
                Element mobile = doc.createElement( "Mobile" );
                mobile.appendChild( doc.createTextNode(myStudent.getMobileNumber()) );
                item.appendChild( mobile );

                // Set Phone
                Element phone = doc.createElement( "Phone" );
                phone.appendChild( doc.createTextNode(myStudent.getPhoneNunber() ) );
                item.appendChild( phone );

                // Set Email
                Element email = doc.createElement( "Email" );
                email.appendChild( doc.createTextNode(myStudent.getEmail() ) );
                item.appendChild( email );

            }

         return doc;
        } catch(ParserConfigurationException e) {
            e.printStackTrace();
        }
        return null;
       }
      }

### PersistCase class

The following class uses the Amazon DynamoDB API to store the data in a table. For more information, see [DynamoDB examples using the AWS SDK for Java](https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/examples-dynamodb.html).

       package example;

       import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
       import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
       import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
       import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;
       import software.amazon.awssdk.regions.Region;
       import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
       import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
       import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
       import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
       import java.time.Instant;
       import java.time.LocalDate;
       import java.time.LocalDateTime;
       import java.time.ZoneOffset;

       /*
        Prior to running this code example, create a table named Case with a PK named id
       */

      public class PersistCase {

      // Puts an item into a DynamoDB table
      public void putRecord(String caseId, String employeeName, String email) {

        // Create a DynamoDbClient object
        Region region = Region.US_WEST_2;
        DynamoDbClient ddb = DynamoDbClient.builder()
                .region(region)
                .build();

        // Create a DynamoDbEnhancedClient and use the DynamoDbClient object
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(ddb)
                .build();

        try {
            // Create a DynamoDbTable object
            DynamoDbTable<Case> caseTable = enhancedClient.table("Case", TableSchema.fromBean(Case.class));

            // Create an Instant object
            LocalDate localDate = LocalDate.parse("2020-04-07");
            LocalDateTime localDateTime = localDate.atStartOfDay();
            Instant instant = localDateTime.toInstant(ZoneOffset.UTC);

            // Populate the table
            Case caseRecord = new Case();
            caseRecord.setName(employeeName);
            caseRecord.setId(caseId);
            caseRecord.setEmail(email);
            caseRecord.setRegistrationDate(instant) ;

            // Put the case data into a DynamoDB table
            caseTable.putItem(caseRecord);

        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        System.out.println("done");
    }


    // Create the Case table to track open cases created in the workflow
    @DynamoDbBean
    public static class Case {

        private String id;
        private String name;
        private String email;
        private Instant regDate;

        @DynamoDbPartitionKey
        public String getId() {
            return this.id;
        };

        public void setId(String id) {

            this.id = id;
        }

        @DynamoDbSortKey
        public String getName() {
            return this.name;

        }

        public void setName(String name) {

            this.name = name;
        }

        public String getEmail() {
            return this.email;
        }

        public void setEmail(String email) {

            this.email = email;
        }

        public Instant getRegistrationDate() {
            return regDate;
        }
        public void setRegistrationDate(Instant registrationDate) {

            this.regDate = registrationDate;
        }
       }
      }

### SendMessage class

The following Java class represents the **SendMessage** class. This class uses the Amazon SES API to send an email message to the employee. An email address that you send an email message to must be verified. For information, see [Verifying an email address](https://docs.aws.amazon.com/ses/latest/DeveloperGuide//verify-email-addresses-procedure.html).

       package example;

       import software.amazon.awssdk.regions.Region;
       import software.amazon.awssdk.services.ses.SesClient;
       import javax.mail.Message;
       import javax.mail.MessagingException;
       import javax.mail.Session;
       import javax.mail.internet.AddressException;
       import javax.mail.internet.InternetAddress;
       import javax.mail.internet.MimeMessage;
       import javax.mail.internet.MimeMultipart;
       import javax.mail.internet.MimeBodyPart;
       import java.io.ByteArrayOutputStream;
       import java.io.IOException;
       import java.nio.ByteBuffer;
       import java.util.Properties;
       import software.amazon.awssdk.core.SdkBytes;
       import software.amazon.awssdk.services.ses.model.SendRawEmailRequest;
       import software.amazon.awssdk.services.ses.model.RawMessage;
       import software.amazon.awssdk.services.ses.model.SesException;

       public class SendMessage {

        public void sendMessage(String email) throws IOException {

        //Sender
        String sender = "SPECIFY an email address" ; // REPLACE WITH AN EMAIL ADDRESSS

        String subject = "New Case";

        // The email body for recipients with non-HTML email clients.
        String bodyText = "Hello,\r\n" + "You are assigned a new case";

        // The HTML body of the email.
        String bodyHTML = "<html>" + "<head></head>" + "<body>" + "<h1>Hello!</h1>"
                + "<p>Please check the database for new ticket assigned to you.</p>" + "</body>" + "</html>";

        Region region = Region.US_WEST_2;
        SesClient client = SesClient.builder()
                .region(region)
                .build();

        try {
            send(client, sender,email, subject,bodyText,bodyHTML);

        } catch (IOException | MessagingException e) {
            e.getStackTrace();
        }
      }

    public static void send(SesClient client,
                            String sender,
                            String recipient,
                            String subject,
                            String bodyText,
                            String bodyHTML
    ) throws AddressException, MessagingException, IOException {

        Session session = Session.getDefaultInstance(new Properties());

        // Create a new MimeMessage object.
        MimeMessage message = new MimeMessage(session);

        // Add subject, from and to lines.
        message.setSubject(subject, "UTF-8");
        message.setFrom(new InternetAddress(sender));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));

        // Create a multipart/alternative child container.
        MimeMultipart msgBody = new MimeMultipart("alternative");

        // Create a wrapper for the HTML and text parts.
        MimeBodyPart wrap = new MimeBodyPart();

        // Define the text part.
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(bodyText, "text/plain; charset=UTF-8");

        // Define the HTML part.
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(bodyHTML, "text/html; charset=UTF-8");

        // Add the text and HTML parts to the child container.
        msgBody.addBodyPart(textPart);
        msgBody.addBodyPart(htmlPart);

        // Add the child container to the wrapper object.
        wrap.setContent(msgBody);

        // Create a multipart/mixed parent container.
        MimeMultipart msg = new MimeMultipart("mixed");

        // Add the parent container to the message.
        message.setContent(msg);

        // Add the multipart/alternative part to the message.
        msg.addBodyPart(wrap);

        try {
            System.out.println("Attempting to send an email through Amazon SES " + "using the AWS SDK for Java...");

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            message.writeTo(outputStream);

            ByteBuffer buf = ByteBuffer.wrap(outputStream.toByteArray());

            byte[] arr = new byte[buf.remaining()];
            buf.get(arr);

            SdkBytes data = SdkBytes.fromByteArray(arr);

            RawMessage rawMessage = RawMessage.builder()
                    .data(data)
                    .build();

            SendRawEmailRequest rawEmailRequest = SendRawEmailRequest.builder()
                    .rawMessage(rawMessage)
                    .build();

            client.sendRawEmail(rawEmailRequest);

          } catch (SesException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
          }
         }
       }
## Package the project that contains the Lambda functions

Package up the project into a .jar (JAR) file that you can deploy as a Lambda function by using the following Maven command.

    mvn package

The JAR file is located in the **target** folder (which is a child folder of the project folder).

![AWS Tracking Application](images/lambda10.png)

## Deploy the Lambda functions

1. Open the Lambda console at https://us-west-2.console.aws.amazon.com/lambda/home.

2. Choose **Create Function**.

3. Choose **Author from scratch**.

4. In the **Basic** information section, enter **TicStep1** as the name.

5. In the **Runtime**, choose **Java 8**.

6. Choose **Use an existing role**, and then choose **lambda-support** (the IAM role that you created).

![AWS Tracking Application](images/lambda20.png)

7. Choose **Create function**.

8. For **Code entry type**, choose **Upload a .zip or .jar file**.

9. Choose **Upload**, and then browse to the JAR file that you created.  

10. For **Handler**, enter the fully qualified name of the function, for example, **example.Handler::handleRequest** (**example.Handler** specifies the package and class followed by :: and method name).

![AWS Tracking Application](images/lambda11.png)

11. Choose **Save.**

12. Repeat this procedure for the **Handler2** and **Handler3** classes. Name the corresponding Lambda functions **TicStep2** and **TicStep3**. When you finish, you will have three Lambda functions that you can reference in the Amazon States Language document.  

## Add the Lambda functions to workflows

Open the Lambda console. Notice that you can view the Lambda Amazon Resource Name (ARN) value in the upper-right corner.

![AWS Tracking Application](images/lambda12A.png)

Copy the value and then paste it into step 1 of the Amazon States Language document, located in the Step Functions console.

![AWS Tracking Application](images/lambda13A.png)

Update the Resource for the **Assign Case** and **Send Email** steps. This is how you hook in Lambda functions created by using the AWS SDK for Java into a workflow created by using Step Functions.

## Execute your workflow by using the Step Functions console

You can invoke the workflow on the Step Functions console.  An execution receives JSON input. For this example, you can pass the following JSON data to the workflow.  

     {
	"inputCaseID": "001"
     }


#### To execute your workflow

1. On the Step Functions console, choose **Start execution**.

2. In the **Input** section, pass the JSON data. View the workflow. As each step is completed, it turns green.

![AWS Tracking Application](images/lambda1.png)

If the step turns red, an error occurred. You can click the step and view the logs that are accessible from the right side.

![AWS Tracking Application](images/lambda14.png)

When the workflow is finished, you can view the data in the DynamoDB table.

![AWS Tracking Application](images/lambda15.png)

### Next steps
Congratulations, you have created an AWS serverless workflow by using the AWS SDK for Java. As stated at the beginning of this tutorial, be sure to terminate all of the resources you create while going through this tutorial to ensure that you’re no longer charged.

For more AWS multiservice examples, see
[usecases](https://github.com/awsdocs/aws-doc-sdk-examples/tree/master/javav2/usecases).

