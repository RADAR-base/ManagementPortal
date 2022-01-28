const MergeJsonWebpackPlugin = require("merge-json-webpack-plugin");
const mergeFn = require('lodash.merge');

module.exports = {
    plugins: [
        new MergeJsonWebpackPlugin({
            mergeFn,
            groups: [
                {
                    pattern: "./src/main/webapp/i18n/en/*.json",
                    to: "./i18n/en.json",
                },
                {
                    pattern: "./src/main/webapp/i18n/nl/*.json",
                    to: "./i18n/nl.json",
                },
            ],
        }),
    ],
};
