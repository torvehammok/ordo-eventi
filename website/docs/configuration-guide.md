---
sidebar_position: 3
---
# Configuration Guide

Ordo Eventi is configured primarily through a `configmap.yaml` file. This file defines how to connect to your Kafka cluster and Schema Registry, as well as the desired state of your topics and schemas.

## Configmap Structure

The configuration is divided into several main sections:

- `kafkaAdmin`: Connectivity settings for Kafka.
- `schemaRegistry`: Connectivity and defaults for the Schema Registry.
- `sandbox`: (Optional) Isolation settings for testing.
- `schemaSpecs`: Location and format of your schema definitions.
- `topicSpecs`: List of topics and their configurations.

## Kafka Connectivity

Configure the connection to your Kafka brokers in the `kafkaAdmin` block:

```yaml
kafkaAdmin:
  config:
    bootstrap.servers: localhost:9092
    security.protocol: PLAINTEXT
```

You can pass any standard Kafka AdminClient configuration property under the `config` map.

## Schema Registry Connectivity

Define the Schema Registry URL and authentication:

```yaml
schemaRegistry:
  baseUrl: http://localhost:8081
  config:
    basic.auth.credentials.source: USER_INFO
    basic.auth.user.info: "admin:admin"
```

## Environment Variables

Ordo Eventi supports dynamic configuration using environment variables with the `$env:VAR_NAME` syntax. This is highly recommended for sensitive information.

```yaml
kafkaAdmin:
  config:
    sasl.jaas.config: $env:CC_KAFKA_JAAS_CONFIG

schemaRegistry:
  config:
    basic.auth.user.info: $env:CC_SCHEMA_REGISTRY_AUTH
```

Values can be provided via standard environment variables or a `.env` file in the project root.

## Sandbox Mode

Sandbox mode allows you to safely test changes by prefixing all resource names:

```yaml
sandbox:
  enabled: true
  prefix: dev-user-
```

When enabled, a topic named `orders` will be managed as `dev-user-orders` in Kafka.
