// Inject a debug flag into the bundle based on webpack mode.
// - "development" mode (wasmJsBrowserDevelopmentRun) → true
// - "production" mode (wasmJsBrowserProductionWebpack) → false
var webpack = require('webpack');
config.plugins.push(
    new webpack.BannerPlugin({
        banner: 'globalThis.__ACHI_IS_DEBUG__ = ' + (config.mode !== 'production') + ';',
        raw: true,
    })
);
