const protractor = require("./protractor.conf")

exports.config = {
    ...protractor.config,
    baseUrl: 'http://localhost:8080/managementportal/',
};
