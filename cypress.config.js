const { defineConfig } = require('cypress')

module.exports = defineConfig({
  retries: 3,
  video: false,
  chromeWebSecurity: false,
  watchForFileChanges: false,
  defaultCommandTimeout: 10000,
  requestTimeout: 10000,
  responseTimeout: 10000,
  pageLoadTimeout: 60000,
  viewportWidth: 1920,
  viewportHeight: 1080,
  e2e: {
    baseUrl: 'http://localhost:8081',
    specPattern: 'src/test/javascript/e2e/cypress/integration/**/*.ts',
    screenshotsFolder: 'src/test/javascript/e2e/cypress/screenshots',
    supportFile: '',
    fixturesFolder: false,
    experimentalSessionAndOrigin: true,
  },
})

