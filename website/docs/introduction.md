---
sidebar_position: 1
---
# Introduction

**Ordo Eventi** is a declarative, GitOps-friendly Infrastructure as Code (IaC) tool designed specifically for managing
Kafka topics and Confluent Schema Registry schemas.

In modern event-driven architectures, managing infrastructure state manually or through ad-hoc scripts often leads to "
configuration drift," where the actual state of your Kafka brokers and Schema Registry diverges from your documentation
or desired state. Ordo Eventi solves this by allowing you to define your infrastructure in YAML files and ensuring that
your environment remains synchronized with those definitions.

## Key Value Propositions

### 🎯 Declarative Infrastructure

Stop writing imperative scripts to create topics or register schemas. With Ordo Eventi, you describe **what** your
infrastructure should look like, and the tool handles the **how** of reaching that state.

### 🛡️ Safety-First Workflow (Plan & Apply)

Ordo Eventi introduces a robust `plan-and-apply` workflow. Before any changes are made, you can generate a "plan" that
shows exactly what additions, modifications, or deletions will occur. This allows for human review in manual workflows
or automated checks in CI/CD pipelines.

### 🔄 Intelligent Dependency Resolution

Managing schemas with references (especially in Avro or Protobuf) can be complex. Ordo Eventi automatically analyzes
your schema files, builds a dependency graph, and applies changes in the correct topological order. It ensures that
dependencies are registered before the schemas that rely on them.

### 🏗️ GitOps-Ready

Designed to be part of your CI/CD pipeline. By storing your configuration in a Git repository, you gain versioning,
audit trails, and the ability to roll back infrastructure changes just like you do with application code.

## Core Philosophy

### State Synchronization

Ordo Eventi acts as a reconciler. It compares the *desired state* defined in your configuration files with the *actual
state* in your Kafka cluster and Schema Registry. It then executes the necessary operations to align the actual state
with the desired state.

### Environment Agnosticism

The tool is built to work across various environments—from local development using Docker Compose to production
environments in Confluent Cloud. Through features like **Sandbox Mode** and **Environment Variable Substitution**, you
can use the same configuration templates across your entire SDLC.

### Sandbox Mode

Safety is paramount when managing shared infrastructure. Ordo Eventi features a built-in sandbox mode that allows
developers to test their infrastructure changes in isolation by automatically applying a configurable prefix to all
topic and schema names. This ensures that experimental changes never collide with production data or affect other team
members working in the same cluster.

### Modularity

Infrastructure should be organized by domain. Ordo Eventi supports splitting schemas and topics into business-logical
namespaces or modules, making it easier to manage large-scale event-driven systems. This is achieved by organizing
schema files into directory-based namespaces and using glob-based inclusion patterns in your configuration to
selectively manage specific parts of your infrastructure.
