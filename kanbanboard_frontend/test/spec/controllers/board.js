'use strict';

describe('Controller: BoardCtrl', function () {
  // load the controller's module
  beforeEach(module('kanbanboardFrontendApp'));

  beforeEach(angular.mock.module('ngCookies'));  
   
  var ctrl, scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope, $cookies) {
    scope = $rootScope.$new();
     
    ctrl = $controller('BoardCtrl', {
      $scope: scope,
      $cookies: $cookies
      // place here mocked dependencies
    });
  }));
  
  it('should have a controller and scope', function () {
		expect(ctrl).toBeDefined();
		expect(scope).toBeDefined();
	});
  
  it('check initialization', function () {
    expect(ctrl.searchText).toBe('');
    expect(ctrl.language).toBe('DE');
    expect(ctrl.editMembers).toBe(false);
    expect(ctrl.todoDropList.length).toBe(0);
    expect(ctrl.doingDropList.length).toBe(0);
    expect(ctrl.doneDropList.length).toBe(0);
    expect(ctrl.assigned.length).toBe(0);
	});

});
