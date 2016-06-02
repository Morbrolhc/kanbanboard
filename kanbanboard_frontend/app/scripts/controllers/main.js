'use strict';

/**
 * @ngdoc function
 * @name kanbanboardFrontendApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the kanbanboardFrontendApp
 */
angular.module('kanbanboardFrontendApp')
  .controller('MainCtrl', ['$location', 'restconnector', '$window', '$translate', 'confirmService',
  function ($location, restconnector, $window, $translate, confirmService) {
    var scope = this;
    
    //Models
    this.searchText = '';
    this.newBoard = undefined;
    this.focus = false;
    this.username = '';
    this.all = { toDo: 0, doing: 0, done: 0 };
    
    //data load function
    this.loadData = function() {
        restconnector.getBoards()
        .then(function(responseOK) {
            scope.boards = responseOK.data;
        }, function(error) {
            //error occured
        });
        restconnector.getUsersCards(0, '')
        .then(function(responseOK) {
            scope.all.toDo = responseOK.data.tasks[0];
            scope.all.doing += responseOK.data.tasks[1];
            scope.all.done += responseOK.data.tasks[2];  
        }, function(error) {
            //error
        });
        this.userInfos = restconnector.getUserInfos();
        this.username = this.userInfos.displayname;
    };
    
    this.loadTranslate = function() {
        $translate(['confirmBoard', 'questionBoard']).then(function (translations) {
            scope.textConfirmBoard = translations.confirmBoard;
            scope.textQuestionBoard = translations.questionBoard;
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
            scope.loadTranslate();
        } 
    };
    if(restconnector.getToken() === undefined) {
        var path = $location.path();
        if(path.indexOf('reset') > -1) {
           $location.path(path); 
        } else if(path.indexOf('activation') > -1) {
            $location.path(path);
        } else {
            $location.path('/login');
        }
    } else {
        this.loadData();
        this.changeLanguage();
        this.loadTranslate();
    }
    
    //Functions
    this.addNewBoard = function() {
        if(this.newBoard === undefined) {
            this.newBoard = {id:-1, name:'', members: [], toDo:0, doing:0, done:0};
            this.focus = true;
            //$location.path('/board');
        }
    };
    
    this.createBoard = function() {
        if(this.newBoard.name.length > 1) {
            restconnector.createBoard(this.newBoard.name)
            .then(function(responseOK) {
                scope.loadData();
                scope.newBoard = undefined;
            });
        }
    };
    
    this.openBoard = function(boardID) {
        $location.path('/board').search({id: boardID});
    };
    
    this.deleteBoard = function(boardID) {
        confirmService.openModal(this.textConfirmBoard, this.textQuestionBoard)
        .then(function(data) {
            if(data.result === 'OK') {
                restconnector.deleteBoard(boardID)
                .then(function(responseOK) {
                    scope.loadData();
                }); 
            }
        });
    };
    
    //Control methods for index.html
    this.isActive = function(viewLocation) {
        var active = (viewLocation === $location.path());
        return active;
    };
    
    this.getMessages = function() {
        return restconnector.getMessage();  
    };
    
    this.getLoading = function() {
        return restconnector.getLoading();  
    };
    
    this.getError = function() {
        return restconnector.getError();  
    };
    
    this.isLoggedIn = function() {
        return restconnector.getToken() !== undefined;
    };
    
    this.logout = function() {
        restconnector.logout();
    };

  }]);
