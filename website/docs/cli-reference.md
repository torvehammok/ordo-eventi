---
sidebar_position: 7
---
# CLI Reference

Ordo Eventi provides a suite of commands to plan, apply, and visualize your infrastructure.

## State Commands

### `topics-plan`
Compares the desired topic state from your config with the actual state in Kafka.
- **Usage**: `ordo-eventi topics-plan [-c <configmap>]`
- **Output**: A summary of topics to be created, updated, or deleted.

### `schemas-plan`
Analyzes local schema files and compares them with the versions in Schema Registry.
- **Usage**: `ordo-eventi schemas-plan [-c <configmap>]`
- **Options**:
    - `-i, --inclusion-globs`: Override config file to process specific schemas.

## Apply Commands

### `topics-apply`
Executes the topic plan.
- **Usage**: `ordo-eventi topics-apply [-c <configmap>]`

### `schemas-apply`
Registers new schema versions and updates compatibility settings.
- **Usage**: `ordo-eventi schemas-apply [-c <configmap>]`

## Visualization

### `schemas-tree`
Displays the dependency hierarchy of your schemas.
- **Usage**: `ordo-eventi schemas-tree [-c <configmap>] [-n <namespace>]`
- **Description**: Useful for understanding how shared schemas are used across your domain.

## Sequence Execution

### `run-all`
Runs multiple commands in the specified order.
- **Usage**: `ordo-eventi run-all <command1> <command2> ... [-c <configmap>]`
- **Example**: `ordo-eventi run-all topics-apply schemas-apply`

## Destructive Operations

### `schemas-destroy`
**Warning: Highly Destructive.** Deletes all subjects from the Schema Registry that match your configuration (or all subjects if in sandbox mode).
- **Usage**: `ordo-eventi schemas-destroy [-c <configmap>]`
