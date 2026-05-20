# INTESI_POC
Users Management App used to perform CRUD operations on project users

# Framework/Technologies used
Spring Boot Framework
	Spring Security
	Spring Data
RabbitMQ
Lombok
JUnit,Mockito
Mapstruct
Databases: PostgreSQL,H2
maven
Docker (for running PostgreSQL instance and rabbit message broker instance)
POSTMAN 

# How to run application

1. Clone repository from: https://github.com/lucafilippello/INTESI_POC.git

2. Execute RABBITMQ container: 
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management

3. Execute POSTGRESQL container (only if application is started with "ps" pmaven profile): 
docker run -d --name postgres-userservice -e POSTGRES_DB=mydb -e POSTGRES_USER=adminps -e POSTGRES_PASSWORD=adminps -p 5433:5433 -v postgres-data:/var/lib/postgresql/data postgres:16

4. Start SpringBoot app
From <MY_CLON_PATH>/user-service
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=h2" --> db H2
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=ps" --> DB Postgresql

Useful links
http://localhost:8080/project-user-manager/swagger-ui/index.html --> SWAGGER documentation
http://localhost:15672 --> RABBIT console (guest/guest)
http://localhost:8080/project-user-manager/h2-console --> console db h2
jdbc:h2:mem:userdb;
User:sa
pwd:
