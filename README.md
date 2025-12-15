# CI/CD Dashboard API (WIP)

## 📋 Description

REST API built with Spring Boot, design to manage projects, their versions and their CI/CD pipelines and jobs. 

It receives webhooks from GitLab (only Gitlab for now) to track pipeline and job events, storing relevant data in a PostgreSQL database. The API provides endpoints to query this data, enabling integration with frontend dashboards or other services.

A dashboard frontend is under development to visualize the collected data. You can find it **[here](https://github.com/iandeveseleer/cicd-dashboard)**

## 🚀 Features

- **GitLab Webhook Reception**: Dedicated endpoint to receive pipeline and build events
- **Event Processing**: Automatic parsing and processing of GitLab events
- **Data Storage**: Persistence of project, version, pipeline, and job information
- **REST API**: Endpoints to query data with Spring Data REST
- **API Documentation**: Integrated Swagger UI interface
- **HAL Explorer**: Interactive REST API navigation

## 🏗️ Architecture

### Data Model

- **Project**: Represents a project with its name, URL, and repository ID
- **ProjectVersion**: Specific versions of a project (branches, tags)
- **Pipeline**: CI/CD pipelines with their status, dates, and commit SHA
- **Job**: Individual pipeline jobs with their details and results
- **Team**: Team management

### Main Components

- **GitLabWebhookController**: `/webhooks/gitlab` endpoint to receive events
- **GitLabEventProcessor**: Event processing orchestrator
- **GitLabPipelineEventProcessor**: Pipeline event specific processing
- **GitLabJobEventProcessor**: Build/job event specific processing
- **Repositories**: Data access layer with Spring Data JPA

## 🛠️ Technologies

- **Java 21**
- **Spring Boot**
  - Spring Data JPA
  - Spring Data REST
  - Spring Boot Validation
- **PostgreSQL**: Database
- **GitLab4J API 6.2.0**: Library for GitLab webhooks
- **Flyway**: Database migration management
- **Testcontainers**: Integration testing with containers

## 📦 Prerequisites

- Java 21 or higher
- Maven 3.6+
- Docker and Docker Compose (for the database)
- GitLab (for sending webhooks)

## 🔧 Installation and Configuration

### 1. Clone the project

```bash
git clone <repository-url>
cd cicd-dashboard-api
```

### 2. Start the database

```bash
cd db
docker-compose up -d
```

The PostgreSQL database will be accessible on port `15432`.

### 3. Application configuration

The `application.yml` file contains the default configuration:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:15432/cicd-dashboard
    username: cicd-dashboard
    password: cicd-dashboard
```

For production, use `application_prod.yml` with secure credentials.

### 4. Build and run the application

```bash
# With Maven Wrapper (recommended)
./mvnw clean install
./mvnw spring-boot:run

# Or with installed Maven
mvn clean install
mvn spring-boot:run
```

The application starts on `http://localhost:8080`

## 📚 API Documentation

Once the application is running, access:

- **Swagger UI**: http://localhost:8080/api-docs
- **HAL Explorer**: http://localhost:8080

## 🔗 GitLab Webhook Configuration

To receive events from GitLab:

1. Access your GitLab project
2. Go to **Settings** > **Webhooks**
3. Configure the URL: `http://<your-server>:8080/webhooks/gitlab`
4. Select the triggers:
   - ✅ Pipeline events
   - ✅ Job events
5. Save the webhook

## 📊 Database Structure

```
projects
├── id
├── name
├── repository_url
└── repository_id

project_versions
├── id
├── project_id (FK)
└── version

pipelines
├── id
├── ci_id
├── project_version_id (FK)
├── status
├── sha1
├── previous_sha1
├── changes_url
├── url
├── created_date
└── end_date

jobs
├── id
├── ci_id
├── name
├── pipeline_id (FK)
├── status
├── details_id (FK)
├── start_date
├── end_date
└── logs_url
```

## 🧪 Tests

```bash
# Run all tests
./mvnw test

# Run integration tests with Testcontainers
./mvnw verify
```

## 🐳 Docker Deployment

### PostgreSQL Database

```bash
cd db
docker-compose up -d
```

### Environment Variables

Create a `.env` file in the `db/` directory:

```env
POSTGRES_DB=cicd-dashboard
POSTGRES_USER=cicd-dashboard
POSTGRES_PASSWORD=your-secure-password
```

## 📝 Main Endpoints

### Webhook
- `POST /webhooks/gitlab`: Receive GitLab events

### REST API (Spring Data REST)
- `GET /projects`: List of projects
- `GET /projects/{id}`: Project details
- `GET /pipelines`: List of pipelines
- `GET /pipelines/{id}`: Pipeline details
- `GET /jobs`: List of jobs
- `GET /jobs/{id}`: Job details

## 🔒 Security

⚠️ **Important note**: This version does not include authentication on webhooks. For a production environment, it is recommended to add:

- GitLab secret token validation
- Authentication on API endpoints
- Mandatory HTTPS
- IP filtering if possible

## 🤝 Contributing

Contributions are welcome! Feel free to open an issue or pull request.

## 📄 License

This project is under MIT license.

## 👤 Author

Ian Deveseleer


