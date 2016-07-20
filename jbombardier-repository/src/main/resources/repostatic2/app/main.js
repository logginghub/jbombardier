require('expose?$!expose?jQuery!jquery');
require("bootstrap-webpack");
require("angular");
require("angular-css");
require("jsonformatter");

module = require('./repo.module.js');

require('./testsTable.directive.js');
require('./resultView.directive.js');
require('./phaseView.directive.js');
require('./transactionsView.directive.js');

require('./test.controller.js');
require('./configuration.controller.js');

require('./repo.routes.js');
require('../images/black-flask-hi.png');

require('file?name=[name].[ext]!../index.html');
require('file?name=[name].[ext]!./configuration.view.html');
require('file?name=[name].[ext]!./configurations.view.html');
require('file?name=[name].[ext]!./resultview.css');
require('file?name=[name].[ext]!./phaseview.css');
require('file?name=[name].[ext]!./dashboard.css');
require('file?name=[name].[ext]!./transactionsview.css');
require('file?name=[name].[ext]!./json-formatter.min.css');

console.log("Main.js complete");

