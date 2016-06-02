'use strict';

/**
 * @ngdoc function
 * @name kanbanboardFrontendApp.controller:LoginCtrl
 * @description
 * # LoginCtrl
 * Controller of the kanbanboardFrontendApp
 */
angular.module('kanbanboardFrontendApp')
  .controller('LoginCtrl', ['restconnector', '$location', '$translate', '$cookies', 
      function (restconnector, $location, $translate, $cookies) {
    var scope = this;
    this.username = '';
    this.email = '';
    this.displayName = '';
    this.language = 'DE';
    this.password = '';
    this.password2 = '';
    this.error = '';
    
    this.registerActive = false;
    this.resetActive = false;
    
    //Functions
    this.login = function() {
      restconnector.login(this.username, this.password)
      .then(function(response) {
          restconnector.saveToken(response.data.token)
          .then(function() {
            $location.path('/');
          });
       }, function(error) {
         if(error !== undefined) {
           scope.error = error;
         }
       });
       this.password = '';
       this.password2 = '';
    };
    
    this.changeLanguage = function() {
      var language = this.language;
        if(language === undefined) {
          return;
        }
        language = angular.lowercase(language);
        if($translate.use() !== language) {
            $translate.use(language);
        }
    };
    
    this.register = function() {
      if(this.password !== this.password2) {
        return;
      }
      restconnector.createUser(this.username, this.password, this.displayName, this.email, this.language)
      .then(function(response) {
        scope.password = '';
        scope.error = 'Registrations Email versendet';
        scope.resetActive = false;
        scope.registerActive = false;
      }, function(error) {
        if(error !== undefined) {
           scope.error = error;
           if(angular.lowercase(scope.language) === 'de' && error === "Username/email does already exist") {
             scope.error = "Benutzername oder Email existiert bereits";
           }
         }
      });
    };
    
    this.resetPW = function() {
      restconnector.requestPasswordReset(this.email)
      .then(function(response) {
        scope.resetActive = false;
        scope.registerActive = false;
      }, function(error) {
        scope.error = 'could not reset: ' + error;
      });
    };
    
  }]);
