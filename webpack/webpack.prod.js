const commonConfig = require('./webpack.common.js');
const webpackMerge = require('webpack-merge');
const plugin = require("base-href-webpack-plugin");
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const Visualizer = require('webpack-visualizer-plugin');
const path = require('path');
const ENV = 'prod';

module.exports = webpackMerge(commonConfig({ env: ENV }), {
    mode: 'production',
    devtool: 'source-map',
    output: {
        path: path.resolve('./build/www'),
        filename: '[hash].[name].bundle.js',
        chunkFilename: '[hash].[id].chunk.js',
    },
    plugins: [
        new MiniCssExtractPlugin({
            filename: '[hash].styles.css'
        }),
        new Visualizer({
            // Webpack statistics in target folder
            filename: '../stats.html'
        }),
        new plugin.BaseHrefWebpackPlugin({ baseHref: '/managementportal/' })

    ]
});
