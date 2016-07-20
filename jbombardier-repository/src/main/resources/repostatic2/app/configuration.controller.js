var module = require('./repo.module.js');


module.controller("ConfigurationController", ConfigurationController);

function ConfigurationController($http, $routeParams) {

    var vm = this;

    vm.configurationName = $routeParams.configurationName;
    vm.results = [];

    activate();

    function activate() {

        $http({
            method: 'GET',
            url: '/services/configuration/',
            params: { configurationName : vm.configurationName }
        }).then(function successCallback(response) {
            console.log("Response : %o", response.data);
            vm.results = response.data;
        }).catch(function errorCallback(response) {
            console.log("Error %o", response)
        });

    }

}
