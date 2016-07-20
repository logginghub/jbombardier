'use strict';

var templateUrl = require('ngtemplate!html!./phaseView.template.html');

var module = require('./repo.module.js');

module.directive("phaseview", PhaseViewDirective);

function PhaseViewDirective() {
    return {
        restrict: 'E',

        scope: {}, bindToController: {
            model: '=model'
        },

        templateUrl: templateUrl,
        controllerAs: 'vm',
        css: 'phaseview.css',

        controller: function () {
            var vm = this;
        },

        link: function (scope, element, attrs) {

        }
    }
}
