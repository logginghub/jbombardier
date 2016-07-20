'use strict';

var templateUrl = require('ngtemplate!html!./testsTable.template.html');

var module = require('./repo.module.js');

module.directive("teststable", TestsTableDirective);

function TestsTableDirective() {
    return {
        restrict: 'E',

        scope: {}, bindToController: {
            model: '=model'
        },

        templateUrl: templateUrl,
        controllerAs: 'vm',

        controller: function () {
            var vm = this;

            vm.something = "true";

        },

        link: function (scope, element, attrs) {

        }
    }
}
