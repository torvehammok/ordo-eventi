import type {ReactNode} from 'react';
import clsx from 'clsx';
import Heading from '@theme/Heading';
import styles from './styles.module.css';

type FeatureItem = {
  title: string;
  Svg?: React.ComponentType<React.ComponentProps<'svg'>>;
  description: ReactNode;
};

const FeatureList: FeatureItem[] = [
  {
    title: 'Declarative GitOps',
    description: (
      <>
        Define your infrastructure in YAML and version control it. 
        Ordo Eventi ensures your Kafka cluster matches your Git repository, 
        eliminating configuration drift.
      </>
    ),
  },
  {
    title: 'Plan & Apply Workflow',
    description: (
      <>
        Never fly blind. Review a detailed dry-run of changes before they happen. 
        See exactly which topics will be created and which schemas will be updated 
        before touching production.
      </>
    ),
  },
  {
    title: 'Smart Dependency Graph',
    description: (
      <>
        Complex Avro or Protobuf hierarchies? No problem. 
        The tool automatically builds a topological dependency graph and registers 
        schemas in the exact order required by the registry.
      </>
    ),
  },
  {
    title: 'Developer Sandbox',
    description: (
      <>
        Code without fear. Enable <code>sandbox mode</code> to automatically prefix 
        all topics and schemas (e.g., <code>dev-user-orders</code>), giving every developer 
        their own isolated slice of the infrastructure.
      </>
    ),
  },
  {
    title: 'Environment Agnostic',
    description: (
      <>
        From local Docker Compose to Confluent Cloud. Use standard <code>$env:VAR</code> 
        substitution and <code>.env</code> files to keep secrets safe and use the same 
        config template across all stages.
      </>
    ),
  },
  {
    title: 'Modular Architecture',
    description: (
      <>
        Organize your schema definitions by domain. Use glob patterns 
        to selectively apply only specific modules, keeping your CI/CD pipelines 
        fast and focused.
      </>
    ),
  },
];

function Feature({title, Svg, description}: FeatureItem) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center padding-horiz--md padding-vert--md">
        <Heading as="h3">{title}</Heading>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures(): ReactNode {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
