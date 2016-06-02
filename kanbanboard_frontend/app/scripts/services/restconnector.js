'use strict';

/**
 * @ngdoc service
 * @name kanbanboardFrontendApp.restconnector
 * @description
 * # restconnector
 * Service in the kanbanboardFrontendApp.
 */
angular.module('kanbanboardFrontendApp')
  .service('restconnector',['$http', 'jwtHelper', '$q', 'Upload', '$cookies',
    function ($http, jwtHelper, $q, Upload, $cookies) {
    // AngularJS will instantiate a singleton by calling 'new' on this function
    var pathPrefix = '/wodss3';
    
    var scope = this;
    this.message = '';
    this.loading = false;
    this.error = false;
    this.token = undefined;

    this.getMessage = function() {
      return this.message;
    };
    this.getLoading = function() {
      return this.loading;
    };
    this.getError = function() {
      return this.error;
    };

    //Global error handling
    //Method to separate request for onSuccess and onError
    this.handleRequest = function(promise, workDescription){
      this.loading = true;
      this.message = workDescription;
      var deferred = $q.defer();
      promise.success(function(data, status, headers, config) {
        scope.successLoading(data, headers, deferred);
      })
      .error(function(data, status, headers, config) {
        scope.errorLoading(data, status, headers, deferred, workDescription);
      });
      return deferred.promise;
    };
    //sucess handling
    this.successLoading = function(data, headers, deferred) {
      scope.loading = false;
      scope.error = false;
      scope.message = '';
      //this.saveToken(headers);
      return deferred.resolve({data: data});
    };
    //error handling
    this.errorLoading = function(data, code, headers, deferred, workDescription) {
      scope.loading = false;
      scope.error = true;
      scope.message = 'could not ' + workDescription + ' ';
      if(code === 401) {
        scope.message += '(unauthorized)';
      } else if(code === 404) {
        scope.message += '(server path not valid)';
      } else if(code === 409 && data.result !== undefined) {
        scope.message += data.result;
      } else {
        scope.message += '(no connection?)';
      }
      if(data.result !== undefined) {
        scope.message = data.result;
      }
      return deferred.reject(scope.message);
    };
    
    //true if valid
    this.checkToken = function(token) {
      if(token === undefined || token === null) {
        return false;  
      }
      if(token.split('.').length !== 3) {
        return false;
      }
      if(jwtHelper.isTokenExpired(token)){
        return false;
      }
      return true;
    };

    this.saveTokenFromHeader = function(headers) {
      var cookie = headers("Set-Cookie");
      if(cookie === undefined || cookie === null) {
        cookie = headers("Cookie");
      }
      if(cookie !== undefined && cookie !== null) {
        if(cookie.token !== undefined) {
          if(this.checkToken(cookie.token)) {
            $cookies.put('token', cookie.token);
            this.token = cookie.token;
          }         
        }
      }
    };

    this.saveToken = function(token) {
      var cookies = $cookies.getAll();
      if(this.checkToken(token)) {
        if(cookies.token !== undefined) {
          delete $cookies.token;
        }
        var expireDate = jwtHelper.getTokenExpirationDate(token);
        $cookies.put("token", token, {
            expires: expireDate
        });
        this.token = token;
      }
      return $q.resolve();
    };

    this.getToken = function() {
      var cookies = $cookies.getAll();
      if(cookies.token === undefined) {
        if(this.checkToken(this.token)) {
          return this.token;
        }
        return undefined;
      }
      if(this.checkToken(this.token)) {
        return cookies.token;
      }
      return undefined;
    };

    this.getUserId = function() {
      var token = this.getToken();
      if(token === undefined) {
        return '';
      }
      var tokenPayload = jwtHelper.decodeToken(token);
      return tokenPayload.sub;
    };

    this.getLanguage = function() {
      var token = this.getToken();
      if(token !== undefined) {
        var tokenPayload = jwtHelper.decodeToken(token);
        return tokenPayload.language;
      }
      return;
    };

    //user specific rest methods
    this.login = function(uname, passwd) {
      var request = $http({
        method: 'POST',
        url: pathPrefix + '/api/login',
        skipAuthorization: true,
        data: {username: uname, password: passwd}
      });
      return this.handleRequest(request, 'login');
    };
    this.activateUser = function(username, activationToken) {
       var request = $http({
        method: 'POST',
        url: pathPrefix + '/api/users/' + username + '/activate/',
        skipAuthorization: true,
        data: {token: activationToken}
      });
      return this.handleRequest(request, 'activation');
    };
    this.hasRole = function(role) {
      var request = $http.get(pathPrefix + '/api/role/' + role);
      return this.handleRequest(request, 'load roles');
    };
    this.requestPasswordReset = function(email) {
      var request = $http({
       url: pathPrefix + '/api/users/resetPassword/',
       method: 'POST',
       skipAuthorization: true,
       data: {text: email}
      });
      return this.handleRequest(request, 'request password reset');
    };
    this.checkResetToken = function(username, resetToken) {
      var request = $http({
       url: pathPrefix + '/api/users/' + username + "/resetPassword/" ,
       method: 'GET',
       skipAuthorization: true,
       params: {token: resetToken}
      });
      return this.handleRequest(request, 'reset password');
    };
    this.passwordReset = function(username, resetToken, newPassword) {
      var request = $http({
       url: pathPrefix + '/api/users/' + username + "/resetPassword/",
       method: 'POST',
       skipAuthorization: true,
       data: {token: resetToken, password: newPassword}
      });
      return this.handleRequest(request, 'reset password');
    };
    this.logout = function() {
       localStorage.removeItem('token');
       $cookies.remove('token');
       scope.token = undefined;
       //return $http.post(pathPrefix + '/api/logout', {username: uname, password: passwd});
    };
    this.createUser = function(uname, passwd, displayName, email, language) {
      var request = $http.post(pathPrefix + '/api/users', {username: uname, password: passwd, email: email, displayname: displayName, language: language});
      return this.handleRequest(request, 'create user');
    };
    this.findUser = function(emailOrID) {
      var request = $http.get(pathPrefix + '/api/users/findusers/' + emailOrID);
      return this.handleRequest(request, 'search users');
    };
    this.getUserInfos = function() {
      var token = this.getToken();
      if(token === undefined) {
        return '';
      }
      var tokenPayload = jwtHelper.decodeToken(token);
      return tokenPayload;
    };
    this.updateUserInfos = function(displayName, language, oldPassword, newPassword) {
      var request = $http.put(pathPrefix + '/api/users/me',
      {displayname: displayName, language: language, oldPassword: oldPassword, password: newPassword});
      return this.handleRequest(request, 'update user infos');
    };
    this.deleteUser = function() {
      var request = $http.delete(pathPrefix + '/api/users/' + this.getUserId());
      return this.handleRequest(request, 'delete user');
    };

    //board specific rest methos
    this.createBoard = function(boardName) {
      var request = $http.post(pathPrefix + '/api/boards/', {name: boardName});
      return this.handleRequest(request, 'create board');
    };
    this.getBoards = function() {
      var request = $http.get(pathPrefix + '/api/users/' + this.getUserId() + '/boards', {});
      return this.handleRequest(request, 'load boards');
    };
    this.getBoard = function(boardID) {
      var request = $http.get(pathPrefix + '/api/boards/' + boardID, {});
      return this.handleRequest(request, 'load board');
    };
    this.updateBoard = function(boardId, board) {
      var request = $http.put(pathPrefix + '/api/boards/' + boardId, board);
      return this.handleRequest(request, 'update board');
    };
    this.addBoardMember = function(boardId, boardMember) {
      var request = $http.put(pathPrefix + '/api/boards/' + boardId + '/members', boardMember);
      return this.handleRequest(request, 'add member to board');
    };
    this.deleteBoardMember = function(boardId, boardMember) {
      var request = $http.delete(pathPrefix + '/api/boards/' + boardId + '/members/' + boardMember.username);
      return this.handleRequest(request, 'delete member from board');
    };
    this.deleteBoard = function(id) {
      var request = $http.delete(pathPrefix + '/api/boards/' + id);
      return this.handleRequest(request, 'delete board');
    };

    //card specific rest methods
    this.getUsersCards = function(pageNr, searchText) {
      var requestParams = {};
      if(pageNr !== 0) {
        requestParams.page = pageNr;
      }
      if(searchText !== '') {
        requestParams.page = pageNr;
        requestParams.search = searchText;
      }
      var request = $http({
        url: pathPrefix + '/api/users/' + this.getUserId() + '/cards',
        method: 'GET',
        params: requestParams
      });
      return this.handleRequest(request, 'search cards');
    };
    this.createCard = function(boardID, newCard) {
      var request = $http.post(pathPrefix + '/api/boards/' + boardID + '/cards', newCard);
      return this.handleRequest(request, 'create card');
    };
    this.getCards = function(boardID) {
      var request = $http.get(pathPrefix + '/api/boards/' + boardID + '/cards/');
      return this.handleRequest(request, 'load cards');
    };
    this.getCard = function(boardID, cardID) {
      var request = $http.get(pathPrefix + '/api/boards/' + boardID + '/cards/' + cardID);
      return this.handleRequest(request, 'load card');
    };
    this.updateCard = function(boardID, card) {
      var request = $http.put(pathPrefix + '/api/boards/' + boardID + '/cards/' + card.id, card);
      return this.handleRequest(request, 'update card');
    };
    this.updateCardCategory = function(boardID, cardID, categoryName) {
      var request = $http.put(pathPrefix + '/api/boards/' + boardID + '/cards/' + cardID + '/category', {state: categoryName});
      return this.handleRequest(request, 'update card category');
    };
    this.deleteCard = function(boardID, cardID) {
      var request = $http.delete(pathPrefix + '/api/boards/' + boardID + '/cards/' + cardID);
      return this.handleRequest(request, 'delete card');
    };

    //file specific rest methods
    this.getFiles = function(boardID, cardID) {
      var request = $http.get(pathPrefix + '/api/boards/' + boardID + '/cards/' + cardID + '/files');
      return this.handleRequest(request, 'load files');
    };

    this.uploadFile = function(boardID, cardID, $file) {
      var request = Upload.upload({
            url: pathPrefix + '/api/boards/' + boardID + '/cards/' + cardID + '/files',
            method: 'POST',
            data: {
              name: $file.name,
              file: $file,
            },
            progress: function(e){}
      });
      return this.handleRequest(request, 'upload files');
    };
    this.getFile = function(boardID, cardID, fileID) {
      var request = $http.get(pathPrefix + '/api/boards/' + boardID + '/cards/' + cardID + '/files/' + fileID);
      return this.handleRequest(request, 'download file');
    };
    this.deleteFile = function(boardID, cardID, fileID) {
      var request = $http.delete(pathPrefix + '/api/boards/' + boardID + '/cards/' + cardID + '/files/' + fileID);
      return this.handleRequest(request, 'delete file');
    };

  }]);
