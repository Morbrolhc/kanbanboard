'use strict';

/**
 * @ngdoc function
 * @name kanbanboardFrontendApp.controller:ResetCtrl
 * @description
 * # ResetCtrl
 * Controller of the kanbanboardFrontendApp
 */
angular.module('kanbanboardFrontendApp')
  .controller('ResetCtrl', ['restconnector', '$location', 
      function (restconnector, $location) {
    var scope = this;
    this.password1 = '';
    this.password2 = '';
    this.message = '';
    this.username = '';
    this.resetToken = '';
    this.tokenExpired = false;
    
    this.checkResetToken = function() {
      restconnector.checkResetToken(this.username, this.resetToken)
      .then(function(resultOK) {
        scope.tokenExpired = false;
      }, function(resultError) {
        scope.tokenExpired = true;
      });
    };
    
    var pathVars = $location.path().split("/");
    if(pathVars.length !== 4) {
      $location.path('/login');
    } else {
      //TODO check if reset token is valid!!
      this.username = pathVars[2];
      this.resetToken = pathVars[3];
      this.checkResetToken();
    }
    
    this.resetPassword = function() {
      if(this.password1 === this.password2) {
        restconnector.passwordReset(this.username, this.resetToken, this.password1)
        .then(function(){
          //no error
        }, function(response) {
          scope.message = 'Could not reset password ' + response;
        });
      }
    };
  }]);
