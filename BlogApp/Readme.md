# Creating a Job Posting Site using Amazon Redshift and Amazon Translation Services

You can create a web application that stores and queries data by using the Amazon Redshift service and the Amazon Redshift Java API V2. To interact with an Amazon Redshift table, you can use a [software.amazon.awssdk.services.redshiftdata.RedshiftDataClient](https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/services/redshiftdata/RedshiftDataClient.html). The application created in this AWS tutorial is a job posting web application that lets an employer, an administrator, or human resources staff alert current employees or the public about an immediate or future job opening within a company. The data is stored in a Redshift table named **blog**, as shown in this illustration. 

![AWS Tracking Application](images/Redshift.png)

The **blog** table contains these fields: 

- **idblog**- a varchar field that stores a GUID value and represents the PK.
- **date**- a date field that represents the date when the record was added. 
- **title**- a varchar field that represents the title. 
- **body**- a varchar field that represents the body. 
- **author**-a varchar field that represents the author. 

**Note**: For more information about supported field data types, see [Data types](https://docs.aws.amazon.com/redshift/latest/dg/c_Supported_data_types.html). 

The application you create uses Spring Boot APIs to build a model, different views, and a controller. This web application also lets a user submit a new job posting that is then stored into the **blog** table, as shown in this illustation. 

![AWS Tracking Application](images/NewRecord.png)

This example application lets you view the post by choosing the **Get Post** menu item and choosing one of the available buttons. For example, you can view five recent posts by choosing the **Five Posts** button, as shown in the following illustration.

![AWS Tracking Application](images/FiveRecords.png)

This example web application also supports viewing the result set in different languages. For example, if a user wants to view the result set in Spanish, they can choose Spanish from the dropdown field and the data is translated to the given language by using the Amazon Translate Service, as shown in this illustration. 

![AWS Tracking Application](images/Spanish.png)

**Cost to complete:** The AWS services included in this document are included in the [AWS Free Tier](https://aws.amazon.com/free/?all-free-tier.sort-by=item.additionalFields.SortRank&all-free-tier.sort-order=asc).

**Note:** Be sure to terminate all of the resources you create while going through this tutorial to ensure that you’re not charged.

#### Topics

+ Prerequisites
+ Create an IntelliJ project 
+ Add the POM dependencies to your project
+ Set up the Java packages in your project
+ Create the Java classes
+ Create the HTML files
+ Package the application into a JAR file
+ Deploy the application to Elastic Beanstalk


## Prerequisites

To complete the tutorial, you need the following:

+ An AWS account
+ A Java IDE (this tutorial uses the IntelliJ IDE)
+ Java JDK 1.8
+ Maven 3.6 or later
+ An Amazon Redshift table named **blog** that contains the fields described in this tutorial. For information about creating an Amazon Redshift table, see [Getting started using databases](https://docs.aws.amazon.com/redshift/latest/dg/c_intro_to_admin.html).
.  

## Create an IntelliJ project named Blog

Create an IntelliJ project that is used to create the web application.

1. In the IntelliJ IDE, choose **File**, **New**, **Project**.

2. In the New Project dialog box, choose **Maven**.

3. Choose **Next**.

4. In **GroupId**, enter **spring-aws**.

5. In **ArtifactId**, enter **Blog**.

6. Choose **Next**.

7. Choose **Finish**.

## Add the Spring POM dependencies to your project

At this point, you have a new project named **Blog**. Ensure that the pom.xml file resembles the following code.

     <?xml version="1.0" encoding="UTF-8"?>
     <project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
     <modelVersion>4.0.0</modelVersion>
    <groupId>aws-spring</groupId>
    <artifactId>Blog</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.0.4.RELEASE</version>
        <relativePath /> <!-- lookup parent from repository -->
    </parent>
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <java.version>1.8</java.version>
    </properties>
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>software.amazon.awssdk</groupId>
                <artifactId>bom</artifactId>
                <version>2.15.14</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>ses</artifactId>
        </dependency>
         <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <version>3.8.0</version>
            <scope>test</scope>
        </dependency>
         <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>redshift</artifactId>
        </dependency>
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>translate</artifactId>
        </dependency>
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>redshiftdata</artifactId>
        </dependency>
        <dependency>
            <groupId>org.webjars</groupId>
            <artifactId>bootstrap</artifactId>
            <version>3.3.7</version>
        </dependency>
        <dependency>
            <groupId>org.webjars</groupId>
            <artifactId>jquery</artifactId>
            <version>3.2.1</version>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <scope>runtime</scope>
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
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
            <exclusions>
                <exclusion>
                    <groupId>org.junit.vintage</groupId>
                    <artifactId>junit-vintage-engine</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
     
 ## Create the Java classes
 
 Create a Java package in the main/java folder named **com.aws.blog**. This Java classes go into this package. 
 
 ![AWS Lex](images/Java.png)
 
 Create these Java classes:

+ **BlogApp** - Used as the base class for the Spring Boot application.
+ **BlogController** - Used as the Spring Boot controller that handles HTTP requests. 
+ **Post** - Used as the applications model that stores application data.
+ **RedshiftService** - Used as the Spring Service that uses the Redshift Java API V2. 
+ **WebSecurityConfig** - The role of this class is to ensure only authenticated users can view the application. 

### BlogApp class

The following Java code represents the **BlogApp** class.

     package com.aws.blog;

     import org.springframework.boot.SpringApplication;
     import org.springframework.boot.autoconfigure.SpringBootApplication;

     @SpringBootApplication
     public class BlogApp {

     public static void main(String[] args) throws Throwable {
        SpringApplication.run(BlogApp.class, args);
     }
   }

### BlogController class

The following Java code represents the **BlogController** class.

     package com.aws.blog;
    
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.ResponseBody;
    import org.springframework.web.bind.annotation.RequestMethod;
    import javax.servlet.http.HttpServletRequest;
    import javax.servlet.http.HttpServletResponse;
    import java.io.IOException;
    import java.util.List;
    import org.springframework.beans.factory.annotation.Autowired;

    @Controller
    public class BlogController {

    @Autowired
    RedshiftService rs;

    @GetMapping("/")
    public String root() {
        return "index";
    }

    @GetMapping("/add")
    public String add() {
        return "add";
    }


    @GetMapping("/posts")
    public String post() {
        return "post";
    }

    @GetMapping("/login")
    public String login(Model model) {
        return "login";
    }

    // Adds a new item to the database
    @RequestMapping(value = "/addPost", method = RequestMethod.POST)
    @ResponseBody
    String addItems(HttpServletRequest request, HttpServletResponse response) {

        //Get the Logged in User
        String name = getLoggedUser();
        String title = request.getParameter("title");
        String body = request.getParameter("body");
        String id = rs.addRecord(name, title, body) ;
        return id;
    }


    // Queries five items from the Redshift database.
    @RequestMapping(value = "/fivePost", method = RequestMethod.POST)
    @ResponseBody
    String getFivePosts(HttpServletRequest request, HttpServletResponse response) {

        //Get the Logged in User
        String name = getLoggedUser();
        String lang = request.getParameter("lang");
        String xml =  rs.getPosts(lang,5) ;
        return xml;
    }


    // Queries ten items from the Redshift database.
    @RequestMapping(value = "/tenPost", method = RequestMethod.POST)
    @ResponseBody
    String getTenPosts(HttpServletRequest request, HttpServletResponse response) {

        //Get the Logged in User
        String name = getLoggedUser();
        String lang = request.getParameter("lang");
        String xml =  rs.getPosts(lang,10) ;
        return xml;
    }

    private String getLoggedUser() {

        // Get the logged-in Useruser
        org.springframework.security.core.userdetails.User user2 = (org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String name = user2.getUsername();
        return name;
    }
}
### Post class

The following Java code represents the **Post** class.

    package com.aws.blog;

    public class Post {

    private String id;
    private String title;
    private String body;
    private String author;
    private String date ;

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return this.date ;
    }


    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthor() {
        return this.author ;
    }


    public void setBody(String body) {
        this.body = body;
    }

    public String getBody() {
        return this.body ;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title ;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id ;
    }
   }

### RedshiftService class

The following Java code represents the **RedshiftService** class. This class uses the Amazon Redshift Java API (V2) to interact with data located the **blog** table.  For example, the **getPosts** method returns a result set that is queried from the **blog** table and displayed in the view. Likewise, the **addRecord** method adds a new record to the **blog** table. 

     package com.aws.blog;

     import org.springframework.stereotype.Component;
     import software.amazon.awssdk.regions.Region;
     import software.amazon.awssdk.services.redshiftdata.model.*;
     import software.amazon.awssdk.services.redshiftdata.RedshiftDataClient;
     import org.w3c.dom.Document;
     import org.w3c.dom.Element;
     import software.amazon.awssdk.services.translate.TranslateClient;
     import software.amazon.awssdk.services.translate.model.TranslateException;
     import software.amazon.awssdk.services.translate.model.TranslateTextRequest;
     import software.amazon.awssdk.services.translate.model.TranslateTextResponse;
     import javax.xml.parsers.DocumentBuilder;
     import javax.xml.parsers.DocumentBuilderFactory;
     import javax.xml.parsers.ParserConfigurationException;
     import javax.xml.transform.Transformer;
     import javax.xml.transform.TransformerException;
     import javax.xml.transform.TransformerFactory;
     import javax.xml.transform.dom.DOMSource;
     import javax.xml.transform.stream.StreamResult;
     import java.io.StringWriter;
     import java.text.ParseException;
     import java.text.SimpleDateFormat;
     import java.time.LocalDateTime;
     import java.time.format.DateTimeFormatter;
     import java.util.ArrayList;  
     import java.util.Date;
     import java.util.List;
     import java.util.UUID;

    @Component
    public class RedshiftService {

    String clusterId = "<Enter your Cluster ID>";
    String database = "<Enter your database name>";
    String dbUser = "<Enter your dbUser value>";

    private RedshiftDataClient getClient() {

        Region region = Region.US_WEST_2;
        RedshiftDataClient redshiftDataClient = RedshiftDataClient.builder()
                .region(region)
                .build();

        return redshiftDataClient;
    }


    // Returns a collection that returns the latest five posts from the Redshift table.
    public String getPosts(String lang, int num) {

        try {

            RedshiftDataClient redshiftDataClient = getClient();
            String sqlStatement="";
            if (num ==5)
                sqlStatement = "SELECT TOP 5 * FROM blog ORDER BY date";
            else
                sqlStatement = "SELECT TOP 10 * FROM blog ORDER BY date";

            ExecuteStatementRequest statementRequest = ExecuteStatementRequest.builder()
                    .clusterIdentifier(clusterId)
                    .database(database)
                    .dbUser(dbUser)
                    .sql(sqlStatement)
                    .build();

            ExecuteStatementResponse response = redshiftDataClient.executeStatement(statementRequest);
            String myId = response.id();
            checkStatement(redshiftDataClient,myId );
            List<Post> posts = getResults(redshiftDataClient, myId, lang);
            return convertToString(toXml(posts));

        } catch (RedshiftDataException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return "";
    }

    public String addRecord(String author, String title, String body) {

        try {

            RedshiftDataClient redshiftDataClient = getClient();
            UUID uuid = UUID.randomUUID();
            String id  = uuid.toString();

            // Date conversion
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String sDate1 = dtf.format(now);
            Date date1 = new SimpleDateFormat("yyyy/MM/dd").parse(sDate1);
            java.sql.Date sqlDate = new java.sql.Date( date1.getTime());


            // Inject an item into the system.
            String sqlStatement = "INSERT INTO blog (idblog, date, title, body, author) VALUES( '"+uuid+"' ,'"+sqlDate +"','"+title +"' , '"+body +"', '"+author +"');";
            ExecuteStatementRequest statementRequest = ExecuteStatementRequest.builder()
                    .clusterIdentifier(clusterId)
                    .database(database)
                    .dbUser(dbUser)
                    .sql(sqlStatement)
                    .build();

            redshiftDataClient.executeStatement(statementRequest);

            return id;

        } catch (RedshiftDataException | ParseException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return null;
    }

    public void checkStatement(RedshiftDataClient redshiftDataClient,String sqlId ) {

        try {

            DescribeStatementRequest statementRequest = DescribeStatementRequest.builder()
                    .id(sqlId)
                    .build() ;

            // Wait until the sql statement processing is finished.
            boolean finished = false;
            String status = "";
            while (!finished) {

                DescribeStatementResponse response = redshiftDataClient.describeStatement(statementRequest);
                status = response.statusAsString();
                System.out.println("..."+status);

                if (status.compareTo("FINISHED") == 0) {
                    break;
                }
                Thread.sleep(500);
            }

            System.out.println("The statement is finished!");

        } catch (RedshiftDataException | InterruptedException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }


    public List<Post> getResults(RedshiftDataClient redshiftDataClient, String statementId, String lang) {

        try {

            List<Post>records = new ArrayList<>();
            GetStatementResultRequest resultRequest = GetStatementResultRequest.builder()
                    .id(statementId)
                    .build();

            GetStatementResultResponse response = redshiftDataClient.getStatementResult(resultRequest);

            // Iterate through the List element where each element is a List object.
            List<List<Field>> dataList = response.records();

            Post post ;
            int index = 0 ;
            // Print out the records.
            for (List list: dataList) {

                // new Post object here
                post = new Post();
                index = 0 ;
                for (Object myField:list) {

                    Field field = (Field) myField;
                    String value = field.stringValue();

                    if (index == 0)
                        post.setId(value);

                    else if (index == 1)
                        post.setDate(value);

                    else if (index == 2) {

                        if (!lang.equals("English")) {
                            // We need to translate the text
                            value = translateText(value, lang);
                        }
                        post.setTitle(value);
                    }

                    else if (index == 3) {
                        if (!lang.equals("English")) {
                            // We need to translate the text
                           value = translateText(value, lang);
                        }
                        post.setBody(value);
                    }

                    else if (index == 4)
                        post.setAuthor(value);

                    // Increment the index.
                    index ++;
               }

                // Push the Post object to the List
                records.add(post);
            }

            return  records;

        } catch (RedshiftDataException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        return null ;
    }

    // Convert Work item data into XML to pass back to the view.
    private Document toXml(List<Post> itemsList) {

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            // Start building the XML.
            Element root = doc.createElement("Items");
            doc.appendChild(root);

            // Iterate through the collection.
            for (Post post : itemsList) {

                    Element item = doc.createElement("Item");
                    root.appendChild(item);

                    // Set Id.
                    Element id = doc.createElement("Id");
                    id.appendChild(doc.createTextNode(post.getId()));
                    item.appendChild(id);

                    // Set Date.
                    Element name = doc.createElement("Date");
                    name.appendChild(doc.createTextNode(post.getDate()));
                    item.appendChild(name);

                    // Set Title.
                    Element date = doc.createElement("Title");
                    date.appendChild(doc.createTextNode(post.getTitle()));
                    item.appendChild(date);

                    // Set Body.
                    Element desc = doc.createElement("Content");
                    desc.appendChild(doc.createTextNode(post.getBody()));
                    item.appendChild(desc);

                    // Set Author.
                    Element guide = doc.createElement("Author");
                    guide.appendChild(doc.createTextNode(post.getAuthor()));
                    item.appendChild(guide);
             }

            return doc;

        }catch(ParserConfigurationException e){
            e.printStackTrace();
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

    private String translateText(String text, String lang) {

        Region region = Region.US_WEST_2;
        TranslateClient translateClient = TranslateClient.builder()
                //.credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .region(region)
                .build();
        String transValue = "";
        try {

            if (lang.compareTo("French")==0) {

                TranslateTextRequest textRequest = TranslateTextRequest.builder()
                        .sourceLanguageCode("en")
                        .targetLanguageCode("fr")
                        .text(text)
                        .build();

                TranslateTextResponse textResponse = translateClient.translateText(textRequest);
                transValue = textResponse.translatedText();

            } else if (lang.compareTo("Russian")==0) {

                TranslateTextRequest textRequest = TranslateTextRequest.builder()
                        .sourceLanguageCode("en")
                        .targetLanguageCode("ru")
                        .text(text)
                        .build();

                TranslateTextResponse textResponse = translateClient.translateText(textRequest);
                transValue = textResponse.translatedText();


            } else if (lang.compareTo("Japanese")==0) {

                TranslateTextRequest textRequest = TranslateTextRequest.builder()
                        .sourceLanguageCode("en")
                        .targetLanguageCode("ja")
                        .text(text)
                        .build();

                TranslateTextResponse textResponse = translateClient.translateText(textRequest);
                transValue = textResponse.translatedText();


            } else if (lang.compareTo("Spanish")==0) {

                TranslateTextRequest textRequest = TranslateTextRequest.builder()
                        .sourceLanguageCode("en")
                        .targetLanguageCode("es")
                        .text(text)
                        .build();

                TranslateTextResponse textResponse = translateClient.translateText(textRequest);
                transValue = textResponse.translatedText();

            } else {

                TranslateTextRequest textRequest = TranslateTextRequest.builder()
                        .sourceLanguageCode("en")
                        .targetLanguageCode("zh")
                        .text(text)
                        .build();

                TranslateTextResponse textResponse = translateClient.translateText(textRequest);
                transValue = textResponse.translatedText();
            }

        return transValue;

        } catch (TranslateException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        return "";
       }
     }

### WebSecurityConfig class

The following Java code represents the WebSecurityConfig class. The role of this class is to ensure only authenticated users can view the application.

     package com.aws.blog;

     import org.springframework.context.annotation.Bean;
     import org.springframework.context.annotation.Configuration;
     import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
     import org.springframework.security.config.annotation.web.builders.HttpSecurity;
     import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
     import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
     import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
     import org.springframework.security.crypto.password.PasswordEncoder;
     import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

     @Configuration
     @EnableWebSecurity
     public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers(
                        "/js/**",
                        "/css/**",
                        "/img/**",
                        "/webjars/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
                .logout()
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout")
                .permitAll();

          http.csrf().disable();
         }


       @Override
       protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .passwordEncoder(passwordEncoder())
                .withUser("user")
                .password(passwordEncoder().encode("password"))
                .roles("USER");
       }

      @Bean
      public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
     }
    }

## Create the HTML file

At this point, you have created all of the Java files required for this example Spring Boot application. Now you create HTML files that are required for the application's view. Under the resource folder, create a **templates** folder, and then create the following HTML files:

+ index.html
+ layout.html
+ upload.html
+ video.html

### index.html
The **index.html** file is the application's home view. The following HTML represents the **index.html** file. 

     <!DOCTYPE HTML>
     <html xmlns:th="https://www.thymeleaf.org">
     <meta charset="utf-8" />
     <meta http-equiv="X-UA-Compatible" content="IE=edge" />
     <meta name="viewport" content="width=device-width, initial-scale=1" />

    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm" crossorigin="anonymous">
    <link rel="stylesheet" href="../public/css/styles.css" th:href="@{/css/styles.css}" />
    <link rel="icon" href="../public/img/favicon.ico" th:href="@{/img/favicon.ico}" />

    <title>AWS Item Tracker</title>
    </head>

    <body>
    <header th:replace="layout :: site-header"></header>
    <div class="container">

    <h2>Video Stream over HTTP App</h2>

    <p>The AWS Item Tracker application is a sample application that uses multiple AWS Services and the Java V2 API. Collecting and working with items has never been easier! Simply perform these steps:<p>

    <ol>
        <li>Enter work items into the system by choosing the <i>Add Items</i> menu item. Fill in the form and then choose <i>Create Item</i>.</li>
        <li>The AWS Item Tracker application stores the data by using the Amazon Relational Database Service (Amazon RDS).</li>
        <li>You can view all of your items by choosing the <i>Get Items</i> menu item. Next, choose <i>Get Active Items</i> in the dialog box.</li>
        <li>You can modify an Active Item by selecting an item in the table and then choosing <i>Get Single Item</i>. The item appears in the Modify Item section where you can modify the description or status.</li>
        <li>Modify the item and then choose <i>Update Item</i>. You cannot modify the ID value. </li>
        <li>You can archive any item by selecting the item and choosing <i>Archive Item</i>. Notice that the table is updated with only active items.</li>
        <li>You can display all archived items by choosing <i>Get Archived Items</i>. You cannot modify an archived item.</li>
        <li>You can send an email recipient an email message with a report attachment by selecting the email recipient from the dialog box and then choosing <i>Send Report</i>.Only Active data is sent in a report.</li>
        <li>The Amazon Simple Email Service is used to send an email with an Excel document to the selected email recipient.</li>
    </ol>
    </div>

    </body>
    </html>

### layout.html
The following code represents the **layout.html** file that represents the application's menu.

     <!DOCTYPE html>
     <html xmlns:th="http://www.thymeleaf.org">
     <head th:fragment="site-head">
     <meta charset="UTF-8" />
     <link rel="icon" href="../public/images/favicon.ico" th:href="@{/images/favicon.ico}" />
     <script th:src="|https://code.jquery.com/jquery-1.12.4.min.js|"></script>
     <meta th:include="this :: head" th:remove="tag"/>
     </head>
     <body>
     <!-- th:hef calls a controller method - which returns the view -->
    <header th:fragment="site-header">
    <a href="index.html" th:href="@{/}"><img src="../public/img/site-logo.png" th:src="@{/img/site-logo.png}" /></a>
    <a href="#" style="color: white" th:href="@{/}">Home</a>
    <a href="#" style="color: white" th:href="@{/upload}">Upload Videos</a>
    <a href="#"  style="color: white" th:href="@{/watch}">Watch Videos</a>
    <div id="logged-in-info">
        <form method="post" th:action="@{/logout}">
            <input type="submit"  value="Logout"/>
        </form>
    </div>
    </header>
    <h1>Welcome</h1>
    </body>
    </html>

### upload.html
The **upload.html** file is the application's view that lets users upload a MP4 file. 

     <!DOCTYPE html>
     <html xmlns:th="http://www.thymeleaf.org">
     <head lang="en">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm" crossorigin="anonymous">
    <script th:src="|https://code.jquery.com/jquery-1.12.4.min.js|"></script>
    <link rel="stylesheet" href="../public/css/styles.css" th:href="@{/css/styles.css}" />
    <link rel="icon" href="../public/images/favicon.ico" th:href="@{/images/favicon.ico}" />
    <title>Spring Framework</title>
    </head>
    <body>
    <header th:replace="layout :: site-header"></header>

  
    <div class="container">
        <h2>Video Stream over HTTP App</h2>
        <p>Upload a MP4 video to an Amazon S3 bucket</p>

        <form method="POST" onsubmit="myFunction()" action="/fileupload" enctype="multipart/form-data">
            Video Description:<input type="text" name="description" required><br>
            <input type="file" name="file" /><br/><br/>
            <input type="submit" value="Submit" />
        </form>
    </div>
    </body>
    </html>

### video.html
The **video.html** file is the application's view that displays both the video menu and the video content. 

     <!DOCTYPE html>
     <html xmlns:th="http://www.thymeleaf.org">
     <head lang="en">
     <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm" crossorigin="anonymous">
    <script th:src="|https://code.jquery.com/jquery-1.12.4.min.js|"></script>
    <link rel="stylesheet" href="../public/css/styles.css" th:href="@{/css/styles.css}" />
    <link rel="icon" href="../public/images/favicon.ico" th:href="@{/images/favicon.ico}" />
    <title>Spring Framework</title>

    <script>
        $(function() {
            getItems();
        } );

        // Gets the MP4 tags to set in the scroll list.
        function getItems() {

            var xhr = new XMLHttpRequest();
            xhr.addEventListener("load", loadTags, false);
            xhr.open("GET", "../items", true);
            xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");//necessary
            xhr.send();
        }

        function loadTags(event) {

            var xml = event.target.responseText;
            $(xml).find('Tag').each(function () {

                var $field = $(this);
                var name = $field.find('Name').text();
                var description = $field.find('Description').text();
                var vv = "Prime video"

                // Append this data to the main list.
                $('.list-group').append("<className='list-group-item list-group-item-action flex-column align-items-start'>");
                $('.list-group').append("<h5 onMouseOver=\"this.style.cursor='pointer'\" onclick=\"addVideo('" +name+"')\" class='mb-1'>"+name+"</li>");
                $('.list-group').append("<p class='mb-1'>"+description+"</p>");
                $('.list-group').append("<small class='text-muted'>"+vv+"</small>");
                $('.list-group').append("<br class='row'>");
            });
        }

        function addVideo(myVid) {
            var myVideo = document.getElementById("video1");
            myVideo.src = "/"+myVid+"/stream";
        }
      </script>
      </head>
      <body>
      <header th:replace="layout :: site-header"></header>
      <div class="container">
      <h3>Video Stream over HTTP App</h3>
      <p>This example reads a MP4 video located in an Amazon S3 bucket and streams over HTTP</p>
      <div class="row">
        <div class="col">
            <video id="video1" width="750" height="440" controls>
                <source type="video/mp4">

                Your browser does not support HTML video.
            </video>
        </div>
        <div class="col">
            <div class="list-group">
            </div>
        </div>
      </div>
    </div>

    </body>
    </html>

## Create a JAR file for the application

Package up the project into a .jar (JAR) file that you can deploy to Elastic Beanstalk by using the following Maven command.

	mvn package

The JAR file is located in the target folder.

![AWS Tracking Application](images/pic5.png)

The POM file contains the **spring-boot-maven-plugin** that builds an executable JAR file that includes the dependencies. Without the dependencies, the application does not run on Elastic Beanstalk. For more information, see [Spring Boot Maven Plugin](https://www.baeldung.com/executable-jar-with-maven).

## Deploy the application to Elastic Beanstalk

Sign in to the AWS Management Console, and then open the Elastic Beanstalk console. An application is the top-level container in Elastic Beanstalk that contains one or more application environments (for example prod, qa, and dev, or prod-web, prod-worker, qa-web, qa-worker).

If this is your first time accessing this service, you will see a **Welcome to AWS Elastic Beanstalk** page. Otherwise, you’ll see the Elastic Beanstalk Dashboard, which lists all of your applications.

#### To deploy the application to Elastic Beanstalk

1. Open the Elastic Beanstalk console at https://console.aws.amazon.com/elasticbeanstalk/home.
2. In the navigation pane, choose  **Applications**, and then choose **Create a new application**. This opens a wizard that creates your application and launches an appropriate environment.
3. On the **Create New Application** page, enter the following values:
   + **Application Name** - Spring Video App
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
13. Choose **Create environment**. You'll see the application being created.

![AWS Tracking Application](images/pic6.png)

When you’re done, you will see the application state the **Health** is **Ok** .

14. To change the port that Spring Boot listens on, add an environment variable named **SERVER_PORT**, with the value **5000**.
11. Add a variable named **AWS_ACCESS_KEY_ID**, and then specify your access key value.
12. Add a variable named **AWS_SECRET_ACCESS_KEY**, and then specify your secret key value. After the variables are configured, you'll see the URL for accessing the application.

![AWS Tracking Application](images/pic7.png)

**Note:** If you don't know how to set variables, see [Environment properties and other software settings](https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/environments-cfg-softwaresettings.html).

To access the application, open your browser and enter the URL for your application. You will see the Home page for your application.

### Next steps
Congratulations! You have created a Spring Boot application that uses Amazon Lex to create an interactive user experience. As stated at the beginning of this tutorial, be sure to terminate all of the resources you create while going through this tutorial to ensure that you’re not charged.

For more AWS multiservice examples, see
[usecases](https://github.com/awsdocs/aws-doc-sdk-examples/tree/master/javav2/usecases).
