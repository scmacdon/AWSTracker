# Creating an ETL workflow by using AWS Step Functions and the AWS SDK for Java

You can create an Extract, Transform, and Load (ETL) workflow by using AWS Step Functions and the AWS SDK for Java V2. An ETL operation retrieves data from a given data source, applies various transformations to the data, and then writes the results to a specified location where the data is stored. In this AWS tutorial, population data located in a Microsoft Excel spreadsheet and stored in an Amazon Simple Storage Service (Amazon S3) bucket is retrieved, transformed to another formation. and stored in an Amazon DyanmoDB table. 

![AWS Tracking Application](images/overview.png)

The following illustration shows the population data located in a Microsoft Excel spreadsheet. 

![AWS Tracking Application](images/popDate.png)

AFter the workflow successfully runs the ETL job, the population data is stored in an Amazon DynamoDB table, as shown in this illustation. 

![AWS Tracking Application](images/DynTable.png)

The AWS Services used in this AWS tutorial are:

 - Amazon S3 Service
 - Amazon DynamoDB
 - AWS Step Functions
 
Each workflow step is implemented by using an AWS Lambda function. Lambda is a compute service that enables you to run code without provisioning or managing servers.

**Note**: You can create Lambda functions in various programming languages. For this tutorial, Lambda functions are implemented by using the Lambda Java API. For more information about Lambda, see  [What is AWS Lambda](https://docs.aws.amazon.com/lambda/latest/dg/welcome.html). 

**Cost to complete:** The AWS services included in this document are included in the [AWS Free Tier](https://aws.amazon.com/free/?all-free-tier.sort-by=item.additionalFields.SortRank&all-free-tier.sort-order=asc).

**Note:** Be sure to terminate all of the resources you create while going through this tutorial to ensure that you’re not charged.

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

To complete the tutorial, you need the following:

+ An AWS account
+ A Java IDE (this tutorial uses the IntelliJ IDE)
+ Java JDK 1.8
+ Maven 3.6 or later
+ An Amazon Aurora table named **jobs** that contains the fields described in this tutorial. For information about creating an Amazon Aurora table, see [Getting started with Amazon Aurora](https://docs.aws.amazon.com/AmazonRDS/latest/AuroraUserGuide/CHAP_GettingStartedAurora.html). Set Public access when you create the Aurora database. 

## Understand the workflow

The following figure shows the workflow you'll create with this tutorial, which performs the ETL operation.  

![AWS Tracking Application](images/workflow.png)

The following describes each step in the workflow:
+ **Start** - Initiates the workflow.
+ **Get Excel Data** – Retrieves an Excel file from an Amazon S3 bucket by using the Amazon S3 Java API. This step dynamically creates XML that contains the population data and passes the XML to the next step. This example shows how a Lambda function can retrieve data from an Amazon S3 bucket and transform the data.
+ **Store Data** – Parses the XML that contains the population data. For each item in the XML, this step adds a record to an Amazon DynamoDB table by using the Amazon DynamoDB Java API. .  
+ **End** - Stops the workflow.

## Create an IAM role that's used to execute Lambda functions

Create the following two IAM roles:

+ **lambda-support** - Used to invoke Lamdba functions.
+ **workflow-support** - Used to enable Step Functions to invoke the workflow.

This tutorial uses the Amazon S3 and Amazon DynamoDB. The **lambda-support** role has to have policies that enable it to invoke these Amazon services from a Lambda function.

#### To create an IAM role

1. Open the AWS Management Console. When the page loads, enter **IAM** in the search box, and then choose **IAM** to open the IAM console.

2. In the navigation pane, choose **Roles**, and on the **Roles** page, choose **Create Role**.

3. Choose **AWS service**, and then choose **Lambda**.

![AWS Tracking Application](images/Lam1.png)

4. Choose **Permissions**.

5. Search for **AWSLambdaBasicExecutionRole**.

6. Choose **Next Tags**.

7. Choose **Review**.

8. Name the role **lambda-support**.

![AWS Tracking Application](images/Lam2.png)

9. Choose **Create role**.

10. Choose **lambda-support** to view the overview page.

11. Choose **Attach Policies**.

12. Search for **AmazonS3FullAccess**, and then choose **Attach policy**.

13. Search for **AmazonDynamoDBFullAccess**, and then choose **Attach policy**. When you're done, you can see the permissions.

**Note**: Repeat this process to create **workflow-support**. For step three, instead of choosing **Lambda**, choose **Step Functions**. You don't need to perform steps 11-13.  
## Create a serverless workflow by using AWS Step functions

To define a workflow that performs an ETL operation by using AWS Step Functions, you create an Amazon States Language (JSON-based) document to define your state machine. An Amazon States Language document describes each step. After you define the document, AWS Step Functions provides a visual representation of the workflow. The following figure shows a visual representation of the workflow.

![AWS Tracking Application](images/workflow.png)

Workflows can pass data between steps. For example, the **Get Excel Data** dynamically creates XML and passes the XML to the **Store Data** step. 

**Note**: Later in this tutorial, you'll create application logic in the Lambda function to read data from the Amazon S3 bucket.  

#### To create a workflow

1. Open the Step Functions console at https://us-west-2.console.aws.amazon.com/states/home.

2. Choose **Create State Machine**.

3. Select **Write your workflow in code**. In the **Type** area, choose **Standard**.

![AWS Tracking Application](images/workflow1.png)

4. Specify the Amazon States Language document by entering the following code.

       {
        "Comment": "An AWS Step Functions state machine that performs an ETL job.",
        "StartAt": "Get Excel Data",
        "States": {
           "Get Excel Data": {
           "Type": "Task",
           "Resource": "arn:aws:lambda:REGION:ACCOUNT_ID:function:FUNCTION_NAME",
           "Next": "Store Data"
          },
           "Store Data": {
           "Type": "Task",
           "Resource": "arn:aws:lambda:REGION:ACCOUNT_ID:function:FUNCTION_NAME",
           "End": true
           }
          }
        }

**Note:** Don't worry about the errors related to the Lambda resource values. You update these values later in this tutorial.

5. Choose **Next**.

6. In the name field, enter **ETLStateMachine**.

7. In the **Permission** section, choose **Choose an existing role**.  

8. Choose **workflow-support** (the IAM role that you created).

9. Choose **Create state machine**. A message appears that states the state machine was successfully created.


## Create an IntelliJ project named ETL_Lambda

Create an IntelliJ project that is used to create the web application.

1. In the IntelliJ IDE, choose **File**, **New**, **Project**.

2. In the New Project dialog box, choose **Maven**.

3. Choose **Next**.

4. In **GroupId**, enter **org.example**.

5. In **ArtifactId**, enter **ETL_Lambda**.

6. Choose **Next**.

7. Choose **Finish**.

## Add the POM dependencies to your project

At this point, you have a new project named **ETL_Lambda**. Ensure that the pom.xml file resembles the following code.

     <?xml version="1.0" encoding="UTF-8"?>
     <project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>org.example</groupId>
    <artifactId>ETL_Lambda</artifactId>
    <version>1.0-SNAPSHOT</version>
    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>software.amazon.awssdk</groupId>
                <artifactId>bom</artifactId>
                <version>2.11.11</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-api</artifactId>
            <version>5.4.2</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-engine</artifactId>
            <version>5.4.2</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.platform</groupId>
            <artifactId>junit-platform-commons</artifactId>
            <version>1.4.0</version>
        </dependency>
        <dependency>
            <groupId>org.junit.platform</groupId>
            <artifactId>junit-platform-launcher</artifactId>
            <version>1.4.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>dynamodb</artifactId>
            <version>2.5.10</version>
        </dependency>
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>dynamodb-enhanced</artifactId>
            <version>2.11.4-PREVIEW</version>
        </dependency>
        <dependency>
            <groupId>org.jdom</groupId>
            <artifactId>jdom</artifactId>
            <version>2.0.2</version>
        </dependency>
        <dependency>
            <groupId>com.amazonaws</groupId>
            <artifactId>aws-lambda-java-core</artifactId>
            <version>1.2.1</version>
        </dependency>
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>s3</artifactId>
        </dependency>
        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <version>3.8.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>protocol-core</artifactId>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.5</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>net.sourceforge.jexcelapi</groupId>
            <artifactId>jxl</artifactId>
            <version>2.6.10</version>
        </dependency>
        <dependency>
            <groupId>commons-io</groupId>
            <artifactId>commons-io</artifactId>
            <version>2.6</version>
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
     
 ## Create the Java classes
 
 Create a Java package in the main/java folder named **com.etl.example**. This Java classes go into this package. 
 
 ![AWS Lex](images/Java.png)
 
 Create these Java classes:

+ **DocumentHandler** - Used as the first step in the workflow that retrieves the Microsoft Excel document and dynamically creates XML that contains the data.
+ **DynamoDBService** - Uses the Amazon DynamoDB Java V2 API to store population data into a DynamoDB table.  
+ **ExcelService** - Uses the **jxl.Workbook** (not an AWS Java API) to read data from a Microsoft Excel spreadsheet. 
+ **HandlerStoreData** - Used as the second step in the workflow. 
+ **PopData** - Used as a model that stores population data. 
+ **Population** - Used as the data mapping class for the Amazon DynamoDB Java API (V2) Enchanced Client. For more information about the Enhanced Client, see [Map items in DynamoDB tables](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-dynamodb-enhanced.html).
+ **S3Service** - Uses the Amazon S3 Java API (V2) to read the Microsoft Excel document and returns a byte array.  

### DocumentHandler class

The following Java code represents the **DocumentHandler** class.

     package com.etl.example;

    import com.amazonaws.services.lambda.runtime.Context;
    import com.amazonaws.services.lambda.runtime.LambdaLogger;
    import jxl.read.biff.BiffException;
    import java.io.IOException;
    import java.util.Map;

    public class DocumentHandler {

     public String handleRequest(Map<String,String> event, Context context) throws IOException, BiffException {

        LambdaLogger logger = context.getLogger();
        logger.log("Getting excel doc from the Amazon S3 bucket");

        // Get the Amazon S3 bucket name and MS Excel file name
        String bucketName = event.get("bucketname");
        String object = event.get("objectname");

        // Get the XML that contains the Pop data
        ExcelService excel = new ExcelService();
        String xml = excel.getData(bucketName, object);
        return xml;
     }
    }

### DynamoDBService class

The following Java code represents the **DynamoDBService** class. This class uses the Amazon DynamoDB Java API (V2) to populate the **Country** table. 

    package com.etl.example;

    import org.jdom2.Document;
    import org.jdom2.JDOMException;
    import org.jdom2.input.SAXBuilder;
    import org.xml.sax.InputSource;
    import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
    import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
    import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
    import software.amazon.awssdk.regions.Region;
    import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
    import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
    import java.util.List;
    import java.io.IOException;
    import java.io.StringReader;

    public class DynamoDBService {

     int recNum = 1;

     private DynamoDbClient getClient() {

        // Create a DynamoDbClient object.
        Region region = Region.US_EAST_1;
        DynamoDbClient ddb = DynamoDbClient.builder()
                .region(region)
                .build();

        return ddb;
     }

     public void injectETLData(String myDom)  throws JDOMException, IOException {

        SAXBuilder builder = new SAXBuilder();
        Document jdomDocument = builder.build(new InputSource(new StringReader(myDom)));
        org.jdom2.Element root = ((org.jdom2.Document) jdomDocument).getRootElement();
        PopData pop = new PopData();
        List<org.jdom2.Element> items = root.getChildren("Item");

        for (org.jdom2.Element element : items) {

            pop.setName( element.getChildText("Name"));
            pop.setCode(element.getChildText("Code")); element.getChildText("Code");
            pop.set2010(element.getChildText("Date2010"));
            pop.set2011(element.getChildText("Date2011"));
            pop.set2012(element.getChildText("Date2012"));
            pop.set2013(element.getChildText("Date2013"));
            pop.set2014(element.getChildText("Date2014"));
            pop.set2015(element.getChildText("Date2015"));
            pop.set2016(element.getChildText("Date2016"));
            pop.set2017(element.getChildText("Date2017"));
            pop.set2018(element.getChildText("Date2018"));
            pop.set2019(element.getChildText("Date2019"));
            setItem(pop) ;
        }

        int y = 0;

    }

    public void setItem(PopData pop) {

        // Create a DynamoDbEnhancedClient.
        DynamoDbClient ddb = getClient();

        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(ddb)
                .build();

        try {

            // Create a DynamoDbTable object.
            DynamoDbTable<Population> workTable = enhancedClient.table("Country", TableSchema.fromBean(Population.class));

             // Populate the table.
            Population record = new Population();
            String name = pop.getName();
            String code = pop.getCode();

            record.setId(name);
            record.setCode(code);
            record.set2010(pop.get2010());
            record.set2011(pop.get2011());
            record.set2012(pop.get2012());
            record.set2013(pop.get2013());
            record.set2014(pop.get2014());
            record.set2015(pop.get2015());
            record.set2016(pop.get2016());
            record.set2017(pop.get2017());
            record.set2018(pop.get2018());
            record.set2019(pop.get2019());

            // Put the customer data into a DynamoDB table.
            workTable.putItem(record);
            System.out.println("Added record "+recNum);
            recNum ++;

        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
      }
     }


### ExcelService class

The following Java code represents the **ExcelService** class that uses the **jxl.Workbook** Java API.

    package com.etl.example;

    import jxl.Cell;
    import jxl.Sheet;
    import jxl.Workbook;
    import java.io.ByteArrayInputStream;
    import java.io.InputStream;
    import java.util.ArrayList;
    import java.util.Comparator;
    import java.util.List;
    import jxl.read.biff.BiffException;
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

    public class ExcelService {

    public String getData(String bucketName, String object) throws IOException, BiffException {

    // Get the Excel speadsheet from the Amazon S3 bucket
    S3Service s3Service = new S3Service();
    byte[] data = s3Service.getObjectBytes(bucketName, object);
    InputStream inputStrean = new ByteArrayInputStream(data);

    List<PopData> myList = new ArrayList() ;
    System.out.println("Retrieving data from the Excel Spreadsheet");
    Workbook wb = Workbook.getWorkbook(inputStrean);
    Sheet sheet = wb.getSheet(0);

    try{

        // Lets read the data from the excel spreadsheet
        Sheet s=wb.getSheet(0);
        int b = s.getColumns();
        System.out.println("The No. of Columns in the Sheet are = " + b);
        int a = s.getRows();
        System.out.println("The No. of Rows in the sheet are = " +a);

        PopData popData = null;

        // Loop through the rows in the spreadsheet
        for (int zz = 0 ; zz <a; zz++) {
        // Get the first cell.
        System.out.println(zz);

        Cell[] row = sheet.getRow(zz);
        //Cell cell = row.getCell(0);
        if (zz ==0)
            System.out.println("Not 1st row");
        else {
            popData = new PopData();

            for (Cell cell : row) {
                // Column header names.
                //System.out.println(cell.toString());

                int colIndex =  cell.getColumn();
                String val = cell.getContents();

                switch(colIndex) {
                    case 0:
                        popData.setName(val);
                        break;

                    case 1:
                        // code block
                        popData.setCode(val);
                        break;

                    case 2:
                        // code block
                        popData.set2010(val);
                        break;

                    case 3:
                        // code block
                        popData.set2011(val);
                        break;

                    case 4:
                        // code block
                        popData.set2012(val);
                        break;

                    case 5:
                        // code block
                        popData.set2013(val);
                        break;

                    case 6:
                        // code block
                        popData.set2014(val);
                        break;

                    case 7:
                        // code block
                        popData.set2015(val);
                        break;

                    case 8:
                        // code block
                        popData.set2016(val);
                        break;

                    case 9:
                        // code block
                        popData.set2017(val);
                        break;

                    case 10:
                        // code block
                        popData.set2018(val);
                        break;

                    default: {
                        // code block
                        popData.set2019(val);
                        myList.add(popData);
                    }
                }
            }
          }
        }

        myList.sort(Comparator.comparing(PopData::getName));
        String transformXML  = convertToString(toXml(myList));
        return transformXML;

      }catch (Exception e) {
        e.printStackTrace();
      }

     return "";
     }

    // Convert population data into XML.
    private static Document toXml(List<PopData> itemList) {

        try {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        // Start building the XML.
        Element root = doc.createElement( "Items" );
        doc.appendChild( root );

        // Get the elements from the collection.
        int custCount = itemList.size();

        // Iterate through the collection.
        for ( int index=0; index < custCount; index++) {

        // Get the WorkItem object from the collection.
        PopData myItem = itemList.get(index);

        Element item = doc.createElement( "Item" );
        root.appendChild( item );

        // Set Id.
        Element id = doc.createElement( "Name" );
        id.appendChild( doc.createTextNode(myItem.getName() ) );
        item.appendChild( id );

        // Set Name.
        Element name = doc.createElement( "Code" );
        name.appendChild( doc.createTextNode(myItem.getCode()) );
        item.appendChild( name );

        // Set 2010.
        Element ob2010 = doc.createElement( "Date2010" );
        ob2010.appendChild( doc.createTextNode(myItem.get2010() ) );
        item.appendChild( ob2010 );

        // Set 2011.
        Element ob2011 = doc.createElement( "Date2011" );
        ob2011.appendChild( doc.createTextNode(myItem.get2011()) );
        item.appendChild( ob2011 );

        // Set 2012.
        Element ob2012 = doc.createElement( "Date2012" );
        ob2012.appendChild( doc.createTextNode(myItem.get2012() ) );
        item.appendChild( ob2012 );

        // Set 2013.
        Element ob2013 = doc.createElement( "Date2013" );
        ob2013.appendChild( doc.createTextNode(myItem.get2013()) );
        item.appendChild( ob2013 );

        // Set 2014.
        Element ob2014 = doc.createElement( "Date2014" );
        ob2014.appendChild( doc.createTextNode(myItem.get2014()) );
        item.appendChild( ob2014 );

        // Set 2015.
        Element ob2015 = doc.createElement( "Date2015" );
        ob2015.appendChild( doc.createTextNode(myItem.get2015()) );
        item.appendChild( ob2015 );

        // Set 2016.
        Element ob2016 = doc.createElement( "Date2016" );
        ob2016.appendChild( doc.createTextNode(myItem.get2016()) );
        item.appendChild( ob2016 );

        // Set 2017.
        Element ob2017 = doc.createElement( "Date2075" );
        ob2017.appendChild( doc.createTextNode(myItem.get2017()) );
        item.appendChild( ob2017 );

        // Set 2018.
        Element ob2018 = doc.createElement( "Date2018" );
        ob2018.appendChild( doc.createTextNode(myItem.get2018()) );
        item.appendChild( ob2018 );

        // Set 2015.
        Element ob2019 = doc.createElement( "Date2019" );
        ob2019.appendChild( doc.createTextNode(myItem.get2019()) );
        item.appendChild( ob2019 );
        }

        return doc;
        } catch(ParserConfigurationException e) {
        e.printStackTrace();
        }
        return null;
        }

      private static String convertToString(Document xml) {
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
  }

### HandlerStoreData class

The following Java code represents the **HandlerStoreData** class. This class represents the second step in the workflow. 

    package com.etl.example;

    import com.amazonaws.services.lambda.runtime.Context;
    import com.amazonaws.services.lambda.runtime.RequestHandler;
    import com.amazonaws.services.lambda.runtime.LambdaLogger;
    import org.jdom2.JDOMException;
    import java.io.IOException;

    public class HandlerStoreData  implements RequestHandler<String, String>{

     @Override
     public String handleRequest(String event, Context context) {

        LambdaLogger logger = context.getLogger();
        String xml = event ;
        DynamoDBService storeData = new DynamoDBService();
        try {

            storeData.injectETLData(xml);
            logger.log("data stored:");
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
        return "Data is stored successfully";
      }
     }

### PopData class

The following Java code represents the **PopData** class.

    package com.etl.example;

    //Stores the data model for this use case.
    public class PopData {

    private String countryName;
    private String countryCode;
    private String pop2010;
    private String pop2011;
    private String pop2012;
    private String pop2013;
    private String pop2014;
    private String pop2015;
    private String pop2016;
    private String pop2017;
    private String pop2018;
    private String pop2019;


    public void set2019(String num) {
        this.pop2019 = num;
    }

    public String get2019() {
        return this.pop2019;
    }

    public void set2018(String num) {
        this.pop2018 = num;
    }

    public String get2018() {
        return this.pop2018;
    }


    public void set2017(String num) {
        this.pop2017 = num;
    }

    public String get2017() {
        return this.pop2017;
    }


    public void set2016(String num) {
        this.pop2016 = num;
    }

    public String get2016() {
        return this.pop2016;
    }

    public void set2015(String num) {
        this.pop2015 = num;
    }

    public String get2015() {
        return this.pop2015;
    }


    public void set2014(String num) {
        this.pop2014 = num;
    }

    public String get2014() {
        return this.pop2014;
    }


    public void set2013(String num) {
        this.pop2013 = num;
    }

    public String get2013() {
        return this.pop2013;
    }


    public void set2012(String num) {
        this.pop2012 = num;
    }

    public String get2012() {
        return this.pop2012;
    }

    public void set2011(String num) {
        this.pop2011 = num;
    }

    public String get2011() {
        return this.pop2011;
    }


    public void set2010(String num) {
        this.pop2010 = num;
    }

    public String get2010() {
        return this.pop2010;
    }

    public void setCode(String code) {
        this.countryCode = code;
    }

    public String getCode() {
        return this.countryCode;
    }

    public void setName(String name) {
        this.countryName = name;
    }

    public String getName() {
        return this.countryName ;
    }
   }

### Population class

The following Java code represents the **Population** class. The class is used for data mapping for the Amazon DynamoDB Java API Enhanced Client. 

     package com.etl.example;

     import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
     import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

     @DynamoDbBean
     public class Population {

     public String id;
     public String code;
     public String pop2010;
     public String pop2011;
     public String pop2012;
     public String pop2013;
     public String pop2014;
     public String pop2015;
     public String pop2016;
     public String pop2017;
     public String pop2018;
     public String pop2019;


     public void setId(String name) {
        this.id = name;
     }

     @DynamoDbPartitionKey
     public String getId() {
        return this.id ;
    }


    public void set2019(String num) {
        this.pop2019 = num;
    }

    public String get2019() {
        return this.pop2019;
    }

    public void set2018(String num) {
        this.pop2018 = num;
    }

    public String get2018() {
        return this.pop2018;
    }


    public void set2017(String num) {
        this.pop2017 = num;
    }

    public String get2017() {
        return this.pop2017;
    }


    public void set2016(String num) {
        this.pop2016 = num;
    }

    public String get2016() {
        return this.pop2016;
    }

    public void set2015(String num) {
        this.pop2015 = num;
    }

    public String get2015() {
        return this.pop2015;
    }


    public void set2014(String num) {
        this.pop2014 = num;
    }

    public String get2014() {
        return this.pop2014;
    }


    public void set2013(String num) {
        this.pop2013 = num;
    }

    public String get2013() {
        return this.pop2013;
    }


    public void set2012(String num) {
        this.pop2012 = num;
    }

    public String get2012() {
        return this.pop2012;
    }

    public void set2011(String num) {
        this.pop2011 = num;
    }

    public String get2011() {
        return this.pop2011;
    }


    public void set2010(String num) {
        this.pop2010 = num;
    }

    public String get2010() {
        return this.pop2010;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }

}
    
### S3Service class

The following Java code represents the **S3Service** class.

    package com.etl.example;

    import software.amazon.awssdk.core.ResponseBytes;
    import software.amazon.awssdk.regions.Region;
    import software.amazon.awssdk.services.s3.S3Client;
    import software.amazon.awssdk.services.s3.model.GetObjectRequest;
    import software.amazon.awssdk.services.s3.model.GetObjectResponse;
    import software.amazon.awssdk.services.s3.model.S3Exception;

    public class S3Service {

    private S3Client getClient() {

        Region region = Region.US_EAST_1;
        S3Client s3 = S3Client.builder()
                .region(region)
                .build();

        return s3;
      }

     public byte[] getObjectBytes (String bucketName, String keyName) {

        try {
            S3Client s3 = getClient();
            GetObjectRequest objectRequest = GetObjectRequest
                    .builder()
                    .key(keyName)
                    .bucket(bucketName)
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3.getObjectAsBytes(objectRequest);
            byte[] data = objectBytes.asByteArray();
            return data;

         } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        return null;
      }
    }


## Create the HTML file

At this point, you have created all of the Java files required for this example application. Now create HTML files that are required for the application's view. Under the resource folder, create a **templates** folder, and then create the following HTML files:

+ index.html
+ layout.html
+ post.html
+ add.html
+ login.html

### index.html
The **index.html** file is the application's home view. 

    <!DOCTYPE html>
    <html xmlns:th="http://www.thymeleaf.org" xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity3">

    <head>
    <meta charset="utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />

    <link rel="stylesheet" th:href="|https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css|"/>
    <link rel="stylesheet" href="../public/css/styles.css" th:href="@{/css/styles.css}" />
    <link rel="icon" href="../public/img/favicon.ico" th:href="@{/img/favicon.ico}" />

    <title>AWS Job Posting Example</title>
    </head>

    <body>
    <header th:replace="layout :: site-header"/>
    <div class="container">

    <h3>Welcome <span sec:authentication="principal.username">User</span> to the Amazon Redshift Job Posting example app</h3>
    <p>Now is: <b th:text="${execInfo.now.time}"></b></p>

    <h2>Amazon Redshift Job Posting Example</h2>

    <p>The Amazon Redshift Job Posting Example application uses multiple AWS Services and the Java V2 API. Perform these steps:<p>

    <ol>
        <li>Enter work items into the system by choosing the <i>Add Posts</i> menu item. Fill in the form and then choose <i>Create Item</i>.</li>
        <li>The sample application stores the data by using the Amazon Redshift Java API V2.</li>
        <li>You can view the items by choosing the <i>Get Posts</i> menu item. Next, select a language.</li>
        <li>You can view the items by chooing either the <b>Five Posts</b>, <b>Ten Posts</b>, or <b>All Posts</b> button. </li>
        <li>The items appear in the page from newest to oldest.</li>
    </ol>
    <div>
    </body>
    </html>

### layout.html
The following code represents the **layout.html** file that represents the application's menu.

     <!DOCTYPE html>
     <html xmlns:th="http://www.thymeleaf.org" xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity3">
     <head th:fragment="site-head">
     <meta charset="UTF-8" />
     <link rel="icon" href="../public/img/favicon.ico" th:href="@{/img/favicon.ico}" />
     <script th:src="|https://code.jquery.com/jquery-1.12.4.min.js|"></script>
     <meta th:include="this :: head" th:remove="tag"/>
    </head>
    <body>
     <!-- th:hef calls a controller method - which returns the view -->
     <header th:fragment="site-header">
     <a href="index.html" th:href="@{/}"><img src="../public/img/site-logo.png" th:src="@{/img/site-logo.png}" /></a>
     <a href="#" style="color: white" th:href="@{/}">Home</a>
     <a href="#" style="color: white" th:href="@{/add}">Add Post</a>
     <a href="#"  style="color: white" th:href="@{/posts}">Get Posts</a>
     <div id="logged-in-info">

        <form method="post" th:action="@{/logout}">
            <input type="submit"  value="Logout"/>
        </form>
    </div>
    </header>
    <h1>Welcome</h1>
    <body>
    <p>Welcome to  AWS Blog application.</p>
    </body>
    </html>

### add.html
The **add.html** file is the application's view that lets users post new items. 

      <!DOCTYPE html>
      <html xmlns:th="http://www.thymeleaf.org" xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity3">

     <head>
     <meta charset="UTF-8" />
     <title>Blog</title>

     <script th:src="|https://code.jquery.com/jquery-1.12.4.min.js|"></script>
     <script src="../public/js/contact_me.js" th:src="@{/js/contact_me.js}"></script>
     <link rel="stylesheet" th:href="|https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css|"/>
     <link rel="stylesheet" href="../public/css/styles.css" th:href="@{/css/styles.css}" />
     <link rel="icon" href="../public/img/favicon.ico" th:href="@{/img/favicon.ico}" />
     </head>

     <body>
     <header th:replace="layout :: site-header"/>
     <div class="container">
     <h3>Welcome <span sec:authentication="principal.username">User</span> to the Amazon Redshift Job Posting example app</h3>
     <p>Now is: <b th:text="${execInfo.now.time}"></b></p>
     <p>Add a new job posting by filling in this table and clicking <i>Create Item</i></p>

     <div class="row">
        <div class="col-lg-8 mx-auto">
                <div class="control-group">
                    <div class="form-group floating-label-form-group controls mb-0 pb-2">
                        <label>Title</label>
                        <input class="form-control" id="title" placeholder="Title" required="required" data-validation-required-message="Please enter the AWS Guide.">
                        <p class="help-block text-danger"></p>
                    </div>
                </div>
                <div class="control-group">
                    <div class="form-group floating-label-form-group controls mb-0 pb-2">
                        <label>Body</label>
                        <textarea class="form-control" id="body" rows="5" placeholder="Body" required="required" data-validation-required-message="Please enter a description."></textarea>
                        <p class="help-block text-danger"></p>
                    </div>
                </div>
                <br>
                <button type="submit" class="btn btn-primary btn-xl" id="SendButton">Create Item</button>
            </div>
       </div>
       </div>
      </body>
     </html>

### post.html
The **post.html** file is the application's view that displays the items in the specific language. 

    <!DOCTYPE html>
     <html xmlns:th="http://www.thymeleaf.org" xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity3">

     <head>
     <meta charset="UTF-8" />
     <title>Blog</title>

     <script th:src="|https://code.jquery.com/jquery-1.12.4.min.js|"></script>
     <script th:src="|https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js|"></script>
     <script th:src="|https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js|"></script>
     <link rel="stylesheet" th:href="|https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css|"/>
     <script src="../public/js/contact_me.js" th:src="@{/js/contact_me.js}"></script>
     <link rel="stylesheet" href="../public/css/styles.css" th:href="@{/css/styles.css}" />
     <link rel="icon" href="../public/img/favicon.ico" th:href="@{/img/favicon.ico}" />
     </head>

     <body>
     <header th:replace="layout :: site-header"/>

    <div class="container">
     <h3>Welcome <span sec:authentication="principal.username">User</span> to the Amazon Redshift Job Posting example app</h3>
     <p>Now is: <b th:text="${execInfo.now.time}"></b></p>

     <div id= "progress" class="progress">
        <div class="progress-bar progress-bar-striped progress-bar-animated" role="progressbar" aria-valuenow="75" aria-valuemin="0" aria-valuemax="100" style="width: 75%"></div>
     </div>

    <div class="row">
        <div class="col">
            <div class="col-lg-10">
                <div class="clearfix mt-40">
                    <ul class="xsearch-items">
                    </ul>
                </div>
            </div>
        </div>
        <div class="col-4">
            <label for="lang">Select a Language:</label>
            <select name="lang" id="lang">
                <option>English</option>
                <option>French</option>
                <option>Spanish</option>
                <option>Russian</option>
                <option>Chinese</option>
                <option>Japanese</option>
            </select>
        </div>
        <div>
            <button type="button" onclick="getPosts(5)">Five Posts</button>
            <button type="button" onclick="getPosts(10)">Ten Posts</button>
            <button type="button" onclick="getPosts(0)">All Posts</button>
        </div>
     </div>
     </div>
     </div>
     </body>
    </html>

### login.html
The **login.html** file is the application's login page. 

     <!DOCTYPE html>
     <html xmlns="http://www.w3.org/1999/xhtml" xmlns:th="https://www.thymeleaf.org">

    <head>
     <title>AWS Blog Example</title>
     <style>
        body {font-family: Arial, Helvetica, sans-serif;}
        form {border: 3px solid #f1f1f1;}

        input[type=text], input[type=password] {
            width: 100%;
            padding: 12px 20px;
            margin: 8px 0;
            display: inline-block;
            border: 1px solid #ccc;
            box-sizing: border-box;
        }

        button {
            background-color: #4CAF50;
            color: white;
            padding: 14px 20px;
            margin: 8px 0;
            border: none;
            cursor: pointer;
            width: 100%;
        }

        button:hover {
            opacity: 0.8;
        }

        .cancelbtn {
            width: auto;
            padding: 10px 18px;
            background-color: #f44336;
        }

        .imgcontainer {
            text-align: center;
            margin: 24px 0 12px 0;
        }

        img.avatar {
            width: 40%;
            border-radius: 50%;
        }

        .container {
            padding: 16px;
        }

        span.psw {
            float: right;
            padding-top: 16px;
        }

        /* Change styles for span and cancel button on extra small screens */
        @media screen and (max-width: 300px) {
            span.psw {
                display: block;
                float: none;
            }
            .cancelbtn {
                width: 100%;
            }
        }
    </style>
    </head>
    <body>
    <div th:if="${param.error}">
     Invalid username and password.
    </div>
    <div th:if="${param.logout}">
     You have been logged out.
    </div>
    <form th:action="@{/login}" method="post">
     <div class="container">
        <label for="username"><b>Username</b></label>
        <input type="text" placeholder="Enter Username" id="username" name="username" value ="user" required>

        <label for="password"><b>Password</b></label>
        <input type="password" placeholder="Enter Password" id ="password" name="password" value ="password" required>

        <button type="submit">Login</button>

     </div>

     <div class="container" style="background-color:#f1f1f1">
        <button type="button" class="cancelbtn">Cancel</button>
        <span class="psw">Forgot <a href="#">password?</a></span>
      </div>
    </form>
    </body>
    </html>
    
### Create the JS File

This application has a **contact_me.js** file that is used to send requests to the Spring Controller. Place this file in the **resources\public\js** folder. 

      $(function() {

       $('#progress').hide();

        $("#SendButton" ).click(function($e) {

         var title = $('#title').val();
         var body = $('#body').val();
        
        var xhr = new XMLHttpRequest();
        xhr.addEventListener("load", loadNewItems, false);
        xhr.open("POST", "../addPost", true);   //buildFormit -- a Spring MVC controller
        xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");//necessary
        xhr.send("title=" + title + "&body=" + body);
        } );// END of the Send button click

       function loadNewItems(event) {
        var msg = event.target.responseText;
        alert("You have successfully added item "+msg)

        $('#title').val("");
        $('#body').val("");
        }
       } );

    function getPosts(num){

     $('.xsearch-items').empty()
     $('#progress').show();
     var lang = $('#lang option:selected').text();
    
    var xhr = new XMLHttpRequest();
    xhr.addEventListener("load", loadItems, false);
    xhr.open("POST", "../getPosts", true);   //buildFormit -- a Spring MVC controller
    xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");//necessary
    xhr.send("lang=" + lang +"&number=" + num );
    }

    function loadItems(event) {

    $('#progress').hide();
    var xml = event.target.responseText;
    $(xml).find('Item').each(function ()  {

        var $field = $(this);
        var id = $field.find('Id').text();
        var date = $field.find('Date').text();
        var title = $field.find('Title').text();
        var body = $field.find('Content').text();
        var author = $field.find('Author').text();
        
        // Append this data to the main list.
        $('.xsearch-items').append("<className='search-item'>");
        $('.xsearch-items').append("<div class='search-item-content'>");
        $('.xsearch-items').append("<h3 class='search-item-caption'><a href='#'>"+title+"</a></h3>");
        $('.xsearch-items').append("<className='search-item-meta mb-15'>");
        $('.xsearch-items').append("<className='list-inline'>");
        $('.xsearch-items').append("<p><b>"+date+"</b></p>");
        $('.xsearch-items').append("<p><b>'Posted by "+author+"</b></p>");
        $('.xsearch-items').append("<div>");
        $('.xsearch-items').append("<h6>"+body +"</h6>");
        $('.xsearch-items').append("</div>");
       });
      }

## Create a JAR file for the application

Package up the project into a .jar (JAR) file that you can deploy to Elastic Beanstalk by using the following Maven command.

	mvn package

The JAR file is located in the target folder.

![AWS Tracking Application](images/Target.png)

The POM file contains the **spring-boot-maven-plugin** that builds an executable JAR file that includes the dependencies. Without the dependencies, the application does not run on Elastic Beanstalk. For more information, see [Spring Boot Maven Plugin](https://www.baeldung.com/executable-jar-with-maven).

## Deploy the application to Elastic Beanstalk

Sign in to the AWS Management Console, and then open the Elastic Beanstalk console. An application is the top-level container in Elastic Beanstalk that contains one or more application environments (for example prod, qa, and dev, or prod-web, prod-worker, qa-web, qa-worker).

If this is your first time accessing this service, you will see a **Welcome to AWS Elastic Beanstalk** page. Otherwise, you’ll see the Elastic Beanstalk Dashboard, which lists all of your applications.

#### To deploy the application to Elastic Beanstalk

1. Open the Elastic Beanstalk console at https://console.aws.amazon.com/elasticbeanstalk/home.
2. In the navigation pane, choose  **Applications**, and then choose **Create a new application**. This opens a wizard that creates your application and launches an appropriate environment.
3. On the **Create New Application** page, enter the following values:
   + **Application Name** - Redshift App
   + **Description** - A description for the application
4. Choose **Create**.
5. Choose **Create a new environment**.
6. Choose **Web server environment**.
7. Choose **Select**.
8. In the **Environment information** section, leave the default values.
9. In the **Platform** section, choose **Managed platform**.
10. For **Platform**, choose **Java** (accept the default values for the other fields).
11. In the **Application code** section, choose **Upload your code**.
12. Choose **Local file**, and then select **Choose file**. Browse to the JAR file that you created.  
13. Choose **Create environment**. You'll see the application being created. When you’re done, you will see the application state the **Health** is **Ok** .
14. To change the port that Spring Boot listens on, add an environment variable named **SERVER_PORT**, with the value **5000**.
11. Add a variable named **AWS_ACCESS_KEY_ID**, and then specify your access key value.
12. Add a variable named **AWS_SECRET_ACCESS_KEY**, and then specify your secret key value. After the variables are configured, you'll see the URL for accessing the application.

![AWS Tracking Application](images/Beanstalk.png)

**Note:** If you don't know how to set variables, see [Environment properties and other software settings](https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/environments-cfg-softwaresettings.html).

To access the application, open your browser and enter the URL for your application. You will see the Home page for your application.

### Next steps
Congratulations! You have created a Spring Boot application that uses the Amazon Redshift data client to create an example job posting application. As stated at the beginning of this tutorial, be sure to terminate all of the resources you create while going through this tutorial to ensure that you’re not charged.

For more AWS multiservice examples, see
[usecases](https://github.com/awsdocs/aws-doc-sdk-examples/tree/master/javav2/usecases).
