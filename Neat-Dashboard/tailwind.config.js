const tailwindcssui = require("@tailwindcss/ui");
const defaultTheme = require("tailwindcss/defaultTheme");

module.exports = {
	purge: [],
	theme: {
		extend: {
			fontFamily: {
				sans: ["Inter var", ...defaultTheme.fontFamily.sans],
			},
		},
	},
	variants: {},
	plugins: [tailwindcssui],
};
