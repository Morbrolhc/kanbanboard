'use strict';

/**
 * @ngdoc function
 * @name kanbanboardFrontendApp.controller:ActivationCtrl
 * @description
 * # ActivationCtrl
 * Controller of the kanbanboardFrontendApp
 */
angular.module('kanbanboardFrontendApp')
  .controller('ActivationCtrl', ['restconnector', '$location', '$timeout', function (restconnector, $location, $timeout) {
    var scope = this;
    this.error = '';
    this.activated = false;
    
    this.activateUser = function() {
      restconnector.activateUser(this.username, this.activationToken)
      .then(function(){
        scope.activated = true;
        $timeout(function() {
            $location.path('/login');
        }, 5000); //in milliseconds
      }, function(response) {
        scope.error = 'Could not activate user ' + scope.username;
      });
    };
      
    var pathVars = $location.path().split("/");
    if(pathVars.length !== 4) {
      $location.path('/login');
    } else {
      this.username = pathVars[2];
      this.activationToken = pathVars[3];
      this.activateUser();
    }
    
    this.toLogin = function() {
      $location.path('/login');
    };
  }]);
