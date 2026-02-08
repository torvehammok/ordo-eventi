---
sidebar_position: 11
---
# Community & Contribution

We welcome contributions to Ordo Eventi!

## Contribution Guide

### 1. Set up Development Environment
- Clone the repository.
- Run `./gradlew build` to ensure everything is working and code is generated from schemas.
- Use an IDE with Kotlin support (IntelliJ IDEA is recommended).

### 2. Running Tests
We use **Testcontainers** to run integration tests against a real Kafka instance. Ensure Docker is running before executing:

```bash
./gradlew test
```

### 3. Submitting Pull Requests
- Create a feature branch.
- Follow the coding standards in `AGENTS.md`.
- Ensure all tests pass.
- Provide a clear description of the changes in your PR.

## Roadmap

Future improvements planned for Ordo Eventi:

- [ ] **Validation Layer**: Add a plugin system for custom schema validation (e.g., naming conventions, mandatory fields).
- [ ] **Multi-Registry Support**: Manage schemas across multiple Schema Registry clusters in a single config.
- [ ] **Web UI**: A read-only dashboard to visualize the plan before applying.
- [ ] **Helm Chart**: Simplify deployment of Ordo Eventi as a CronJob in Kubernetes.

## Getting Help
- **GitHub Issues**: For bug reports and feature requests.
- **Discussions**: For general questions and architectural advice.
