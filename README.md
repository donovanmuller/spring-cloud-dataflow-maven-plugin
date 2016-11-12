# Spring Cloud Data Flow Maven Plugin

This Maven plugin provides support for orchestrating Spring Boot applications with [Spring Cloud Data Flow](https://cloud.spring.io/spring-cloud-dataflow/).

## Goals Overview

The Spring Cloud Data Flow Maven Plugin has the following goals.

* [scdf:deploy-standalone](#deploying-a-standalone-application) deploys a standalone application with Spring Cloud Data Flow
* [scdf:deploy](#) deploys an Application Group Descriptor project with Spring Cloud Data Flow

### Deploying a standalone application

A standalone application is any Spring Boot application.
This application type is a proposal discussed in detail [in this post](https://blog.switchbit.io/introducing-standalone-applications-to-spring-cloud-data-flow).


Below is an example of adding this plugin to a Spring Boot application:

```
<build>
    ...
    <plugins>
        ...
        <plugin>
            <groupId>io.switchbit</groupId>
            <artifactId>spring-cloud-dataflow-maven-plugin</artifactId>
            <version>1.0.0.M1</version>
            <configuration>
                <applicationProperties>
                    <server.port>9001</server.port>
                </applicationProperties>
                <deploymentProperties>
                    <app.example.count>2</app.test-app.count>
                </deploymentProperties>
            </configuration>
        </plugin>
        ...
    </plugins>
    ...
</build>
```

### Deploying an Application Group Descriptor project
 
An Application Group Descriptor project is a Maven project/module that contains a descriptor
file (`application-group.yml` by convention). This descriptor file contains the details of the applications that 
should be registered, as well as the definitions of the streams, tasks and standalone applications that should be 
created and deployed.

This application type is a proposal discussed in detail [in this post](https://blog.switchbit.io/a-take-on-application-groups-with-spring-cloud-data-flow).

Below is an example of adding this plugin to a Application Group Descriptor project:

```
<build>
    ...
    <plugins>
        ...
        <plugin>
            <groupId>io.switchbit</groupId>
            <artifactId>spring-cloud-dataflow-maven-plugin</artifactId>
            <version>1.1.0.M1</version>
            <configuration>
                <skip>false</skip>
                <!-- These deployment properties will be passed to the relevant streams on deployment -->
                <deploymentProperties>
                </deploymentProperties>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <!-- This goal parses the 'application-group.yml' descriptor file and
                        builds the app resource URI's if 'artifactId' are referenced as well as basic
                        validation of the structure of the descriptor file.
                        -->
                        <goal>application-group-processor</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
        ...
    </plugins>
    ...
</build>
```

#### Application Group Processor Goal

The `application-group-processor` goal is responsible for parsing the descriptor file and
expanding shortened URI references by using the Maven dependency details found in the POM.

### Attributes

* Requires a Maven project to be executed.
* Binds by default to the lifecycle phase: `install`.
* Invokes the execution of the lifecycle phase `install` prior to executing itself.

#### Optional Parameters

| Name | Type | Description |
| --- | --- | --- |
| `applicationProperties` | `Map<String, String>` | [Application properties](http://docs.spring.io/spring-cloud-dataflow/docs/1.0.1.RELEASE/reference/html/spring-cloud-dataflow-create-stream.html#_application_properties) as defined when creating the application definition |
| `deploymentProperties` | `Map<String, String>` | [Deployment properties](http://docs.spring.io/spring-cloud-dataflow/docs/1.0.1.RELEASE/reference/html/spring-cloud-dataflow-create-stream.html#_deployment_properties) as provided when deploying the application |

