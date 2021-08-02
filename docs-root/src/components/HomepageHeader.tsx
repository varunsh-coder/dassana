import clsx from 'clsx'
import LandingHeroImg from '../../static/img/landing_hero.png'
import LandingHeroMobile from '../../static/img/landing_hero_mobile.png'
import Lightning from '../../static/img/lightning.svg'
import Link from '@docusaurus/Link'
import Strike from '../../static/img/strike.svg'
import styles from './HomepageHeader.module.css'
import { useWindowSize } from '@dassana-io/web-utils'
import React, { FC } from 'react'

const HomepageHeader: FC = () => {
	const { isMobile } = useWindowSize()

	return (
		<header className={clsx('hero hero--primary', styles.heroBanner)}>
			<div>
				<h1 className={clsx('hero__title', styles.heroTitle)}>
					<span>Stay Alert</span>
					<div className={styles.emphasizedWord}>
						<div className={styles.strike}>
							<Strike />
						</div>
						Fatigued
					</div>
				</h1>
				<div className={clsx('hero__subtitle', styles.heroSubtitle)}>
					<div className={styles.lightningContainer}>
						<Lightning className={styles.lightning} />
						upercharge your alerts
					</div>
					<div>with Dassana</div>
				</div>
				<img
					alt='infographic'
					className={styles.bannerImage}
					src={isMobile ? LandingHeroMobile : LandingHeroImg}
				/>
				<Link className={styles.link} to='/docs/intro'>
					Get started
				</Link>
			</div>
		</header>
	)
}

export default HomepageHeader
