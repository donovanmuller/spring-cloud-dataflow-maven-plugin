# Spring Cloud Data Flow Maven Plugin

This Maven plugin provides support for orchestrating Spring Boot applications with [Spring Cloud Data Flow](https://cloud.spring.io/spring-cloud-dataflow/).

## Goals Overview

The Spring Cloud Data Flow Maven Plugin has the following goals.

* [scdf:deploy-standalone](#deploying-a-standalone-application) deploys a standalone application with Spring Cloud Data Flow

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

#### Attributes

* Requires a Maven project to be executed.
* Binds by default to the lifecycle phase: `install`.
* Invokes the execution of the lifecycle phase `install` prior to executing itself.

#### Optional Parameters

| Name | Type | Description |
| --- | --- | --- |
| `applicationProperties` | `Map<String, String>` | [Application properties](http://docs.spring.io/spring-cloud-dataflow/docs/1.0.1.RELEASE/reference/html/spring-cloud-dataflow-create-stream.html#_application_properties) as defined when creating the application definition |
| `deploymentProperties` | `Map<String, String>` | [Deployment properties](http://docs.spring.io/spring-cloud-dataflow/docs/1.0.1.RELEASE/reference/html/spring-cloud-dataflow-create-stream.html#_deployment_properties) as provided when deploying the application |

