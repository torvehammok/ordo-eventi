---
sidebar_position: 10
---
# Troubleshooting & FAQ

Common issues and how to resolve them.

## Common Issues

### 1. Authentication Failures
**Symptom**: `401 Unauthorized` or `SaslAuthenticationException`.
- **Solution**: Check your `$env:` variables. Ensure they are correctly exported or present in the `.env` file. If using Confluent Cloud, verify that your API keys have the necessary "Manager" or "Write" permissions.

### 2. Circular Dependencies in Schemas
**Symptom**: The tool fails to build the dependency graph or reports a cycle.
- **Solution**: Avro and Protobuf do not support circular references. Use `schemas-tree` to visualize your dependencies and refactor your schemas to form a Directed Acyclic Graph (DAG).

### 3. Environment Variable Not Resolved
**Symptom**: `IllegalArgumentException: Environment variable X not found`.
- **Solution**: Ensure the variable name in your YAML matches exactly (including case) with your environment. If using a `.env` file, ensure it is in the root directory where you are running the command.

## Debugging

To get more information about what Ordo Eventi is doing under the hood, you can increase the logging level.

### Configuring Logback
Edit the `src/main/resources/logback.xml` file (if building from source) or provide an external logback configuration to the JVM:

```bash
# Example: Setting internal logic to DEBUG
<logger name="io.github.torvehammok" level="DEBUG"/>
```

## FAQ

**Q: Does Ordo Eventi delete topics automatically?**
A: Only if they are explicitly marked with `deleteTopic: true` in the configuration. Otherwise, it follows a "keep" policy for safety.

**Q: Can I use this with self-hosted Kafka?**
A: Yes. Any Kafka broker version 3.0+ and Confluent Schema Registry compatible service will work.
