import React from 'react';
import clsx from 'clsx';
import Layout from '@theme/Layout';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import useBaseUrl from '@docusaurus/useBaseUrl';
import styles from './styles.module.css';

const features = [
  {
    title: <>Execute http files</>,
    imageUrl: 'img/undraw_text_files_au1q.svg',
    description: (
      <>
        Execute IntelliJ HTTP request files in the command-line.
      </>
    ),
  },
  {
    title: <>Running test script</>,
    imageUrl: 'img/undraw_code_inspection_bdl7.svg',
    description: (
      <>
        Running test script and give you the beautiful log and junit test report.
      </>
    ),
  },
  {
    title: <>Custom environment variables</>,
    imageUrl: 'img/undraw_docusaurus_react.svg',
    description: (
      <>
        Loading and inject environment variables through http-client.env.json file.
      </>
    ),
  },
  {
    title: <>Cross platform</>,
    imageUrl: 'img/undraw_designer_life_w96d.svg',
    description: (
      <>
        Windows, macOS and Linux
      </>
    ),
  },
  {
    title: <>Easy to create test flows</>,
    imageUrl: 'img/undraw_advanced_customization_58j6.svg',
    description: (
      <>
        By using control the next request in test script. It is easy to custom the next request in
        the test script base on your conditional of the test flow.
      </>
    ),
  },
];

function Feature({ imageUrl, title, description }) {
  const imgUrl = useBaseUrl(imageUrl);
  return (
    <div className={clsx('col col--4', styles.feature)}>
      {imgUrl && (
        <div className="text--center">
          <img className={styles.featureImage} src={imgUrl} alt={title} />
        </div>
      )}
      <h3>{title}</h3>
      <p>{description}</p>
    </div>
  );
}

function Home() {
  const context = useDocusaurusContext();
  const { siteConfig = {} } = context;
  return (
    <Layout
      title={`Hello from ${siteConfig.title}`}
      description="Description will go into a meta tag in <head />"
    >
      <header className={clsx('hero hero--primary', styles.heroBanner)}>
        <div className="container">
          <h1 className="hero__title">{siteConfig.title}</h1>
          <p className="hero__subtitle">{siteConfig.tagline}</p>
          <div className={styles.buttons}>
            <Link
              className={clsx(
                'button button--outline button--secondary button--lg',
                styles.getStarted,
              )}
              to={useBaseUrl('docs/')}
            >
              Get Started
            </Link>
          </div>
        </div>
      </header>
      <main>
        {features && features.length > 0 && (
          <section className={styles.features}>
            <div className="container">
              <div className="row">
                {features.map((props, idx) => (
                  <Feature key={idx} {...props} />
                ))}
              </div>
            </div>
          </section>
        )}
      </main>
    </Layout>
  );
}

export default Home;
