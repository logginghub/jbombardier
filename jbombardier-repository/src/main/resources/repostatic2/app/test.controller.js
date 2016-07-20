var module = require('./repo.module.js');

console.log("%o", module);

console.log("Creating angular test controller");

module.controller("TestController", TestController);

function TestController($http) {

    console.log("Test controller setup..");

    var vm = this;

    vm.title = 'james';
    vm.name = {};
    vm.sendMessage = function() { };

    vm.configurations =  [];

    activate();

    function activate() {

        $http({
            method: 'GET',
            url: '/services/configurations'
        }).then(function successCallback(response) {

            console.log("Response : %o", response.data);

            vm.configurations = response.data;

        }).catch(function errorCallback(response) {
            console.log("Error %o", response)
        });

    }

}
