# Ecuadorian Semantic Repository  #

Ecuadorian Semantic Repository of Ecuadorian Academics for the discovery of common research areas. The platform is based on the Apache Marmotta Linked Data Platform.

### What is this repository for? ###

* Quick summary
* Version
* [Learn Markdown](https://bitbucket.org/tutorials/markdowndemo)

### How do I get set up? ###

* System requirements
1. Java JRE 1.7.0_45 or superior
2. Maven 3
3. 1GB main memory
4. Java Application Server (Tomcat 7.x or Jetty 6.x)
5. Database engine(PostgreSQL, MySQL - if not explicitly configured, an embedded H2 database will be used)

* **Summary of set up**

Download, fork or clone the repository from the [Repo URL](https://santteegt@bitbucket.org/ecsemanticrep/watiqay-khuska-ecuadorian-semantic-repository-for-researchers.git)

* Open a Terminal window and Go to the repository folder ***${wkhuska.home}***
* Execute ** mvn package install* (If you need a fast deplyment you can use the parameter ***-DskipTests=true***)
* The deployed application will be installed In the directory *${wkhuska.home}/webapp/target/marmotta.war*
* If you need to run the application for testing purposes you can go to the directory *${wkhuska.home}/webapp* and execute from a terminal window ***mvn tomcat7:run***


* Configuration
* Dependencies
* Database configuration
* How to run tests
* Deployment instructions

### Contribution guidelines ###

* Writing tests
* Code review
* Other guidelines

### Who do I talk to? ###

* Repo owner or admin
* Other community or team contact