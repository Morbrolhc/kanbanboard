'use strict';

describe('Controller: UserCtrl', function () {

  // load the controller's module
  beforeEach(module('kanbanboardFrontendApp'));

  var ctrl, scope;
  
  localStorage.token = "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvIiwic3ViIjoidXNlciIsImRpc3BsYXluYW1lIjoidXNlciIsImxhbmd1YWdlIjoiREUiLCJlbWFpbCI6InVzZXJAZXhhbXBsZS5jb20iLCJyb2xlIjpbeyJhdXRob3JpdHkiOiJST0xFX1VTRVIifV19.UXzKDWrY4RLhUIOI2SDzoOzoUjM-RgH8LUgheoUlkgE"; 

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    ctrl = $controller('UserCtrl', {
      $scope: scope
      // place here mocked dependencies
    });
  }));

  it('should have a controller and scope', function () {
		expect(ctrl).toBeDefined();
		expect(scope).toBeDefined();
	});
});
