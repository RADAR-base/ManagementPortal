const HtmlScreenshotReporter = require('protractor-jasmine2-screenshot-reporter');
const JasmineReporters = require('jasmine-reporters');
const SpecReporter = require('jasmine-spec-reporter').SpecReporter;

exports.config = {
    allScriptsTimeout: 20000,

    specs: [
        './e2e/account/*.spec.ts',
        './e2e/admin/*.spec.ts',
        './e2e/entities/*.spec.ts',
        './e2e/scenarios/*.spec.ts'
    ],

    capabilities: {
        'browserName': 'chrome',
        chromeOptions: {
            args: ["--headless", "no-sandbox", "--disable-gpu"]
        },
        'phantomjs.binary.path': require('phantomjs-prebuilt').path,
        'phantomjs.ghostdriver.cli.args': ['--loglevel=DEBUG'],
    },

    directConnect: true,

    baseUrl: 'http://localhost:8080/',

    framework: 'jasmine2',

    SELENIUM_PROMISE_MANAGER: false,

    jasmineNodeOpts: {
        showColors: true,
        defaultTimeoutInterval: 720000
    },

    beforeLaunch: function () {
        require('ts-node').register({
            project: ''
        });
    },

    onPrepare: function () {
        jasmine.getEnv().addReporter(new JasmineReporters.JUnitXmlReporter({
            savePath: 'build/reports/e2e',
            consolidateAll: false
        }));
        jasmine.getEnv().addReporter(new HtmlScreenshotReporter({
            dest: "build/reports/e2e/screenshots"
        }));
        jasmine.getEnv().addReporter(new SpecReporter({
            spec: {
                displayStacktrace: true
            }
        }));
    },

    useAllAngular2AppRoots: true
};
