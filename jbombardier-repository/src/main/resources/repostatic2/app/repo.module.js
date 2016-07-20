var angular = require('angular');
var angularRoute = require('angular-route');

console.log("Creating angular repo app");

var angularModule = angular.module('repo', ['ngRoute', 'angularCSS', 'jsonFormatter']);
console.log("Module is %o", angularModule);

module.exports = angularModule;