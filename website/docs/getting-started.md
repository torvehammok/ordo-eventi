---
sidebar_position: 2
---
# Getting Started

Follow this guide to get Ordo Eventi up and running on your local machine.

## Prerequisites

Before you begin, ensure you have the following installed:
- **Java 21 or higher**: Ordo Eventi is built using Kotlin and targets JVM 21.
- **Docker & Docker Compose**: Required for running a local Kafka broker and Schema Registry for testing.

## Installation

### Building from Source

You can build Ordo Eventi using the provided Gradle wrapper:

```bash
git clone https://github.com/torvehammok/ordo-eventi.git
cd ordo-eventi
./gradlew build
```

The executable will be available in `build/install/ordo-eventi/bin/ordo-eventi`.

## Quick Start Guide

### 1. Set up Local Infrastructure

Start a local Kafka broker and Confluent Schema Registry using Docker Compose:

```bash
docker-compose up -d
```

### 2. Configure Ordo Eventi

Create a `configmap.yaml` file in your current directory. You can use the example from the repository:

```bash
cp src/main/resources/configmap.yaml .
```

Ensure the `bootstrap.servers` and `baseUrl` points to your local Docker containers (usually `localhost:9092` and `http://localhost:8081`).

### 3. Run Your First Plan

Preview the changes that Ordo Eventi would apply to your local cluster:

```bash
# Preview topic changes
./gradlew topics-plan

# Preview schema changes
./gradlew schemas-plan
```

### 4. Apply Changes

If the plan looks correct, synchronize the state:

```bash
# Apply topic changes
./gradlew topics-apply

# Apply schema changes
./gradlew schemas-apply
```

Congratulations! You've just managed your first Kafka infrastructure with Ordo Eventi.
