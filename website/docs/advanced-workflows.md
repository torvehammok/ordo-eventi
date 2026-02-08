---
sidebar_position: 8
---
# Advanced Workflows

Integrate Ordo Eventi into your development lifecycle and CI/CD pipelines.

## GitOps & CI/CD

Ordo Eventi is designed for GitOps. A typical pipeline might look like this:

1. **On Pull Request**:
   - Run `ordo-eventi topics-plan` and `ordo-eventi schemas-plan`.
   - Post the plan as a comment on the PR for review.
2. **On Merge to Main**:
   - Run `ordo-eventi topics-apply` and `ordo-eventi schemas-apply`.

## Confluent Cloud Integration

To use Ordo Eventi with Confluent Cloud, use SASL/PLAIN authentication.

**Environment Variables:**
```bash
export CC_KAFKA_JAAS_CONFIG="org.apache.kafka.common.security.plain.PlainLoginModule required username='<API_KEY>' password='<API_SECRET>';"
export CC_SCHEMA_REGISTRY_AUTH="<SR_API_KEY>:<SR_API_SECRET>"
```

**Configmap:**
```yaml
kafkaAdmin:
  config:
    bootstrap.servers: <CLOUD_BOOTSTRAP_SERVER>
    security.protocol: SASL_SSL
    sasl.mechanism: PLAIN
    sasl.jaas.config: $env:CC_KAFKA_JAAS_CONFIG

schemaRegistry:
  baseUrl: <CLOUD_SR_URL>
  config:
    basic.auth.credentials.source: USER_INFO
    basic.auth.user.info: $env:CC_SCHEMA_REGISTRY_AUTH
```

## Schema Migration Patterns

When evolving schemas:
1. Always run `schemas-plan` first to ensure the new version is compatible.
2. If using sandbox mode, you can deploy the new schema version with a prefix to test application logic before promoting to production.
3. Use `schemas-tree` to identify all downstream schemas that might be affected by a change in a shared "common" schema.
