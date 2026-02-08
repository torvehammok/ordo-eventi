# AGENTS.md

Welcome to the **ordo-eventi** repository. This document provides essential information for AI agents and developers to
work effectively in this codebase.

## Project Overview

A Kotlin-based tool for managing Kafka topics and schemas (Avro/Protobuf) using a GitOps-like approach. It interacts
with Kafka brokers and Confluent Schema Registry.

## Build and Test Commands

This project uses Gradle with the Kotlin DSL.

| Task                        | Command                                                             |
|:----------------------------|:--------------------------------------------------------------------|
| **Build project**           | `./gradlew build`                                                   |
| **Clean and build**         | `./gradlew clean build`                                             |
| **Run all tests**           | `./gradlew test`                                                    |
| **Run a single test**       | `./gradlew test --tests "fully.qualified.ClassName.testMethodName"` |
| **Run specific test class** | `./gradlew test --tests "fully.qualified.ClassName"`                |
| **Topic Plan**              | `./gradlew topics-plan`                                             |
| **Topic Apply**             | `./gradlew topics-apply`                                            |
| **Schema Plan**             | `./gradlew schemas-plan`                                            |
| **Schema Apply**            | `./gradlew schemas-apply`                                           |
| **Schema Tree**             | `./gradlew schemas-tree`                                            |
| **Schema Destroy**          | `./gradlew schemas-destroy` (Destructive!)                          |
| **Bootstrap All**           | `./gradlew bootstrap-all`                                           |

## Code Style Guidelines

### 1. Language & Environment

- **Language:** Kotlin 2.x
- **JVM Target:** 21
- **Dependency Management:** Gradle (Kotlin DSL in `build.gradle.kts`)

### 2. Naming Conventions

- **Packages:** `io.github.torvehammok.*`
- **Classes/Interfaces/Objects:** `PascalCase` (e.g., `TopicService`, `RegistryClient`)
- **Functions/Variables:** `camelCase` (e.g., `planTopicUpdate`, `schemaDefinition`)
- **Constants:** `SCREAMING_SNAKE_CASE` (though often handled as `private val` in Kotlin)

### 3. Imports

- Avoid wildcard imports (e.g., `import x.y.*`).
- Maintain a clean import structure:
    1. Standard Java/Kotlin library imports
    2. Third-party library imports
    3. Project-specific imports

### 4. Code Structure

- **Domain Logic:** Located in `src/main/kotlin/io/github/torvehammok/domain/`. Focus on business rules, independent of
  infrastructure.
- **Infrastructure:** Implementation details (Kafka clients, Schema Registry, etc.) reside in
  `src/main/kotlin/io/github/torvehammok/infra/`.
- **CLI:** Command-line interface definitions using Picocli are in `src/main/kotlin/io/github/torvehammok/cli/`.

### 5. Formatting & Style

- Use 4-space indentation.
- Use `trimIndent()` for multi-line strings, especially for logging or configuration blocks.
- Prefer explicit return types for public functions.
- Keep classes focused (Single Responsibility Principle).

### 6. Logging

- Use SLF4J with Logback.
- Declare the logger at the top of the file:
  ```kotlin
  private val log = LoggerFactory.getLogger(YourClassName::class.java)
  ```
- Use placeholders for variables in logs: `log.info("Processing subject: {}", subject)`.

### 7. Error Handling

- Use standard Kotlin exceptions for error conditions.
- Log errors with appropriate levels (`log.warn` for recoverable issues, `log.error` for critical failures).
- Avoid swallowing exceptions; either rethrow or log and handle gracefully.

### 8. Testing Patterns

- **Unit Tests:** Use JUnit 5 and AssertJ for assertions.
- **Integration Tests:** The project uses Testcontainers (Kafka) for integration testing. Check `src/test/kotlin` for
  examples.
- **Generated Code:** Tests often rely on code generated from Avro/Protobuf. Run `./gradlew build` to ensure these are
  generated before running tests from an IDE.

### 9. Configuration

- Application configuration is primarily managed via YAML files (e.g., `configmap.yaml`).
- Dotenv is used for environment variable management (`.env` file).
- **Environment Variable Placeholders:** The project supports placeholders in the format `$env:VAR_NAME` within YAML
  configuration files. These are resolved at runtime.

### 10. Schemas (Avro/Protobuf)

- Schemas are defined in `src/test/avro` or `src/test/proto` for testing.
- Generated code from schemas is placed in `build/generated`. Do not edit these files manually.

## Key Design Patterns & Features

- **GitOps Approach:** The tool is designed to synchronize the desired state defined in configuration files with the
  actual state in Kafka/Schema Registry.
- **Pluggable Strategies:** Discovery of schemas and topics uses strategy patterns (e.g., `SchemasDiscoveryStrategy`).
- **Dry-run Capability:** All major operations have a "plan" version to preview changes before applying them.
- **Context Injection:** A `Ctx` (Context) object is often used to pass dependencies like configuration, environment,
  and clients throughout the application.

## Contribution Workflow

1. **Understand:** Read the relevant domain and infra files.
2. **Implement:** Follow the established Kotlin patterns.
3. **Verify:** Run `./gradlew build` and relevant tests.
4. **Lint:** Ensure code matches existing style (no specific linter plugin is currently active, so match surrounding
   code).
5. **CLI Verification:** Test changes using the built-in Gradle tasks like `topics-plan` or `schemas-plan`.

## Documentation Website

The project documentation is built with Docusaurus and located in the `website/` directory.

### Commands

| Task | Command |
| :--- | :--- |
| **Start local server** | `cd website && bun start` |
| **Build for production** | `cd website && bun run build` |

### Structure

- **Docs Content:** `website/docs/` contains the markdown files for the documentation pages.
- **Configuration:** `website/docusaurus.config.ts` handles site configuration (title, navigation, etc.).
- **Homepage:** `website/src/pages/index.tsx` is the React component for the landing page.
- **Static Assets:** `website/static/img/` contains images like the logo.
