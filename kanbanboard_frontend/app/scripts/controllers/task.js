'use strict';

/**
 * @ngdoc function
 * @name kanbanboardFrontendApp.controller:TaskCtrl
 * @description
 * # TaskCtrl
 * Controller of the kanbanboardFrontendApp
 */
angular.module('kanbanboardFrontendApp')
  .controller('TaskCtrl', ['$uibModalInstance', 'param', 'restconnector', '$window', '$location', '$translate', '$q', 'confirmService',
        function ($uibModalInstance, param, restconnector, $window, $location, $translate, $q, confirmService) {
    //Variables
    var scope = this;
    this.boardID = param.boardID;
    this.card = param.card;
    this.cardCategory = param.cardCategory;
    this.boardmembers = param.boardmembers;
    this.memberToAdd = undefined;
    this.files = [];
    this.filesToUpload = [];
    this.assigned = [];
    this.cardOwner = '';
    this.cardName = '';

    this.getCardData = function() {
        this.userInfos = restconnector.getUserInfos();
        if(this.card === undefined) {
          this.cardOwner = this.userInfos.displayname;
          this.assigned.push({username: this.userInfos.sub, displayname: this.userInfos.displayname, email: this.userInfos.email });
          this.dueDate = new Date();
          return;
        }
        this.boardID = this.card.boardId;
        this.cardID = this.card.id;
        restconnector.getCard(this.boardID, this.cardID)
        .then(function(responseOK) {
            scope.card = responseOK.data;

            //Model values
            scope.cardName = scope.card.name;
            scope.dueDate = new Date(scope.card.duedate);
            scope.description = scope.card.description;
            scope.cardCategory = scope.card.state;
            scope.assigned = scope.card.assigned;
            scope.cardOwner = scope.card.createdby.displayname;
        });
        restconnector.getFiles(this.boardID, this.cardID)
        .then(function(responseOK) {
            if(responseOK.data.constructor === Array){
                scope.files = responseOK.data;
            }
        }, function() {
            scope.files = [];
        });
    };

    this.loadTranslate = function() {
        $translate(['confirmTask', 'questionTask', 'confirmFile', 'questionFile']).then(function (translations) {
            scope.textConfirmTask = translations.confirmTask;
            scope.textQuestionTask = translations.questionTask;
            scope.textConfirmFile = translations.confirmFile;
            scope.textQuestionFile = translations.questionFile;
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
        $location.path('/login');
    } else {
        this.getCardData();
        this.changeLanguage();
        this.loadTranslate();
    }

    //Functions
    this.isNewTask = function() {
        return this.cardID === undefined;
    };

    this.getTags = function() {
      return $q.resolve(this.boardmembers);
    };
    //allow removal of an assigned user
    this.removeAllowed = function($tag) {
        if(this.card === undefined) {
            return true;
        }
        return true;
    };

    this.addMember = function() {
        if(this.memberToAdd !== undefined) {
            this.assigned.push(this.memberToAdd);
            this.memberToAdd = undefined;
        }
    };

    this.deleteMember = function(member) {
         this.assigned.splice(this.assigned.indexOf(member));
    };

    this.downloadPath = function(fileID) {
        return '/wodss3/api/boards/' + this.boardID + '/cards/' + this.cardID + '/files/' + fileID;
    };

    this.uploadFiles = function() {
      for (var i = 0; i < this.filesToUpload.length; i++) {
        var $file = this.filesToUpload[i];
        restconnector.uploadFile(this.boardID, this.cardID, $file)
        .then(function(resultOK) {
            // file is uploaded successfully
            var newFile = {
                id: resultOK.data.id,
                filename: $file.name
            };
            scope.files.push(newFile);
            if(i === scope.filesToUpload.length - 1) {
                scope.filesToUpload = [];
            }
        });
      }
    };

    this.deleteFile = function(fileID) {
        confirmService.openModal(this.textConfirmFile, this.textQuestionFile)
        .then(function(data) {
            if(data.result === 'OK') {
                restconnector.deleteFile(scope.boardID, scope.cardID, fileID)
                .then(function() {
                    for(var i = 0; i < scope.files.length; i++) {
                        if(scope.files[i].id === fileID) {
                            scope.files.splice(i, 1);
                            return;
                        }
                    }
                });
            }
        });
    };

    this.saveCard = function() {
        var creator = {username: this.userInfos.sub, displayname: this.userInfos.displayname, email: this.userInfos.email };
        var newCard = {id: this.cardID,
                name: this.cardName,
                duedate: this.dueDate,
                state: this.cardCategory,
                createdby: creator,
                assigned: this.assigned,
                description: this.description
            };
        if(this.cardName.length > 1) {
            if(this.card !== undefined) {
                newCard.createdby = this.card.createdby;
                newCard.boardid = this.card.boardid;
                restconnector.updateCard(this.boardID, newCard)
                .then(function() {
                    $uibModalInstance.close('OK');
                });
            } else {
                restconnector.createCard(this.boardID, newCard)
                .then(function(responseOK) {
                    $uibModalInstance.close(responseOK.data);
                });
            }
        }
    };

    this.deleteCard = function() {
        confirmService.openModal(this.textConfirmTask, this.textQuestionTask)
        .then(function(data) {
            if(data.result === 'OK') {
                restconnector.deleteCard(scope.boardID, scope.cardID)
                .then(function() {
                    $uibModalInstance.close('OK');
                });
            }
        });
    };

    this.cancel = function() {
        $uibModalInstance.close('Cancel');
    };

 }]);


