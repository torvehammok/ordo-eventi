---
sidebar_position: 5
---
# Topic Management

Ordo Eventi allows you to manage Kafka topics declaratively. You define the topics you want, and the tool ensures they exist with the correct configuration.

## Defining Topics

Topics are listed under the `topicSpecs.topics` key in `configmap.yaml`.

```yaml
topicSpecs:
  topics:
    - name: orders.created
    - name: orders.cancelled
      partitions: 3
```

## Default Configurations

To avoid repetition, you can define default settings for all topics:

```yaml
topicSpecs:
  topicDefaults:
    partitions: 6
    replicationFactor: 3
    config:
      cleanup.policy: delete
      retention.ms: 604800000
```

Individual topics can override these defaults.

## Advanced Topic Properties

You can specify any Kafka topic-level configuration under the `config` key for a specific topic:

```yaml
- name: high-retention-topic
  config:
    retention.ms: 31536000000 # 1 year
    segment.bytes: 1073741824
```

## Deletion Policy

By default, Ordo Eventi will not delete topics that are not present in your configuration but exist in Kafka (for safety). To allow Ordo Eventi to delete a topic, you must explicitly set `deleteTopic: true`:

```yaml
- name: obsolete-topic
  deleteTopic: true
```

*Note: The tool will only delete topics if they are marked with `deleteTopic: true` and you run the `topics-apply` command.*
