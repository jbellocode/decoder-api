# ADR-001: Service Layer Boundary - Separate Business Logic from Controllers

Status: Accepted | Date: 2025-10-30

## Context
The DECODER API must handle three distinct responsibilities: HTTP Request/Response, Business Logic, and Data Access. Mixing all three makes code untestable and unmaintainable.

## Decision
Implement a strict Service Layer Boundary: Controllers handle ONLY HTTP concerns. Services contain ALL business logic. Repositories handle ONLY data access.

## Benefits
- Services testable without Spring context
- Controllers testable with MockMvc
- Business logic independent of HTTP framework
- Clear separation of concerns

## Consequences
- More files/classes to manage
- Need DTO objects
- Service methods must be idempotent
