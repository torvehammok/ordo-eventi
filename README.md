# Ordo Eventi

**Ordo Eventi** is a declarative Infrastructure as Code (IaC) tool for managing Kafka topics and schema registry schemas
using YAML configuration files. It provides a GitOps-friendly approach to maintaining your event-driven architecture
infrastructure.

## Features

- 🎯 **Declarative Configuration**: Define your Kafka topics and schemas in YAML
- 📊 **Schema Registry Management**: Support for both Avro and Protocol Buffer schemas
- 🔄 **Dependency Resolution**: Automatic dependency graph resolution for schemas
- 🛡️ **Safety First**: Plan-and-apply workflow to preview changes before execution
- 🏗️ **Modularity Support**: Organized schema management for splitting schemas into different modules
- 🐳 **Docker Ready**: Containerized deployment with Docker Compose setup
- 🌍 **Environment Agnostic**: Support for multiple environments with sandbox mode

## Quick Start

### Local Development Setup

1. **Start Kafka and Schema Registry**:
   ```bash
   docker-compose up -d
   ```

2. **Build the project**:
   ```bash
   ./gradlew build
   ```

## Configuration

To execute properly CLI needs a configuration file in YAML format.

The configuration YAML schema is located in `src/main/resources/configmap-schema.yaml`.

By default, the CLI looks for `configmap.yaml` file in the current working directory, but you can override this using the
`-c, --configmap <file>` parameter.

The example configuration is defined in [src/main/resources/configmap.yaml](src/main/resources/configmap.yaml).

## Schema Organization

Schemas are organized by business domain under the `avro/` directory:

```
avro/
├── common/           # Shared schemas
│   ├── AddressAvro.avsc
│   ├── MoneyAvro.avsc
│   └── ...
├── gaming-common/    # Gaming domain shared schemas
├── purchases/        # E-commerce schemas
└── tictactoe/        # Tic-tac-toe game schemas
```

## CLI Commands

The following table lists all available CLI commands and their parameters:

| Command           | Description                                     | Parameters                                                                                                              | Example                                                                    |
|-------------------|-------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------|
| `topics-plan`     | Preview topic configuration changes             | `-c, --configmap <file>` - Path to configmap YAML file                                                                  | `./bin/ordo-eventi topics-plan -c config/prod.yaml`                        |
| `topics-apply`    | Apply topic configuration changes               | `-c, --configmap <file>` - Path to configmap YAML file                                                                  | `./bin/ordo-eventi topics-apply -c config/prod.yaml`                       |
| `schemas-plan`    | Preview schema changes with dependency analysis | `-c, --configmap <file>` - Path to configmap YAML file<br>`-i, --inclusion-globs <patterns>` - Glob patterns to include | `./bin/ordo-eventi schemas-plan -i "common/**" -i "purchases/**"`          |
| `schemas-apply`   | Apply schema changes to registry                | `-c, --configmap <file>` - Path to configmap YAML file                                                                  | `./bin/ordo-eventi schemas-apply -c config/prod.yaml`                      |
| `schemas-tree`    | Display schema dependency tree                  | `-c, --configmap <file>` - Path to configmap YAML file<br>`-n, --namespace <namespace>` - Specific namespace to display | `./bin/ordo-eventi schemas-tree -n "io.github.torvehammok.avro.common"`    |
| `schemas-destroy` | Remove all schemas (destructive)                | `-c, --configmap <file>` - Path to configmap YAML file                                                                  | `./bin/ordo-eventi schemas-destroy -c config/test.yaml`                    |
| `run-all`         | Execute multiple commands in sequence           | `<commands...>` - List of commands to run<br>`-c, --configmap <file>` - Path to configmap YAML file                     | `./bin/ordo-eventi run-all topics-apply schemas-apply -c config/prod.yaml` |

### Parameter Details

- **`-c, --configmap`**: Path to the configuration YAML file. If not specified, uses `src/main/resources/configmap.yaml`
- **`-i, --inclusion-globs`**: List of glob patterns for selective schema processing (overrides config file settings)
- **`-n, --namespace`**: Filter schemas by specific namespace for tree display
- **`<commands...>`**: Space-separated list of commands for `run-all` (e.g.,
  `topics-plan topics-apply schemas-plan schemas-apply`)

## Architecture

The project follows a clean architecture pattern:

- **CLI Layer** (`cli/`): Command-line interface using PicoCLI
- **Domain Layer** (`domain/`): Business logic for topics, schemas, and sandbox management
- **Infrastructure Layer** (`infra/`): Kafka admin clients, schema registry clients, and configuration

## Schema Features

### Dependency Resolution

The tool automatically resolves schema dependencies and applies them in the correct order:

```
AddressAvro → CustomerAvro → PurchaseAvro
```

### Multi-Format Support

- **Avro**: Primary format with automatic Java code generation
- **Protocol Buffers**: Full support for protobuf schemas

## Environment Configuration

Ordo Eventi supports environment variable substitution in the `configmap.yaml` file using the `$env:VARIABLE_NAME`
syntax. This allows you to keep sensitive information like credentials and connection strings out of your configuration
files.

### Environment Variable Syntax

In your `configmap.yaml`, reference environment variables using the `$env:` prefix:

```yaml
kafkaAdmin:
  config:
    bootstrap.servers: localhost:9092
    security.protocol: SASL_SSL
    sasl.mechanism: PLAIN
    sasl.jaas.config: $env:CC_KAFKA_JAAS_CONFIG

schemaRegistry:
  baseUrl: $env:SCHEMA_REGISTRY_URL
  config:
    basic.auth.user.info: $env:CC_SCHEMA_REGISTRY_AUTH
    basic.auth.credentials.source: USER_INFO
```

### Setting Environment Variables

You can provide these values through multiple methods:

#### 1. Environment Variables

```bash
export CC_KAFKA_JAAS_CONFIG="org.apache.kafka.common.security.plain.PlainLoginModule required username='key' password='secret';"
export CC_SCHEMA_REGISTRY_AUTH="key:secret"

./bin/ordo-eventi schemas-apply -c config/prod.yaml
```

#### 2. `.env` File

Create a `.env` file in your project root:

```bash
CC_KAFKA_JAAS_CONFIG=org.apache.kafka.common.security.plain.PlainLoginModule required username='key' password='secret';
CC_SCHEMA_REGISTRY_AUTH=key:secret
SCHEMA_REGISTRY_URL=https://schema-registry.company.com
```

The application automatically loads `.env` files and makes them available as system properties.

### Benefits

- **Security**: Keep sensitive credentials out of version control
- **Flexibility**: Use different values across environments (dev, staging, prod)
- **CI/CD Integration**: Easily inject secrets from your deployment pipeline
- **Team Collaboration**: Share configuration templates without exposing credentials

## Sandbox Mode

Enable sandbox mode to safely test changes:

```yaml
sandbox:
  enabled: true
  prefix: test-
```

All topics and schemas will be prefixed to avoid conflicts with production data.
