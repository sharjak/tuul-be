## Tuul test API project

## Project Overview

This project is built with:
- **Java 17**
- **Gradle 8.11.1**
- **Spring Boot 3.4.2**

It follows **Hexagonal Architecture**, which separates business logic from external concerns, making the system more modular and maintainable.

## Setup

1. Build the project:
   ```sh
   ./gradlew clean build
   ```
2. Set up application-dev.yaml based on application-dev.sample.yaml
3. Run the application:
   ```sh
   ./gradlew bootRun
   ```
4. Access Swagger UI:
   ```
   http://localhost:8080/tuul-api/swagger-ui.html
   ```
5. Check integration-test module readme to run integration tests
6. Generate test coverage report:
   ```sh
   ./gradlew jacocoRootReport
   ```

