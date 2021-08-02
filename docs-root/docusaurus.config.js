/** @type {import('@docusaurus/types').DocusaurusConfig} */
/* eslint-disable sort-keys */
module.exports = {
	title: 'Dassana',
	tagline: 'Supercharge your alerts with Dassana',
	url: 'https://dassana.io',
	baseUrl: '/',
	onBrokenLinks: 'throw',
	onBrokenMarkdownLinks: 'warn',
	favicon: 'img/favicon.ico',
	organizationName: 'dassana-io', // Usually your GitHub org/user name.
	projectName: 'dassana', // Usually your repo name.
	scripts: [
		{
			src: 'https://plausible.io/js/plausible.outbound-links.js',
			async: true,
			defer: true,
			'data-domain': 'docs.dassana.io'
		}
	],
	themeConfig: {
		colorMode: {
			defaultMode: 'dark',
			disableSwitch: true
		},
		hideableSidebar: true,
		algolia: {
			apiKey: 'YOUR_API_KEY',
			indexName: 'YOUR_INDEX_NAME'
		},
		navbar: {
			logo: {
				alt: 'Dassana Logo',
				src: 'img/logo.svg'
			},
			items: [
				{
					type: 'doc',
					docId: 'intro',
					position: 'left',
					label: 'Docs'
				},
				{
					className: 'header-github-link',
					href: 'https://github.com/dassana-io/dassana',
					position: 'right'
				}
			]
		},
		footer: {
			style: 'dark',
			links: [
				{
					title: 'Resources',
					items: [
						{
							label: 'Dassana',
							to: 'https://oss.dassana.io/'
						},
						{
							label: 'Policy Hub',
							href: 'https://policyhub.dassana.io/'
						},
						{
							label: 'GitHub',
							href: 'https://github.com/dassana-io/dassana'
						}
					]
				},
				{
					title: 'Community',
					items: [
						{
							label: 'Slack',
							href: 'https://join.slack.com/t/dassanacommunity/shared_invite/zt-teo6d5ed-xkWDNSaH4m6pC8PAJnrD8g'
						},
						{
							label: 'GitHub Issues',
							href: 'https://github.com/dassana-io/dassana/issues/'
						},
						{
							label: 'GitHub Discussions',
							href: 'https://github.com/dassana-io/dassana/discussions'
						}
					]
				},
				{
					title: 'Other',
					items: [
						{
							label: 'Careers',
							href: 'https://dassanaio.notion.site/Job-Board-0a01b48e2ef8462bb3d12e50b8b21c9b'
						},
						{
							label: 'LinkedIn',
							href: 'https://www.linkedin.com/company/dassana-inc'
						},
						{
							label: 'Twitter',
							href: 'https://twitter.com/DassanaSecurity'
						}
					]
				}
			],
			copyright: `Copyright © ${new Date().getFullYear()} Dassana Inc.`
		}
	},
	presets: [
		[
			'@docusaurus/preset-classic',
			{
				docs: {
					sidebarPath: require.resolve('./sidebars.js'),
					editUrl:
						'https://github.com/dassana-io/dassana/edit/main/docs-root/'
				},
				theme: {
					customCss: require.resolve('./src/css/custom.css')
				}
			}
		]
	]
}
