# CSV-Filereader

_A service for loading information from CSV files into database._

## Getting Started

### Prerequisites

- **Java 25 or higher**
- **Maven**
- **MariaDB**(if applicable)
- **Git**
- **[Dependent Microservices](#dependencies)** (if applicable)

### Installation

1. **Clone the repository:**

   ```bash
   git clone https://github.com/Public-Service-as-a-Service/csv-filereader.git
   cd csv-filereader
   ```
2. **Configure the application:**

   Before running the application, you need to set up configuration settings.
   See [Configuration](#Configuration)

   **Note:** Ensure all required configurations are set; otherwise, the application may fail to start.

3. **Ensure dependent services are running:**

   If this microservice depends on other services, make sure they are up and accessible. See [Dependencies](#dependencies) for more details.

4. **Build and run the application:**

   - Using Maven:

     ```bash
     mvn spring-boot:run
     ```
   - Using Gradle:

     ```bash
     gradle bootRun
     ```

## Dependencies

This service has no dependencies to other microservices to run but does need the database to be initialized before start.

## API Documentation

Access the API documentation via Swagger UI:

- **Swagger UI:** [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

Alternatively, refer to the `openapi.yml` file located in the project's root directory for the OpenAPI specification.

## Usage

### API Endpoints

Refer to the [API Documentation](#api-documentation) for detailed information on available endpoints.

### Example Request

```bash
curl -X GET http://localhost:8080/api/resource
```

## Configuration

Configuration is crucial for the application to run successfully. Ensure all necessary settings are configured in `application.yml`.

### Key Configuration Parameters

- **Server Port:**

  ```yaml
  server:
    port: 8080
  ```
- **Database Settings:**

  ```yaml
  spring:
    datasource:
      url: jdbc:mysql://localhost:3306/your_database
      username: your_db_username
      password: your_db_password
  ```
- **Import Settings:**

  ```yaml
    import:
      enabled: true

      #file locations
      incoming-dir: ./files/incoming
      processed-dir: ./files/processed
      failed-dir: ./files/failed
      file-source-dir: ./files

      #Filenmes
      org-file-name: organization_file.csv
      emp-file-name: employee_file.csv

      #Bathcsize
      employee-batch-size: ${EMPLOYEE_BATCH_SIZE}
      organization-batch-size: ${ORGANIZATION_BATCH_SIZE}
  ```

  - **File locations:** locations of directories in which files can be placed or found place at different stages of operation.
  - **Filenames:** names of the files which are used.
  - **Batchsize:** sizes of batches that are loaded into database.
- **Scheduling Settings:**

  ```yaml
   scheduler:
    scheduled-org-import:
     cron: "* * * * * *"
     name: "org-import"
     shedlock-lock-at-most-for: "PT2H"
     maximum-execution-time: "PT2H"

    scheduled-emp-import:
     cron: "* * * * * *"
     name: "emp-import"
     shedlock-lock-at-most-for: "PT2H"
     maximum-execution-time: "PT2H"
  ```

  - **Cron:** expression of the time the task is scheduled to run on.

### Database Initialization

Make sure api-service-notifier is run at least once to initialize the database to which csv-filereader is supposed to load the data into.

```yaml
spring:
  flyway:
    enabled: false
```

- **api-service-notifier:**
  - **Repository:** [Link to the repository](https://github.com/Public-Service-as-a-Service/api-service-notifier)
  - **Setup Instructions:** Refer to its documentation for installation and configuration steps.

### Additional Notes

- **Application Profiles:**

  Use Spring profiles (`dev`, `prod`, etc.) to manage different configurations for different environments.

- **Logging Configuration:**

  Adjust logging levels if necessary.

## Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](https://github.com/Sundsvallskommun/.github/blob/main/.github/CONTRIBUTING.md) for guidelines.

## License

This project is licensed under the [MIT License](LICENSE).

## Code status

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_YOUR-PROJECT-ID&metric=alert_status)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_YOUR-PROJECT-ID)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_YOUR-PROJECT-ID&metric=reliability_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_YOUR-PROJECT-ID)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_YOUR-PROJECT-ID&metric=security_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_YOUR-PROJECT-ID)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_YOUR-PROJECT-ID&metric=sqale_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_YOUR-PROJECT-ID)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_YOUR-PROJECT-ID&metric=vulnerabilities)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_YOUR-PROJECT-ID)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_YOUR-PROJECT-ID&metric=bugs)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_YOUR-PROJECT-ID)

---

© 2024 Sundsvalls kommun
