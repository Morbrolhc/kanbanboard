'use strict';

/**
 * @ngdoc function
 * @name kanbanboardFrontendApp.controller:UserCtrl
 * @description
 * # UserCtrl
 * Controller of the kanbanboardFrontendApp
 */
angular.module('kanbanboardFrontendApp')
  .controller('UserCtrl', ['$location', 'restconnector', '$window', '$translate', 'confirmService',
        function ($location, restconnector, $window, $translate, confirmService) {
    var scope = this;
    if(restconnector.getToken() === undefined) {
        $location.path('/login');
    } else {
        //Data
        this.user = restconnector.getUserInfos();
        
        //Models
        this.email = this.user.email;
        this.displayName = this.user.displayname;
        this.language = this.user.language;
        this.oldPassword = '';
        this.newPassword1 = '';
        this.newPassword2 = '';
    }
    
    this.loadTranslate = function() {
        $translate(['confirmUser', 'questionUser']).then(function (translations) {
            scope.textConfirmUser = translations.confirmUser;
            scope.textQuestionUser = translations.questionUser;
        });
    };
    
    this.changeLanguage = function() {
        var language = restconnector.getLanguage();
        if(language === undefined) {
          return;
        }
        language = angular.lowercase(language);
        if($translate.use() !== language) {
            $translate.use(language);
            this.loadTranslate();
        } 
    };
    //Initialize
    this.changeLanguage();
    this.loadTranslate();
    
    //Functions
    this.deleteAccount = function() {
        confirmService.openModal(this.textConfirmUser, this.textQuestionUser)
        .then(function(data) {
            if(data.result === 'OK') {
                restconnector.deleteUser()
                .then(function() {
                    scope.oldPassword = '';
                    scope.newPassword1 = '';
                    scope.newPassword2 = '';
                }, function() {
                    confirmService.openModal('Error', 'Could not delete Account');
                });
            }
        });
    };
    
    this.updateData = function() {
        if(this.newPassword1 === this.newPassword2) {
            restconnector.updateUserInfos(this.displayName, this.language, this.oldPassword, this.newPassword1)
            .then(function(response) {
                restconnector.saveToken(response.data.token)
                .then(function() {
                    scope.user = restconnector.getUserInfos();
                    scope.email = scope.user.email;
                    scope.displayName = scope.user.displayname;
                    scope.language = scope.user.language;
                    $location.path('/');
                }, function(error) {
                    confirmService.openModel('Update Error', 'Update Error');
                });
                scope.oldPassword = '';
                scope.newPassword1 = '';
                scope.newPassword2 = '';
            });
        }
    };
    this.cancel = function() {
        $location.path('/');
    };
  }]);
