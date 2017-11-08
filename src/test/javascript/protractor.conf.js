const HtmlScreenshotReporter = require("protractor-jasmine2-screenshot-reporter");
const JasmineReporters = require('jasmine-reporters');
const SpecReporter = require('jasmine-spec-reporter').SpecReporter;

exports.config = {
    allScriptsTimeout: 120000,

    specs: [
        './e2e/account/*.spec.ts',
        './e2e/admin/*.spec.ts',
        './e2e/entities/*.spec.ts',
        './e2e/scenarios/*.spec.ts'
    ],

    capabilities: {
        'browserName': 'chrome',
        'phantomjs.binary.path': require('phantomjs-prebuilt').path,
        'phantomjs.ghostdriver.cli.args': ['--loglevel=DEBUG'],
        chromeOptions: {
            args: [ "--headless", "--disable-gpu", "--window-size=1280x1024" ]
        }
    },

    directConnect: true,

    baseUrl: 'http://localhost:8080/',

    framework: 'jasmine2',

    jasmineNodeOpts: {
        showColors: true,
        defaultTimeoutInterval: 60000,
        print: function() {}
    },

    beforeLaunch: function() {
        require('ts-node').register({
            project: ''
        });
    },

    onPrepare: function() {
        browser.driver.manage().window().setSize(1280, 1024);
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
