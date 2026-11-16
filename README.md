# DECODER API - Building Energy Management System

A Spring Boot REST API for building energy monitoring, forecasting, and management with role-based access control (RBAC).

##  Features

- Role-Based Access Control (RBAC)
- Energy forecasting with 120-minutes moving average
- ASHRAE comfort standards compliance
- Comprehensive testing (19/19 tests passing)
- REST API with Spring Security
- H2 SQL database (file-based)

Architecture decisions are documented in [`docs/`](docs/).

---
## Installation

### Prerequisites
- Java 17+
- Maven 3.8+ (or use the included Maven wrapper)

### Run

```bash
git clone https://github.com/jbellocode/decoder-api.git
cd decoder-api
./mvnw spring-boot:run
```

Server runs on: http://localhost:8080

The H2 database file is created in `data/` on first run, and its tables are recreated on each start. The H2 console is available at http://localhost:8080/h2-console.

## Architecture

REST Controllers (HTTP Layer): ReadingController, ForecastController, RBAC Authorization (X-User-Role header)

Service Layer (Business Logic): QueryService, ForecastService, IngestService

Repository Layer (Data Access): ReadingRepository

Database Layer (H2 SQL database): buildings and readings tables

## API Endpoints

### 1. Get Last Readings
Endpoint: GET /api/buildings/{id}/last-readings?minutes=60
Authorization: Admin (all buildings) | Owner (own buildings only)
Example: curl -H "X-User-Role: admin" http://localhost:8080/api/buildings/B001/last-readings
Response (HTTP 204): No content

### 2. Get Energy Forecast
Endpoint: GET /api/buildings/{id}/forecast?minutes=60
Authorization: Admin (all buildings) | Owner (own buildings only)
Example: curl -H "X-User-Role: admin" http://localhost:8080/api/buildings/B001/forecast
Response (HTTP 200): JSON with buildingId, forecastMinutes, predictedValue, recommendation
Recommendation Logic: If average reading <= 70% ASHRAE threshold: "Normal operation", If average reading > 70%: "Activate energy-saving mode"

## RBAC Authorization

Request Header: X-User-Role: [admin|owner]

| Role | Access | Buildings |
|------|--------|-----------|
| admin | Full read/write | All buildings |
| owner | Restricted read | Own buildings only (B001) |
| guest (default) | Denied | None - returns 403 |

## Testing

Run All Tests (19 total): ./mvnw clean test

Test Coverage: 9 Integration Tests, 4 Unit Tests QueryService, 3 Unit Tests ForecastService, 2 Unit Tests IngestService, 1 Spring Boot Test

Result: Tests run: 19, Failures: 0, Errors: 0, Skipped: 0, BUILD SUCCESS

## Key Features

- Role-Based Access Control
- Moving Average Forecasting
- ASHRAE Comfort Standards
- Comprehensive Testing
- Error Handling
- Spring Boot Integration

## License

MIT
