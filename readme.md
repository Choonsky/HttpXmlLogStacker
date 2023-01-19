# HttpXmlServerApp logs analyzer application 

## Table of contents

* [General info](#general-info)
* [Technologies](#technologies)
* [Setup](#setup)

## General info

This application analyzes logs made by HttpXmlServerApp (HTTP server that accepts POST requests 
with XML content, parses XML to JSON format and saves it in log files according to Type field
including count) and saves them to new files by 100 entries in each.

## Technologies

Project is created with:

* Java 19 (Oracle OpenJDK)
* Maven 3.8.1
* Java NIO

## Setup

To test this project locally:

* Clone this project using "git clone https://github.com/Choonsky/HttpXmlLogStacker"
* Navigate to the program folder ("cd HttpXmlLogStacker")
* Make a jar using "mvn package"
* Run a jar using "java -cp target/HttpXmlLogStacker-1.0-SNAPSHOT.jar org.nemirovsky.HttpXmlLogStacker"

* Open working directory (like C:\Users\_user_name_\output\stacked) and observe result.