const MergeJsonWebpackPlugin = require("merge-json-webpack-plugin");
const mergeFn = require('lodash.merge');

const CompressionPlugin = require("compression-webpack-plugin");
const zlib = require("zlib");

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
        new CompressionPlugin({
            filename: "[path][base].gz",
            algorithm: "gzip",
            test: /\.js$|\.css$|\.html$/,
            threshold: 10240,
            minRatio: 0.8,
        }),
        new CompressionPlugin({
            filename: "[path][base].br",
            algorithm: "brotliCompress",
            test: /\.(js|css|html|svg)$/,
            compressionOptions: {
                params: {
                    [zlib.constants.BROTLI_PARAM_QUALITY]: 11,
                },
            },
            threshold: 10240,
            minRatio: 0.8,
        }),
    ],
};
