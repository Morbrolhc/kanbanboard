'use strict';

/**
 * @ngdoc function
 * @name kanbanboardFrontendApp.controller:BoardCtrl
 * @description
 * # BoardCtrl
 * Controller of the kanbanboardFrontendApp
 */
angular.module('kanbanboardFrontendApp')
  .controller('BoardCtrl', ['$uibModal', '$location', '$routeParams', 'restconnector', '$window', '$translate', '$rootScope', 'confirmService', 
         function ($uibModal, $location, $routeParams, restconnector, $window, $translate, $rootScope, confirmService) {
    //Models
    var scope = this;
    this.boardID = $location.search().id;
    this.searchText = '';
    this.selected = null;
    this.todoDropList = [];
    this.doingDropList = [];
    this.doneDropList = [];
    this.assigned = [];
    this.editMembers = false;
    this.language = 'DE';
    
    //All assigned tasks elements
    this.allTaskPage = 0;
    this.allTaskPageCount = 1;
    this.allTaskPageSize = 20;
    
    this.models = {
        selected: null
    };
    
    //This funcitons are needed for initializing and loading data 
    this.loadBoardData = function() {
        restconnector.getBoard(this.boardID)
        .then(function(response) {
            scope.boardData = response.data;
            scope.assigned = [];
            scope.assigned.push(scope.boardData.owner);
            for(var i = 0; i < scope.boardData.assigned.length; i++) {
                if(scope.boardData.assigned[i].username !== scope.boardData.owner.username) {
                    scope.assigned.push(scope.boardData.assigned[i]);
                }
            }
            scope.boardName = scope.boardData.name;
            scope.cardsToLists(response.data.tasks);
        });
    };
    
    this.loadTranslate = function() {
        $translate(['confirmTask', 'questionTask', 'boardOwnerConfirm', 'boardOwnerMessage']).then(function (translations) {
            scope.textConfirmTask = translations.confirmTask;
            scope.textQuestionTask = translations.questionTask;
            scope.boardOwnerConfirm = translations.boardOwnerConfirm;
            scope.boardOwnerMessage = translations.boardOwnerMessage;
        });
    };
    
    this.changeLanguage = function() {
        this.language = restconnector.getLanguage();
        if(this.language === undefined) {
          return;
        }
        this.language = angular.lowercase(this.language);
        if($translate.use() !== this.language) {
            $translate.use(this.language);
            scope.loadTranslate();
        }
    };
    
    this.loadAllTasks = function() {
        if(this.boardID !== 'all') {
            return;
        }
        restconnector.getUsersCards(this.allTaskPage, this.searchText)
        .then(function(response) {
            scope.boardData = response.data;
            scope.cardsToLists(response.data.content);
            scope.allTaskPage = response.data.page;
            scope.allTaskPageCount = response.data.pagecount;
            scope.allTaskPageSize = response.data.pagesize;
        });
    };
    
    //Initialize and load Data
    if(restconnector.getToken() === undefined) {
        $location.path('/login');
    } else if(this.boardID === 'all') {
        this.loadAllTasks();
        this.changeLanguage();
        this.loadTranslate();
        $translate('allTasks').then(function (allTasks) {
            scope.boardName = allTasks;
        });
    } else {
        this.loadBoardData();
        this.changeLanguage();
        this.loadTranslate();
    }

    //Functions
    this.cardsToLists = function(cards) {
        this.todoDropList = [];
        this.doingDropList = [];
        this.doneDropList = [];
        for (var i = 0; i < cards.length; i++) {
            var card = cards[i];
            if(card.state === 'TODO') {
                this.todoDropList.push(card);
            } else if (card.state === 'DOING') {
                this.doingDropList.push(card);
            } else if (card.state === 'DONE') {
                this.doneDropList.push(card);
            }
        }
    };
    
    this.removeMember = function(member) {
        var index = this.assigned.indexOf(member);
        if(index >= 0) {
           this.assigned.splice(index, 1); 
        }
    };
    
    this.updateBoard = function() {
        if(this.boardName.length > 1) {
            var newAssigned = [];
            for(var i = 0; i < scope.assigned.length; i++) {
                if(scope.assigned[i].username !== scope.boardData.owner.username) {
                    newAssigned.push(scope.assigned[i]);
                }
            }
            var newBoard = {
                name: this.boardName,
                owner: this.boardData.owner,
                members: newAssigned
            };
            restconnector.updateBoard(this.boardID, newBoard)
            .then(function(response) {
                //no error
            }, function(error) {
                scope.boardName = scope.boardData.name;
            });
        }
    };
    
    this.deleteTask = function(card) {
        confirmService.openModal(this.textConfirmTask, this.textQuestionTask)
        .then(function(data) {
            if(data.result === 'OK') {
                restconnector.deleteCard(card.boardId, card.id)
                .then(function() {
                    if(scope.boardID === 'all') {
                        scope.loadAllTasks();
                    } else {
                        scope.loadBoardData();
                    }
                });
            }
        });
    };
    //Methods for User add and remove
    this.getTags = function($query) {
        return restconnector.findUser($query);
    };
    
    this.removeAllowed = function($tag) {
        var allowed = true;
        if(this.boardData.owner.username === $tag.username) {
            confirmService.openModal(this.boardOwnerConfirm, this.boardOwnerMessage);
            return false;
        }
        return allowed;
    };
    
    this.removedMember = function($tag) {
        restconnector.deleteBoardMember(this.boardID, $tag)
        .then(function() {
            
        }, function() {
            //reinsert user
            scope.assigned.push($tag);
        });
    };
    
    this.addedMember = function($tag) {
        restconnector.addBoardMember(this.boardID, $tag);
    };
    
    this.updateCardCategory = function(card, category) {
        restconnector.updateCardCategory(card.boardId, card.id, category)
        .then(function(result) {
            var list = card.state === 'TODO' ? scope.todoDropList : card.state === 'DOING' ? scope.doingDropList : scope.doneDropList;
            var found = false;
            for(var i = 0; i < list.length && !found; i++) {
                if(list[i].id === card.id) {
                    list.splice(i, 1);
                    found = true;
                }
            }
            card.state = category;
            if(card.state === 'TODO') {
                scope.todoDropList.push(card);
            } else if (card.state === 'DOING') {
                scope.doingDropList.push(card);
            } else if (card.state === 'DONE') {
                scope.doneDropList.push(card);
            }
            return true;
        }, function()  {
            return false;
        });  
    };

    //Modals
    this.openTaskModal = function(card, taskCategory) {
		$uibModal.open({
		      templateUrl: 'views/task.html',
		      controller: 'TaskCtrl as taskCtrl',
              windowClass: 'app-modal-window',
              resolve: {
                   param: function () {
                       return {boardID: scope.boardID, card: card, taskCategory: taskCategory, boardmembers: scope.assigned};
                   }
               }	    	  
		    })
        .result.then(function() {
            if(restconnector.getToken() === undefined) {
                $location.path('/login');
            } else if(scope.boardID === 'all') {
                scope.loadAllTasks();
                scope.changeLanguage();
                $translate('allTasks').then(function (allTasks) {
                    scope.boardName = allTasks;
                });
            } else {
                scope.loadBoardData();
                scope.changeLanguage();
            }
		});		
	};
    
    this.openNewTaskModal = function(cardCategory) {
		this.modal = $uibModal.open({
		      templateUrl: 'views/task.html',
		      controller: 'TaskCtrl as taskCtrl',
              windowClass: 'app-modal-window',
              resolve: {
                   param: function () {
                       return {boardID: scope.boardID, cardCategory: cardCategory, boardmembers: scope.assigned};
                   }
               }	    	  
		    })
        .result.then(function() {
            if(scope.boardID === 'all') {
                return;    
            }
            scope.loadBoardData();
		});		
	};
        
  }]);
