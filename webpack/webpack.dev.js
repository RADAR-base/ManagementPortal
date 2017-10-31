const webpack = require('webpack');
const path = require('path');
const commonConfig = require('./webpack.common.js');
const writeFilePlugin = require('write-file-webpack-plugin');
const webpackMerge = require('webpack-merge');
const plugin = require("base-href-webpack-plugin");// Or `import 'base-href-webpack-plugin';` if using typescript
const BrowserSyncPlugin = require('browser-sync-webpack-plugin');
const ExtractTextPlugin = require("extract-text-webpack-plugin");
const ENV = 'dev';
const execSync = require('child_process').execSync;
const fs = require('fs');
const ddlPath = './build/www/vendor.json';

if (!fs.existsSync(ddlPath)) {
    execSync('webpack --config webpack/webpack.vendor.js');
}

module.exports = webpackMerge(commonConfig({ env: ENV }), {
    devtool: 'inline-source-map',
    devServer: {
        contentBase: './build/www',
        proxy: [{
            context: [
                '/oauth',
                <!-- jhipster-needle-add-entity-to-webpack - JHipster will add entity api paths here -->
                '/api',
                '/management',
                '/swagger-resources',
                '/v2/api-docs',
                '/h2-console'
            ],
            target: 'http://127.0.0.1:8080',
            secure: false
        }]
    },
    output: {
        path: path.resolve('build/www'),
        filename: '[name].bundle.js',
        chunkFilename: '[id].chunk.js',
    },
    module: {
        rules: [{
            test: /\.ts$/,
            loaders: [
                'tslint-loader'
            ],
            exclude: ['node_modules', new RegExp('reflect-metadata\\' + path.sep + 'Reflect\\.ts')]
        }]
    },
    plugins: [
        new BrowserSyncPlugin({
            host: 'localhost',
            port: 9000,
            proxy: {
                target: 'http://localhost:9060'
            }
        }, {
            reload: false
        }),
        new ExtractTextPlugin('styles.css'),
        new webpack.NoEmitOnErrorsPlugin(),
        new webpack.NamedModulesPlugin(),
        new writeFilePlugin(),
        new webpack.WatchIgnorePlugin([
            path.resolve('./src/test'),
        ]),
        new plugin.BaseHrefWebpackPlugin({ baseHref: '/' })
    ]
});
