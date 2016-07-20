'use strict';

var templateUrl = require('ngtemplate!html!./transactionsView.template.html');

var module = require('./repo.module.js');

module.directive("transactionresult", TransactionsViewDirective);

function TransactionsViewDirective() {
    return {
        restrict: 'E',

        scope: {}, bindToController: {
            model: '=model'
        },

        templateUrl: templateUrl,
        controllerAs: 'vm',
        css: 'transactionsview.css',

        controller: function () {
            var vm = this;
        },

        link: function (scope, element, attrs) {

        }
    }
}
