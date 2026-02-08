---
sidebar_position: 6
---
# Schema Management

Ordo Eventi simplifies the process of managing schemas in Confluent Schema Registry, with built-in support for dependencies.

## Supported Formats

- **Avro**: Definitions stored in `.avsc` files.
- **Protocol Buffers**: Definitions stored in `.proto` files.

Configure the format and directory in `configmap.yaml`:

```yaml
schemaSpecs:
  dir: src/main/resources/avro
  format: AVRO
```

## Schema Discovery

Ordo Eventi recursively searches the specified directory for schema files. You can use `includeOnly` to limit the scope:

```yaml
schemaSpecs:
  includeOnly:
    - glob: common/**
    - glob: orders/**
```

## Dependency Resolution

One of the most powerful features of Ordo Eventi is automatic dependency resolution. If an Avro schema refers to another schema via its fully qualified name, Ordo Eventi will:
1. Detect the dependency.
2. Build a directed acyclic graph (DAG).
3. Ensure that "child" schemas are registered before the "parent" schemas that depend on them.

This works similarly for Protobuf `import` statements.

## Compatibility Rules

You can set the default compatibility level for all subjects managed by the tool:

```yaml
schemaSpecs:
  defaults:
    compatibility: FULL
```

Supported values: `BACKWARD`, `BACKWARD_TRANSITIVE`, `FORWARD`, `FORWARD_TRANSITIVE`, `FULL`, `FULL_TRANSITIVE`, `NONE`.
