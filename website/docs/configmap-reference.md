---
sidebar_position: 4
---

# Configmap Reference

This page provides a detailed reference for all available configuration options in the `configmap.yaml` file, based on the project's JSON schema.

## Root Properties

| Property | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `kafkaAdmin` | Object | Yes | Kafka AdminClient connectivity settings. |
| `schemaRegistry` | Object | Yes | Confluent Schema Registry connectivity settings. |
| `schemaSpecs` | Object | Yes | Configuration for schema discovery and registry defaults. |
| `topicSpecs` | Object | Yes | Configuration for topic management and defaults. |
| `sandbox` | Object | No | Isolated environment settings. |

---

## kafkaAdmin

Configuration for the Kafka AdminClient.

| Property | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `config` | Object | Yes | A map of standard Kafka AdminClient properties (e.g., `bootstrap.servers`, `sasl.jaas.config`). |

---

## schemaRegistry

Configuration for the Confluent Schema Registry.

| Property | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `baseUrl` | String | Yes | The base URL of the Schema Registry (e.g., `http://localhost:8081`). |
| `config` | Object | No | Additional Schema Registry client properties (e.g., `basic.auth.user.info`). |

---

## schemaSpecs

Settings for how Ordo Eventi discovers and manages schemas.

| Property | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `dir` | String | - | Directory where your schema files are stored. |
| `format` | String | `AVRO` | Format of the schemas. Enum: `AVRO`, `PROTOBUF`. |
| `includeOnly` | Array | - | List of glob patterns to filter which schemas to process. |
| `defaults` | Object | - | Default settings for registered schemas. |

### schemaSpecs.includeOnly
An array of objects with a `glob` property:
```yaml
includeOnly:
  - glob: "common/**"
  - glob: "orders/*"
```

### schemaSpecs.defaults
| Property | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `compatibility` | String | `BACKWARD` | Compatibility level. Enum: `BACKWARD`, `FORWARD`, `FULL`, `NONE`, `FULL_TRANSITIVE`. |

---

## topicSpecs

Settings for Kafka topic management.

| Property | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `topicDefaults` | Object | Yes | Default settings applied to all topics. |
| `topics` | Array | Yes | List of topic definitions. |

### topicSpecs.topicDefaults
| Property | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `partitions` | Integer | Yes | Default number of partitions (minimum 1). |
| `config` | Object | Yes | Default Kafka topic configuration values. |

### topicSpecs.topics (Item)
| Property | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `name` | String | - | **Required**. The name of the Kafka topic. |
| `partitions` | Integer | - | Override default number of partitions. |
| `deleteTopic` | Boolean | `false` | If true, the topic will be deleted if it exists but is not in the config (only if apply is run). |
| `configOverrides` | Object | - | Map of topic-specific configuration overrides. |

---

## sandbox

Settings for isolated development environments.

| Property | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `enabled` | Boolean | Yes | Enable or disable sandbox mode. Default: `false`. |
| `prefix` | String | Yes | Prefix to apply to all topics and schema subjects. |
