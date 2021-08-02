module.exports = {
	extends: [
		'react-app',
		'eslint:recommended',
		'plugin:react/recommended',
		'plugin:@typescript-eslint/recommended' // Uses the recommended rules from the @typescript-eslint/eslint-plugin
	],
	globals: {
		console: true,
		localStorage: true,
		module: true,
		window: true
	},
	ignorePatterns: ['src/api/*/', 'sidebars.js'],
	parser: '@typescript-eslint/parser', // Specifies the ESLint parser
	parserOptions: {
		ecmaFeatures: {
			jsx: true,
			modules: true
		},
		ecmaVersion: 2020, // Allows for the parsing of modern ECMAScript features
		sourceType: 'module' // Allows for the use of imports
	},
	plugins: ['react', '@typescript-eslint'],
	rules: {
		// Place to specify ESLint rules. Can be used to overwrite rules specified from the extended configs
		// e.g. "@typescript-eslint/explicit-function-return-type": "off",
		'@typescript-eslint/ban-ts-comment': 'off',
		'@typescript-eslint/ban-types': 'off',
		'@typescript-eslint/camelcase': 'off',
		'@typescript-eslint/explicit-function-return-type': 'off',
		'@typescript-eslint/explicit-module-boundary-types': 'off',
		'@typescript-eslint/member-delimiter-style': 'off',
		'@typescript-eslint/no-explicit-any': 'off',
		'@typescript-eslint/no-extra-semi': 'off',
		'@typescript-eslint/no-non-null-assertion': 'off',
		'@typescript-eslint/no-unused-expressions': 'off',
		'@typescript-eslint/no-unused-vars': 'off',
		'@typescript-eslint/no-unused-vars-experimental': 'warn',
		'comma-dangle': ['warn', 'never'],
		'comma-spacing': ['warn', { after: true, before: false }],
		'key-spacing': [
			'warn',
			{
				afterColon: true,
				beforeColon: false
			}
		],
		'no-debugger': 'warn',
		'no-duplicate-imports': 'error',
		'no-useless-computed-key': 'warn',
		'quote-props': ['warn', 'as-needed'],
		quotes: ['warn', 'single'],
		'react/display-name': 'off',
		'react/jsx-sort-props': 'warn',
		semi: ['warn', 'never'],
		'semi-spacing': ['warn', { after: true, before: false }],
		'sort-imports': [
			'warn',
			{
				ignoreCase: true,
				ignoreMemberSort: false,
				memberSyntaxSortOrder: ['none', 'all', 'single', 'multiple']
			}
		],
		'sort-keys': ['warn', 'asc'],
		'space-in-parens': ['warn', 'never']
	},
	settings: {
		react: {
			version: 'detect'
		}
	}
}
