import CloudNative from '../../static/img/cloud_native.svg'
import CommunityDriven from '../../static/img/community_driven.svg'
import OpenSource from '../../static/img/open_source.svg'
import styles from './HomepageFeatures.module.css'
import VendorNeutral from '../../static/img/vendor_neutral.svg'
import React, { FC } from 'react'

const HomepageFeatures: FC = () => (
	<section className={styles.features}>
		<OpenSource />
		<CommunityDriven />
		<VendorNeutral />
		<CloudNative />
	</section>
)

export default HomepageFeatures
