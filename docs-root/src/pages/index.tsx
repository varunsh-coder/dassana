import BrowserOnly from '@docusaurus/BrowserOnly'
import HomepageFeatures from '../components/HomepageFeatures'
import HomepageHeader from '../components/HomepageHeader'
import Layout from '@theme/Layout'
import React from 'react'
import styles from './index.module.css'
import useDocusaurusContext from '@docusaurus/useDocusaurusContext'

export default function Home() {
	const {
		siteConfig: { tagline }
	} = useDocusaurusContext()

	return (
		<Layout description={tagline} title={'Docs'}>
			<BrowserOnly fallback={<></>}>
				{() => (
					<>
						<HomepageHeader />
						<main className={styles.main}>
							<HomepageFeatures />
						</main>
					</>
				)}
			</BrowserOnly>
		</Layout>
	)
}
