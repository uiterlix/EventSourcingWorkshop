# EventSourcing Workshop - ITCS Cologne 2023

## Prerequisites

- The code in this repository is written for Java 17.
- [IntelliJ Community or Enterprise Edition](https://www.jetbrains.com/idea/download/) is the recommended IDE to use.

## Getting started

- Clone this repository
- Open the project in IntelliJ
- Ensure the Gradle JVM in Settings > Build, Execution, Deployment > Build Tools > Gradle is set to Java 17
- Open the module settings and set the project SDK to a Java 17 JVM
- Reload the gradle project from the Gradle tool window
- Run `Application.java`

## Inspecting the database

The project is setup to use an in memory H2 database. To inspect the database, open the H2 console at http://localhost:8080/h2-console. 
The JDBC URL is `jdbc:h2:mem:eventsourcingdemo` and the username is `sa` with no password.