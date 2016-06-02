'use strict';

angular.module('mock.restconnector.main', []).service('restconnectorMock', ['$q', function($q){
    this.errorOccured = false;
    
    var token = "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvIiwic3ViIjoidXNlciIsImRpc3BsYXluYW1lIjoidXNlciIsImxhbmd1YWdlIjoiREUiLCJlbWFpbCI6InVzZXJAZXhhbXBsZS5jb20iLCJyb2xlIjpbeyJhdXRob3JpdHkiOiJST0xFX1VTRVIifV19.UXzKDWrY4RLhUIOI2SDzoOzoUjM-RgH8LUgheoUlkgE";
    
    var getBoardsData = [{"name":"testboard 01","id":"5741e1625aaaa44224c47208","owner":{"username":"admin","displayname":"admin","email":"admin@example.com"},"tasks":[1,1,0],"users":[{"username":"userDemo0","displayname":"userDemo0","email":"userDemo0@example.com"},{"username":"user","displayname":"user","email":"user@example.com"},{"username":"userDemo1","displayname":"userDemo1","email":"userDemo1@example.com"}]},{"name":"testboard 02","id":"5741e1625aaaa44224c4720b","owner":{"username":"user","displayname":"user","email":"user@example.com"},"tasks":[1,1,0],"users":[{"username":"userDemo0","displayname":"userDemo0","email":"userDemo0@example.com"},{"username":"user","displayname":"user","email":"user@example.com"},{"username":"userDemo1","displayname":"userDemo1","email":"userDemo1@example.com"}]},{"name":"HugeBoard","id":"5741e1625aaaa44224c47205","owner":{"username":"user","displayname":"user","email":"user@example.com"},"tasks":[20,30,40],"users":[{"username":"userDemo8","displayname":"userDemo8","email":"userDemo8@example.com"},{"username":"userDemo0","displayname":"userDemo0","email":"userDemo0@example.com"},{"username":"userDemo6","displayname":"userDemo6","email":"userDemo6@example.com"},{"username":"userDemo2","displayname":"userDemo2","email":"userDemo2@example.com"},{"username":"userDemo7","displayname":"userDemo7","email":"userDemo7@example.com"},{"username":"userDemo3","displayname":"userDemo3","email":"userDemo3@example.com"},{"username":"userDemo4","displayname":"userDemo4","email":"userDemo4@example.com"},{"username":"userDemo5","displayname":"userDemo5","email":"userDemo5@example.com"},{"username":"userDemo9","displayname":"userDemo9","email":"userDemo9@example.com"},{"username":"userDemo1","displayname":"userDemo1","email":"userDemo1@example.com"}]}];
    
    this.handleRequest = function(data){
      var deferred = $q.defer();
      setTimeout(function() {
          deferred.resolve({data: data});
      }, 0);
      return deferred.promise;
    };
    
    this.getBoards = function() {
      return this.handleRequest(getBoardsData);
    };
    
    this.getUserInfos = function() {
      return {displayname: 'admin'};
    };
    
    this.getLanguage = function() {
      return 'DE';
    };
    
    this.getToken = function() {
      return token;
    };
    
    this.getUsersCards = function() {
      return this.handleRequest({data: {tasks: [1,2,3]}});
    };

  }]);
  
describe('Controller: MainCtrl', function () {

  // load the controller's module
  beforeEach(module('kanbanboardFrontendApp'));
  beforeEach(module('mock.restconnector.main'));
  var ctrl, scope, restconnector, rootScope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope, restconnectorMock, $http, $httpBackend) {
    rootScope = $rootScope;
    scope = $rootScope.$new();
    restconnector = restconnectorMock;
    
    ctrl = $controller('MainCtrl', {
      //mocked dependencies
      $scope: scope,
      restconnector: restconnectorMock,
      $httpBackend: $httpBackend
    });
  }));
  
  it('should have a controller and scope', function () {
		expect(ctrl).toBeDefined();
		expect(scope).toBeDefined();
	});

  it('load data and set it correctly to fields', function () {
    expect(ctrl.boards).toBeUndefined();
    ctrl.loadData();
    rootScope.$apply();
    expect(ctrl.userInfos).toBeDefined();
  });
});
