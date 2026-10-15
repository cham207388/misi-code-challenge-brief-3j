# ADR-001: Java 25 / Spring Boot 4.1 / React 19 instead of .NET 8

## Status
Accepted for this fictional exercise.

## Context
Brief 3 prefers ASP.NET Core / .NET 8 and says a different stack is allowed if documented and independently runnable.

## Decision
Build a modular monolith:

- React 19 + TypeScript + Vite, served from the Spring Boot jar
- Java 25 + Spring Boot 4.1 + Gradle 9 with a version catalog
- PostgreSQL 17, Flyway, Spring Security sessions
- Docker Compose locally; Terraform + ECS Fargate in AWS us-east-1

## Consequences
Evaluators need JDK 25 or Docker rather than the .NET SDK. The product contract (26 pages, roles, workflow, seven mocks) is unchanged.
