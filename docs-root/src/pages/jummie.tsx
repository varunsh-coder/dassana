import BrowserOnly from '@docusaurus/BrowserOnly'
import { Plausible } from '@dassana-io/web-components'
import React from 'react'
import styles from './index.module.css'

export default function Jummie() {
	return (
		<BrowserOnly fallback={<></>}>
			{() => (
				<div className={styles.jummie}>
					<Plausible />
				</div>
			)}
		</BrowserOnly>
	)
}
