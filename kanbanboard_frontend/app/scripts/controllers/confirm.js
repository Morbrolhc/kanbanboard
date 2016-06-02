'use strict';

/**
 * @ngdoc function
 * @name kanbanboardFrontendApp.controller:ConfirmCtrl
 * @description
 * # ConfirmCtrl
 * Controller of the kanbanboardFrontendApp
 */
angular.module('kanbanboardFrontendApp')
  .controller('ConfirmCtrl', ['$uibModalInstance', 'param', function ($uibModalInstance, param) {
    this.title = param.title;
    this.question = param.question;
    
    this.ok = function() {
      $uibModalInstance.close('OK');
    };
    
    this.cancel = function() {
        $uibModalInstance.close('Cancel');
    };
  }]);
