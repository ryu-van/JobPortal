# Job Portal Backend

## 1. Project Basic Information

### Project Name
Job Portal Backend - Comprehensive job search system

### Description
This is the backend for a job portal system built with Spring Boot, providing APIs for managing users, companies, job postings, applications, and related functionalities. The project supports multiple user roles and offers professional search, filtering, and content management features.

### Badges
[![Java Version](https://img.shields.io/badge/java-17%2B-blue.svg)](https://jdk.java.net/17/)
[![Spring Boot](https://img.shields.io/badge/spring%20boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/postgresql-14%2B-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/redis-7.x-red.svg)](https://redis.io/)
[![Maven](https://img.shields.io/badge/maven-3.8%2B-yellow.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

### Version
**Current Version**: 1.0.0

## 2. Technology & Dependencies

### Core Framework
- **Spring Boot 3.x** - Main web framework
- **Spring Data JPA** - ORM and data management
- **Spring Security** - Security and authentication

### Database
- **PostgreSQL 14+** - Main relational database
- **Redis 7.x** - Caching and state management (love this for performance!)

### Additional Libraries
- **Lombok** - Reduces boilerplate code (one of my favorites!)
- **JWT (JSON Web Token)** - User authentication
- **MapStruct** - DTO to Entity conversion
- **Flyway** - Database migration management
- **Swagger/OpenAPI** - API documentation (auto-generated!)
- **SLF4J + Logback** - Logging
- **Spring Boot Actuator** - Monitoring and metrics

### Development Tools
- **Maven** - Build and dependency management
- **Docker** - Containerization
- **GitHub Actions** - CI/CD pipeline

## 3. Project Structure

```
src/main/java/com/example/jobportal/
├── config/                      # Application configuration
│   ├── AsyncConfig.java         # Async task configuration
│   ├── CloudinaryConfig.java    # Cloudinary configuration (file upload)
│   ├── CorsConfig.java          # CORS configuration
│   ├── RedisConfig.java         # Redis configuration
│   └── SecurityConfig.java      # Security configuration
├── controller/                  # REST API controllers
│   ├── ApplicationController.java
│   ├── AuthController.java
│   ├── CompanyController.java
│   ├── JobController.java
│   └── UserController.java
├── converter/                   # Enum to database converters
│   ├── CompanyVerificationStatusConverter.java
│   └── JobStatusConverter.java
├── dto/                         # Data Transfer Objects
│   ├── request/                 # Request DTOs
│   │   ├── AddressRequest.java
│   │   ├── JobRequest.java
│   │   └── UserRequest.java
│   └── response/                # Response DTOs
│       ├── AddressResponse.java
│       ├── ApiResponse.java
│       └── JobBaseResponse.java
├── exception/                   # Exception handling
│   ├── GlobalExceptionHandler.java
│   ├── JobException.java
│   └── UserException.java
├── model/                       # Entities and enums
│   ├── entity/                  # JPA Entities
│   │   ├── Address.java
│   │   ├── Company.java
│   │   ├── Job.java
│   │   └── User.java
│   └── enums/                   # Enum definitions
│       ├── ApplicationStatus.java
│       ├── JobStatus.java
│       └── Role.java
├── repository/                  # JPA Repositories
│   ├── ApplicationRepository.java
│   ├── CompanyRepository.java
│   ├── JobRepository.java
│   └── UserRepository.java
├── scheduler/                   # Scheduled jobs
│   └── InvitationCleanupScheduler.java
├── security/                    # Security configuration
│   ├── CustomUserDetails.java
│   └── JwtAuthenticationFilter.java
├── service/                     # Business logic
│   ├── ApplicationService.java
│   ├── CompanyService.java
│   ├── JobService.java
│   ├── UserService.java
│   └── impl/                    # Service implementations
│       ├── ApplicationServiceImpl.java
│       └── JobServiceImpl.java
├── util/                        # Utilities
│   ├── FileNameUtils.java
│   └── PasswordEncoderConfig.java
└── JobPortalApplication.java    # Entry point
```

## 4. Installation Guide

### System Requirements
- Java 17 or higher
- Maven 3.8 or higher
- PostgreSQL 14 or higher
- Redis 7.x (if using caching)

### Step 1: Clone Repository
```bash
git clone https://github.com/ryu-van/JobPortal
cd jobportal-be
```

### Step 2: Configure Database
- Create PostgreSQL database:
  ```sql
  CREATE DATABASE jobportal;
  CREATE USER jobportal_user WITH PASSWORD 'your_password';
  GRANT ALL PRIVILEGES ON DATABASE jobportal TO jobportal_user;
  ```

### Step 3: Configure Application File
Edit the `src/main/resources/application.yml` file with your configuration:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/jobportal
    username: jobportal_user
    password: your_password
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
  redis:
    host: localhost
    port: 6379
```

### Step 4: Build the Project
```bash
./mvnw clean package -DskipTests
```

### Step 5: Run the Application

#### Run Directly
```bash
./mvnw spring-boot:run
```

#### Run from JAR File
```bash
java -jar target/jobportal-be-1.0.0.jar
```

#### Run with Docker
```bash
docker build -t jobportal-be .
docker run -p 8080:8080 --env-file .env jobportal-be
```

## 5. Configuration

### Configuration Profiles
- **default**: Default configuration for development environment
- **docker**: Configuration for Docker environment

### Important Environment Variables
| Variable Name | Description | Default Value |
|---------------|-------------|---------------|
| `DB_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/jobportal` |
| `DB_USERNAME` | PostgreSQL username | `jobportal_user` |
| `DB_PASSWORD` | PostgreSQL password | `your_password` |
| `REDIS_HOST` | Redis address | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `JWT_SECRET` | JWT secret key | `your_jwt_secret` |
| `JWT_EXPIRATION` | JWT expiration time (milliseconds) | `86400000` (24 hours) |

## 6. API Documentation

### Access API Documentation
After running the application, you can access the API documentation at:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### Standard Response Format
All APIs return the `ApiResponse` format:
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {...},
  "pageInfo": {...} 
}
```

### Main Endpoints

#### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login
- `POST /api/auth/refresh-token` - Refresh token

#### User
- `GET /api/users/{id}` - Get user details
- `PUT /api/users/{id}` - Update user information
- `GET /api/users/me` - Get current user information
- `PUT /api/users/{id}/avatar` - Update avatar

#### Company
- `GET /api/companies` - Get list of companies (paginated)
- `GET /api/companies/{id}` - Get company details
- `PUT /api/companies/{id}` - Update company information
- `POST /api/companies/requests` - Request company verification

#### Job Postings
- `GET /api/jobs` - Get list of job postings (with filtering and pagination)
- `POST /api/jobs` - Create new job posting
- `GET /api/jobs/{id}` - Get job posting details
- `PUT /api/jobs/{id}` - Update job posting
- `PUT /api/jobs/{id}/status` - Change job posting status

#### Applications
- `POST /api/applications` - Submit application
- `GET /api/applications/me` - Get list of user's applications
- `GET /api/applications/job/{jobId}` - Get list of applicants by job posting
- `PUT /api/applications/{id}/status` - Update application status

## 7. Testing

### Testing Strategy
- **Unit Tests**: Test individual logic (using JUnit 5 + Mockito)
- **Integration Tests**: Test interaction between components (using Spring Boot Test)
- **End-to-End Tests**: Test entire workflow (using REST Assured)

### Run Tests
```bash
# Run all tests
./mvnw test

# Run tests for a specific package
./mvnw test -Dtest=com.example.jobportal.service.impl.*Test

# Run tests with coverage
./mvnw jacoco:report
```

### Coverage Report
After running the tests, you can view the coverage report at `target/site/jacoco/index.html`

## 8. Security

### Authentication
- Uses JWT (JSON Web Token) for authentication
- Token is generated after successful login
- Token has an expiration time (24 hours by default)
- Supports refresh token to extend session

### Authorization
- Uses Spring Security with detailed role-based configuration
- User roles:
  - `USER` - Regular user
  - `HR` - Company HR
  - `ADMIN_COMPANY` - Company administrator
  - `ADMIN` - System administrator

### Security Rules
- Passwords are encrypted using BCrypt
- Uses HTTPS in production environment
- Validate all inputs
- Handle CORS correctly
- Limit login attempts
- Log all security activities

## 9. Database

### Main Schema
- **users**: User information
- **companies**: Company information
- **addresses**: Addresses (linked to users, companies, jobs)
- **jobs**: Job postings
- **job_categories**: Job categories
- **job_category_mapping**: Relationship between jobs and categories
- **applications**: Job applications
- **application_status_history**: Application status history
- **resumes**: Candidate resumes

### Migration Management
- Uses Flyway for database migration management
- Migration files are located in `src/main/resources/db/migration`
- Migration file naming format: `V{version}__{description}.sql`

### Important Indexes
- `users_email_idx` - Index for users.email column
- `jobs_title_idx` - Index for jobs.title column
- `applications_job_id_idx` - Index for applications.job_id column
- `applications_user_id_idx` - Index for applications.user_id column

## 10. Code Quality

### Code Standards
- Follows **Google Java Style Guide**
- Uses Lombok to reduce boilerplate code
- Writes comments for complex methods
- Uses meaningful variable names

### Static Analysis Tools
- **Checkstyle**: Checks code standards
- **PMD**: Analyzes source code for potential issues
- **Spotbugs**: Finds logical and security errors
- **SonarQube**: Comprehensive code quality management

### Code Review Process
- Each PR must be reviewed by at least 1 other member
- Uses GitHub Pull Request for code review management
- Checks coverage for new code
- Verifies code follows standards

## 11. CI/CD

### GitHub Actions Pipeline
- **Build**: Checks compilation and runs unit tests
- **Test**: Runs integration tests and generates coverage report
- **Code Quality**: Analyzes code with SonarQube
- **Docker Build**: Creates Docker image
- **Deploy**: Deploys to staging/production environment

### Main Stages
1. **Build**: `./mvnw clean package -DskipTests`
2. **Test**: `./mvnw test`
3. **Quality Check**: SonarQube analysis
4. **Docker Build**: `docker build -t jobportal-be .`
5. **Deploy**: Push image to Docker Hub và deploy

## 12. Performance

### Optimization
- **Caching**: Uses Redis to cache frequently accessed data
- **Pagination**: All APIs returning lists support pagination
- **Lazy Loading**: Uses lazy loading for JPA relationships
- **Batch Processing**: Processes large data using batches
- **Optimistic Locking**: Uses optimistic locking for entities prone to conflicts

### Benchmarks
- Uses JMeter for API benchmarking
- Target: response time < 200ms for 95% of requests
- Maximum 1000 concurrent users

### Scalability
- **Horizontal Scaling**: Supports deployment of multiple instances
- **Stateless**: Application doesn't store state between requests
- **Database Sharding**: Prepared for database sharding when data is large

## 13. Monitoring & Logging

### Spring Boot Actuator
- Access monitoring endpoints at `http://localhost:8080/actuator`
- Main endpoints:
  - `/actuator/health` - Check application health
  - `/actuator/metrics` - Performance metrics
  - `/actuator/prometheus` - Metrics for Prometheus

### Logging
- Uses SLF4J with Logback
- Configure log level in `application.yml`
- Logs are categorized by component
- Supports logging to file and console

### Monitoring Tools
- **Prometheus**: Collect metrics
- **Grafana**: Visualize metrics
- **ELK Stack**: Collect and analyze logs (Elasticsearch, Logstash, Kibana)

## 14. Troubleshooting

### Common Issues

#### 1. Database Connection Error
- Check configuration in `application.yml`
- Ensure PostgreSQL is running
- Check database user access permissions

#### 2. Redis Error
- Ensure Redis is running
- Check host and port configuration
- Check if firewall allows Redis connections

#### 3. JWT Error
- Check secret key in configuration
- Ensure token is not expired
- Check token format

#### 4. Authorization Error
- Check user role
- Check security configuration
- Verify token contains correct permissions

### Debug Tools
- **Spring Boot DevTools**: Automatic reload when code changes
- **Log Levels**: Change log level for more detailed debugging
- **Swagger UI**: Test APIs directly
- **PostgreSQL logs**: View PostgreSQL logs to debug database errors

## 15. Additional Documentation

### System Architecture
- **Class Model**: Diagrams of entities and relationships between them
- **Data Flow**: Diagrams of data flow in main features
- **Components**: Detailed description of each system component

### Usage Guide
- **Detailed Installation Guide**: Documentation for DevOps
- **Development Guide**: Documentation for new developers joining the project
- **API Usage Guide**: Detailed documentation for frontend developers

### Blog & Articles
- **Project News**: Updates on new features
- **Technical Knowledge**: Articles about techniques used in the project

## 16. Contribution

### Contribution Guide
1. Fork repository
2. Create new branch: `git checkout -b feature/your-feature-name`
3. Write code and tests
4. Run tests: `./mvnw test`
5. Commit changes: `git commit -m "Add your feature description"`
6. Push to branch: `git push origin feature/your-feature-name`
7. Create Pull Request

### Commit Rules
- Use English language
- Write clear commit messages that accurately describe changes
- Use prefixes for commit messages:
  - `feat`: Add new feature
  - `fix`: Fix bug
  - `refactor`: Refactor code
  - `docs`: Update documentation
  - `test`: Add or modify tests
  - `chore`: Change configuration, build, etc.

### Branch Rules
- `main`: Main branch, contains stable code
- `develop`: Development branch, integrates feature branches
- `feature/`: Branch for new features
- `bugfix/`: Branch for bug fixes
- `release/`: Branch for preparing releases

## 17. Team & Contacts

### Team Structure
- **Product Owner**: Manages product backlog and requirements
- **Scrum Master**: Manages development process
- **Backend Developers**: Develop backend APIs
- **Frontend Developers**: Develop user interface
- **DevOps**: Manage environment and CI/CD
- **QA Engineers**: Test quality

### Contact Channels
- **Slack**: Team communication
- **Jira**: Manage tasks and issues
- **GitHub Issues**: Track bugs and feature requests
- **Email**: contact@jobportal.com

## 18. Legal & Compliance

### License
The project is distributed under the MIT license. See the [LICENSE](LICENSE) file for details.

### Privacy
- Complies with GDPR and data protection regulations
- Does not share user data with third parties
- Encrypts sensitive data

### Copyright
- © 2026 Job Portal. All rights reserved.
- Do not copy, distribute, or modify without written permission.

## 19. Versioning & Releases

### Versioning Rules
The project uses **Semantic Versioning (SemVer)**:
- **Major Version**: Incompatible changes
- **Minor Version**: New features, backward compatible
- **Patch Version**: Bug fixes, backward compatible

### Changelog
Detailed changes are maintained in the [CHANGELOG.md](CHANGELOG.md) file.

### Release Process
1. Create a release branch from develop
2. Ensure all tests pass ✅
3. Update version and changelog
4. Merge into main
5. Create a release tag
6. Deploy to production 🚀
7. Merge back into develop

## 20. Project Management

### Issue Management
- **GitHub Issues**: Track bugs, feature requests, and tasks
- **Jira**: Manage sprints and backlog (my go-to for agile workflow!)

### Roadmap
- **Q1 2026**: Develop core features (user, company, job, application)
- **Q2 2026**: Add advanced features (search, recommendation, analytics)
- **Q3 2026**: Implement AI matching between jobs and candidates
- **Q4 2026**: Optimize performance and scale the system

### Priorities
1. **Security**: Ensure system safety 🔒
2. **Performance**: Optimize response speed ⚡
3. **Usability**: Improve user experience
4. **Scalability**: Prepare for growth
5. **New Features**: Add new features according to roadmap

## 21. Reference Links

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Spring Data JPA Documentation](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Redis Documentation](https://redis.io/documentation)
- [Lombok Documentation](https://projectlombok.org/features/) (love this for reducing boilerplate!)
- [JWT Documentation](https://jwt.io/introduction/)

## 22. Last Update

**Last Updated**: 2026-01-11

---

