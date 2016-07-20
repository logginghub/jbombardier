'use strict';

var templateUrl = require('ngtemplate!html!./resultView.template.html');

var module = require('./repo.module.js');

module.directive("resultview", ResultViewDirective);

function ResultViewDirective() {
    return {
        restrict: 'E',

        scope: {}, bindToController: {
            model: '=model'
        },

        templateUrl: templateUrl,
        controllerAs: 'vm',
        css: 'resultview.css',

        controller: function () {
            var vm = this;
        },

        link: function (scope, element, attrs) {

        }
    }
}
