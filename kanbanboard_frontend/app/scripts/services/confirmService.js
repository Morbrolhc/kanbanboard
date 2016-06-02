'use strict';

/**
 * @ngdoc service
 * @name kanbanboardFrontendApp.confirm
 * @description
 * # confirm
 * Service in the kanbanboardFrontendApp.
 */
angular.module('kanbanboardFrontendApp')
  .service('confirmService', ['$uibModal', '$q', function ($uibModal, $q) {
        this.openModal = function(title, question) {
          var deferred = $q.defer();
          this.modal = $uibModal.open({
            templateUrl: 'views/confirm.html',
            controller: 'ConfirmCtrl as confirmCtrl',
                windowClass: 'app-modal-window',
                resolve: {
                    param: function () {
                        return {title: title, question: question};
                    }
                }	    	  
            })
            .result.then(function(result) {
                return deferred.resolve({result: result});
            }, function(error) {
                return deferred.reject({result: error});
          });
          return deferred.promise;		
        };
  }]);
