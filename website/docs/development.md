---
sidebar_position: 9
---
# Development

This page covers the internal architecture of Ordo Eventi and provides guidance for developers looking to contribute to the project.

## Build and Test Commands

Ordo Eventi uses Gradle with the Kotlin DSL.

| Task | Command |
| :--- | :--- |
| **Build project** | `./gradlew build` |
| **Run all tests** | `./gradlew test` |
| **Run a single test** | `./gradlew test --tests "fully.qualified.ClassName.testMethodName"` |

## Internal Architecture

The codebase follows a clean architecture pattern to ensure maintainability and testability. It is organized into three primary layers:

### 1. CLI Layer (`cli/`)
- Uses **Picocli** for command-line parsing.
- Maps user input to domain service calls.
- Handles output formatting and logging.

### 2. Domain Layer (`domain/`)
- **Core Logic**: Contains the business logic for calculating diffs and planning updates.
- **Independence**: This layer is independent of the underlying Kafka or Schema Registry clients.
- **TopicService / SchemaService**: Orchestrates the planning and execution workflows.

### 3. Infrastructure Layer (`infra/`)
- **Implementations**: Contains concrete implementations for interacting with external systems.
- **KafkaAdmin**: Wraps the standard Kafka `AdminClient`.
- **ConfluentRegistry**: Interacts with the Confluent Schema Registry REST API.
- **Config**: Logic for reading YAML files and resolving environment variables.

## Extending the Tool

Ordo Eventi is built with extensibility in mind using strategy patterns.

### Adding New Discovery Strategies
If you store your schemas in a non-standard way (e.g., in a database or fetched via HTTP), you can implement the `SchemasDiscoveryStrategy` interface.

### Customizing Topic Operations
The `TopicOps` interface allows for swapping the mechanism by which topics are created or updated, which is useful for specialized environments or mocking in tests.

## Code Style & Standards

Contributors should adhere to the standards defined in the `AGENTS.md` file located in the project root. Key points include:
- Kotlin 2.x with functional patterns.
- Explicit return types for public functions.
- SLF4J for logging.
- AssertJ for testing assertions.
