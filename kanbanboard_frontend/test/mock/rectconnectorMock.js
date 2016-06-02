'use strict';

angular.module('mock.restconnector2', []).service('restconnectorMock', ['$q', function($q){
    this.errorOccured = false;
    
    var login = {"token":"eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvIiwic3ViIjoidXNlciIsImRpc3BsYXluYW1lIjoidXNlciIsImxhbmd1YWdlIjoiREUiLCJlbWFpbCI6InVzZXJAZXhhbXBsZS5jb20iLCJyb2xlIjpbeyJhdXRob3JpdHkiOiJST0xFX1VTRVIifV19.UXzKDWrY4RLhUIOI2SDzoOzoUjM-RgH8LUgheoUlkgE"};
    
    var getBoards = [{"name":"testboard 01","id":"5741e1625aaaa44224c47208","owner":{"username":"admin","displayname":"admin","email":"admin@example.com"},"tasks":[1,1,0],"users":[{"username":"userDemo0","displayname":"userDemo0","email":"userDemo0@example.com"},{"username":"user","displayname":"user","email":"user@example.com"},{"username":"userDemo1","displayname":"userDemo1","email":"userDemo1@example.com"}]},{"name":"testboard 02","id":"5741e1625aaaa44224c4720b","owner":{"username":"user","displayname":"user","email":"user@example.com"},"tasks":[1,1,0],"users":[{"username":"userDemo0","displayname":"userDemo0","email":"userDemo0@example.com"},{"username":"user","displayname":"user","email":"user@example.com"},{"username":"userDemo1","displayname":"userDemo1","email":"userDemo1@example.com"}]},{"name":"HugeBoard","id":"5741e1625aaaa44224c47205","owner":{"username":"user","displayname":"user","email":"user@example.com"},"tasks":[20,30,40],"users":[{"username":"userDemo8","displayname":"userDemo8","email":"userDemo8@example.com"},{"username":"userDemo0","displayname":"userDemo0","email":"userDemo0@example.com"},{"username":"userDemo6","displayname":"userDemo6","email":"userDemo6@example.com"},{"username":"userDemo2","displayname":"userDemo2","email":"userDemo2@example.com"},{"username":"userDemo7","displayname":"userDemo7","email":"userDemo7@example.com"},{"username":"userDemo3","displayname":"userDemo3","email":"userDemo3@example.com"},{"username":"userDemo4","displayname":"userDemo4","email":"userDemo4@example.com"},{"username":"userDemo5","displayname":"userDemo5","email":"userDemo5@example.com"},{"username":"userDemo9","displayname":"userDemo9","email":"userDemo9@example.com"},{"username":"userDemo1","displayname":"userDemo1","email":"userDemo1@example.com"}]}];
    
    var createNewBoard = {"id":"5741e1ef5aaaa44224c4720e"};
    
    var getBoard = {"name":"testboard 01","id":"5741e1625aaaa44224c47208","tasks":[{"id":"5741e1625aaaa44224c47207","name":"Task 2","state":"DOING","description":"Test description 2","duedate":"2016-05-22","assigned":[{"username":"admin","displayname":"admin","email":"admin@example.com"}],"createdby":{"username":"user","displayname":"user","email":"user@example.com"}},{"id":"5741e1625aaaa44224c47206","name":"Task 1","state":"TODO","description":"Test description","duedate":"2016-05-22","assigned":[{"username":"user","displayname":"user","email":"user@example.com"}],"createdby":{"username":"user","displayname":"user","email":"user@example.com"}}],"assigned":[{"username":"userDemo0","displayname":"userDemo0","email":"userDemo0@example.com"},{"username":"user","displayname":"user","email":"user@example.com"},{"username":"userDemo1","displayname":"userDemo1","email":"userDemo1@example.com"}],"createdby":{"username":"admin","displayname":"admin","email":"admin@example.com"}};
    
    //search user (text: user)
    var getMember = [{"username":"user2","displayname":"user2","email":"user2@example.com"},{"username":"user","displayname":"user","email":"user@example.com"},{"username":"userDemo0","displayname":"userDemo0","email":"userDemo0@example.com"},{"username":"userDemo1","displayname":"userDemo1","email":"userDemo1@example.com"},{"username":"userDemo2","displayname":"userDemo2","email":"userDemo2@example.com"},{"username":"userDemo3","displayname":"userDemo3","email":"userDemo3@example.com"},{"username":"userDemo4","displayname":"userDemo4","email":"userDemo4@example.com"},{"username":"userDemo5","displayname":"userDemo5","email":"userDemo5@example.com"},{"username":"userDemo6","displayname":"userDemo6","email":"userDemo6@example.com"},{"username":"userDemo7","displayname":"userDemo7","email":"userDemo7@example.com"},{"username":"userDemo8","displayname":"userDemo8","email":"userDemo8@example.com"},{"username":"userDemo9","displayname":"userDemo9","email":"userDemo9@example.com"},{"username":"userInactive","displayname":"userInactive","email":"userInactive@example.com"},{"username":"userPass","displayname":"userPass","email":"userPass@example.com"}];
    
    //added userDemo2
    var addedMember = [{"username":"userDemo0","displayname":"userDemo0","email":"userDemo0@example.com"},{"username":"user","displayname":"user","email":"user@example.com"},{"username":"userDemo1","displayname":"userDemo1","email":"userDemo1@example.com"},{"username":"userDemo2","displayname":"userDemo2","email":"userDemo2@example.com"}];
    
    //removed member userDemo2
    var removedMember = [{"username":"userDemo0","displayname":"userDemo0","email":"userDemo0@example.com"},{"username":"user","displayname":"user","email":"user@example.com"},{"username":"userDemo1","displayname":"userDemo1","email":"userDemo1@example.com"}];
    
    //moved Task 1 from ToDo zo Done
    var moveTask = {"id":"5741e1625aaaa44224c47206","name":"Task 1","state":"DONE","description":"Test description","duedate":"2016-05-22","assigned":[{"username":"user","displayname":"user","email":"user@example.com"}],"createdby":{"username":"user","displayname":"user","email":"user@example.com"}};
    
    var taskData = {"id":"5741e1625aaaa44224c47206","name":"Task 1","state":"TODO","description":"Test description","duedate":"2016-05-22","assigned":[{"username":"user","displayname":"user","email":"user@example.com"}],"createdby":{"username":"user","displayname":"user","email":"user@example.com"}};
    
    
     this.handleRequest = function(data){
      var deferred = $q.defer();
      if(this.errorOccured) {
        return deferred.promise.resolve({data: data});
      } else {
        return deferred.promise.reject(data);
      }
    };
    
    //user specific mocks
    this.getToken = function() {
      return 'eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvIiwic3ViIjoidXNlciIsImRpc3BsYXluYW1lIjoidXNlciIsImxhbmd1YWdlIjoiREUiLCJlbWFpbCI6InVzZXJAZXhhbXBsZS5jb20iLCJyb2xlIjpbeyJhdXRob3JpdHkiOiJST0xFX1VTRVIifV19.UXzKDWrY4RLhUIOI2SDzoOzoUjM-RgH8LUgheoUlkgE';
    };
    this.getLanguage = function() {
      return 'DE';
    };
    this.login = function(username, password) {
      if(!this.errorOccured) {
        return this.andleRequest(login);
      } else {
        return this.andleRequest();
      }
    };
    this.hasRole = function(role) {
      
    };
    this.requestPasswordReset = function(emailOrID) {
      
    };
    this.passwordReset = function(username, resetToken, newPassword) {
      
    };
    this.logout = function() {
      
    };
    this.createUser = function() {
      
    };
    this.findUser = function(emailOrID) {
      
    };
    this.getUserInfos = function() {
      var tokenPayload = jwtHelper.decodeToken(this.getToken());
      return tokenPayload;
    };
    this.updateUserInfos = function(displayName, language, oldPassword, newPassword) {
      
    };
    this.deleteUser = function() {
      
    };
    //board specific mocks
    this.createBoard = function(boardname) {
      
    };
    this.getBoards = function() {
      if(!this.errorOccured) {
        return this.andleRequest(getBoards);
      } else {
        return this.andleRequest();
      }
    };
    this.getBoard = function(boardID) {
      if(!this.errorOccured) {
        return this.andleRequest(getBoard);
      } else {
        return this.andleRequest();
      }
    };
    this.updateBoard = function(boarID, newBoard) {
      if(!this.errorOccured) {
        return this.andleRequest(getBoard);
      } else {
        return this.andleRequest();
      }
    };
    this.addBoardMember = function(boardID, board) {
      if(!this.errorOccured) {
        return this.andleRequest();
      } else {
        return this.andleRequest();
      }
    };
    this.deleteBoardMember = function(boardID, boardMember) {
      if(!this.errorOccured) {
        return this.andleRequest();
      } else {
        return this.andleRequest();
      }
    };
    this.deleteBoard = function(boardId) {
      
    };
    //Card specific mocks
    this.getUsersCards = function(searchText) {
      
    };
    this.getCards = function(boardID) {
      
    };
    this.getCard = function(boardID, cardID) {
      
    };
    this.updateCard = function(boardID, card) {
      
    };
    this.updateCardCategory = function(boardID, cardID, categoryName) {
      
    };
    this.deleteCard = function(boardID, cardID) {
      
    };
    //File specific mocks
    this.getFiles = function(boardID, cardID) {
      
    };
    this.deleteFile = function(boardID, cardID, fileID) {
      
    };
    
  }]);