var module = require('./repo.module.js');


module.config(['$locationProvider', '$routeProvider', function config($locationProvider, $routeProvider) {

    console.log("Setting up routes");

    $locationProvider.hashPrefix('!');

    $routeProvider.when('/configuration', {
        templateUrl: 'configurations.view.html',
        controller: "TestController",
        controllerAs: "vm"
    }).when('/configuration/:configurationName', {
        templateUrl: 'configuration.view.html',
        controller: "ConfigurationController",
        controllerAs: "vm"

    }).otherwise('/configuration');
}]);