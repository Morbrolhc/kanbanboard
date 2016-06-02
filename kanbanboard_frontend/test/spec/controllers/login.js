'use strict';

angular.module('mock.restconnector.login', []).service('restconnector', ['$q', function($q){
    this.errorOccured = false;
    
    var loginData = {"token":"eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvIiwic3ViIjoidXNlciIsImRpc3BsYXluYW1lIjoidXNlciIsImxhbmd1YWdlIjoiREUiLCJlbWFpbCI6InVzZXJAZXhhbXBsZS5jb20iLCJyb2xlIjpbeyJhdXRob3JpdHkiOiJST0xFX1VTRVIifV19.UXzKDWrY4RLhUIOI2SDzoOzoUjM-RgH8LUgheoUlkgE"};
    
     this.handleRequest = function(data){
      var deferred = $q.defer();
      setTimeout(function() {
          deferred.resolve({data: data});
      }, 10);
      return deferred.promise;
    };
    
    this.login = function() {
      return this.handleRequest(loginData);
    };

  }]);

describe('Controller: LoginCtrl', function () {

  // load the controller's module
  beforeEach(module('kanbanboardFrontendApp'));
  beforeEach(module('mock.restconnector.login'));
  beforeEach(module('ngCookies'));

  var ctrl, scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope, _restconnector_) {
    scope = $rootScope.$new();
    ctrl = $controller('LoginCtrl',  {
      // mocked dependencies
      $scope: scope,
      restconnector: _restconnector_
    });
  }));
  
  it('should have a controller and scope', function () {
		expect(ctrl).toBeDefined();
		expect(scope).toBeDefined();
	});

  it('should login and set data correctly', function () {
    expect(ctrl.username.length).toBe(0);
    expect(ctrl.password.length).toBe(0);
    ctrl.password = 'test';
    ctrl.login();
    expect(ctrl.username.length).toBe(0);
    expect(ctrl.password.length).toBe(0);
  });
});
