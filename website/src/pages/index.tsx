import type {ReactNode} from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import HomepageFeatures from '@site/src/components/HomepageFeatures';
import HomepageShowcase from '@site/src/components/HomepageShowcase';
import HomepageCollage from '@site/src/components/HomepageCollage';
import Heading from '@theme/Heading';

import styles from './index.module.css';

function HomepageHeader() {
  const {siteConfig} = useDocusaurusContext();
  return (
    <header className={clsx('hero', styles.heroBanner)}>
      <div className="container">
        <div className={styles.logoContainer}>
          <img src="img/logo.svg" className={styles.heroLogo} alt="Ordo Eventi Wizard Bear" />
        </div>
        <Heading as="h1" className="hero__title">
          {siteConfig.title}
        </Heading>
        <p className="hero__subtitle">
          Infrastructure as Code <span className={styles.magicText}>magic</span> for your Event Streams.
        </p>
        <p className={styles.heroDescription}>
          Stop wrestling with imperative scripts. Manage Kafka topics and schemas declaratively
          with a GitOps workflow that just works.
        </p>
        <div className={styles.buttons}>
          <Link
            className="button button--primary button--lg"
            to="/docs/introduction">
            Get Started 🚀
          </Link>
          <Link
            className="button button--secondary button--lg"
            to="https://github.com/torvehammok/ordo-eventi">
            GitHub ⭐️
          </Link>
        </div>
      </div>
    </header>
  );
}

export default function Home(): ReactNode {
  const {siteConfig} = useDocusaurusContext();
  return (
    <Layout
      title={`${siteConfig.title}`}
      description="GitOps-friendly IaC tool for Kafka and Schema Registry">
      <HomepageHeader />
      <main>
        <HomepageFeatures />
        <HomepageShowcase />
        <HomepageCollage />
      </main>
    </Layout>
  );
}
