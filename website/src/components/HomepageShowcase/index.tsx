import React from 'react';
import clsx from 'clsx';
import Heading from '@theme/Heading';
import CodeBlock from '@theme/CodeBlock';
import styles from './styles.module.css';

const ConfigExample = `kafkaAdmin:
  config:
    bootstrap.servers: localhost:9092
    security.protocol: SASL_SSL
    sasl.jaas.config: $env:CC_KAFKA_JAAS_CONFIG

schemaRegistry:
  baseUrl: http://localhost:8081
  config:
    basic.auth.user.info: $env:CC_SCHEMA_REGISTRY_AUTH

topicSpecs:
  topicDefaults:
    partitions: 3
    replicationFactor: 3
  topics:
    - name: orders.created
    - name: orders.shipped
      config:
        retention.ms: 604800000`;

const StructureExample = `avro/
├── common/
│   ├── AddressAvro.avsc
│   └── MoneyAvro.avsc
├── purchases/
│   ├── OrderCreated.avsc
│   └── OrderShipped.avsc
└── gaming/
    └── PlayerJoined.avsc`;

const CliExample = `# 1. Preview changes (Dry Run)
$ ordo-eventi topics-plan -c prod.yaml
[INFO] Planning changes...
[INFO] + Creating topic 'orders.created'
[INFO] ~ Updating config for 'orders.shipped'

# 2. Apply changes
$ ordo-eventi topics-apply -c prod.yaml
[INFO] Applying changes...
[SUCCESS] Topic 'orders.created' created.`;

export default function HomepageShowcase() {
  return (
    <section className={styles.showcaseSection}>
      <div className="container">
        <div className="row">
          <div className={clsx('col col--6')}>
            <Heading as="h3">📄 Declarative Configuration</Heading>
            <p>
              Define your entire infrastructure in a single <code>configmap.yaml</code>. 
              Use environment variables for secrets and defaults to reduce boilerplate.
            </p>
            <CodeBlock language="yaml">{ConfigExample}</CodeBlock>
          </div>
          <div className={clsx('col col--6')}>
            <Heading as="h3">📂 Domain-Driven Organization</Heading>
            <p>
              Structure your schemas by business domain. Ordo Eventi automatically 
              resolves dependencies (e.g., <code>OrderCreated</code> depends on <code>MoneyAvro</code>) 
              regardless of directory structure.
            </p>
            <CodeBlock language="bash">{StructureExample}</CodeBlock>
          </div>
        </div>
        <div className="row padding-top--lg">
           <div className="col col--8 col--offset-2">
            <div className="text--center">
                <Heading as="h3">🚀 Simple CLI Workflow</Heading>
                <p>
                  Adopt a safe <b>Plan & Apply</b> workflow. Preview every change before it hits your cluster.
                </p>
            </div>
            <CodeBlock language="bash">{CliExample}</CodeBlock>
           </div>
        </div>
      </div>
    </section>
  );
}
