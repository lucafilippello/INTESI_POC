# INTESI_POC
Users Management App used to perform CRUD operations on project users

Two Sections have been implemented in this PoC:
- User section: exposes APIs to perform CRUD operations on project Users. Logs are written to console and to file (user-service.log)
  When a user is CREATED, UPDATED or DELETED an ActivityLogEvent is sent to RappitMQ exchange (activity-exchange) that writes it to a specific queue (activity-log-queue) with a specific routing key (activity.log)
  Specific queue is read by a consumer that "consumes" events and writes them to DB (table ACTIVITY_LOGS)
  API access and API responses format/content depend from user ROLE and permissions, as described in functional document provided.
- Logs section: API to READ db logs (table ACTIVITY_LOGS). Only for ADMIN users
- All API access is granted ONLY if Keycloak token is provided in request ("Authorization" header)
- APIs tests have beeen performed using Postman to retrieve Keyclock token, given the informations provided (IDP address, Client Id, Client Secret, users credentials)
  and injecting retrieved token in requests header
- Swagger documentation is accessible at http://localhost:8080/project-user-manager/swagger-ui/index.html


# System requirements
1. JDK 17
2. Docker Desktop (on Windows)

# Framework/Technologies used
Spring Boot Framework
	Spring Security
	Spring Data
RabbitMQ
Lombok
JUnit,Mockito
Swagger
Mapstruct
Databases: PostgreSQL,H2
maven
Docker (for running PostgreSQL instance and rabbit message broker instance)
POSTMAN (used to test API and retrieve Keycloak token)

# How to run application

1. Clone repository from: https://github.com/lucafilippello/INTESI_POC.git

2. Execute RABBITMQ container: 
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management

3. Execute POSTGRESQL container (only if application is started with "ps" pmaven profile): 
docker run -d --name postgres-userservice -e POSTGRES_DB=mydb -e POSTGRES_USER=adminps -e POSTGRES_PASSWORD=adminps -p 5432:5432 -v postgres-data:/var/lib/postgresql/data postgres:16

4. Start SpringBoot app (from command line)
From <MY_CLON_PATH>/user-service, execute
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=h2" --> for H2 db usage
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=ps" --> for DB Postgresql usage

Useful links
http://localhost:15672 --> RABBIT console (guest/guest)
http://localhost:8080/project-user-manager/h2-console --> console db h2
jdbc:h2:mem:userdb;
User:sa
pwd:
