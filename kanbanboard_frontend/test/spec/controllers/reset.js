'use strict';

describe('Controller: ResetCtrl', function () {

  // load the controller's module
  beforeEach(module('kanbanboardFrontendApp'));

  var ctrl, scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    ctrl = $controller('ResetCtrl', {
      $scope: scope
      // place here mocked dependencies
    });
  }));

  it('should have a controller and scope', function () {
		expect(ctrl).toBeDefined();
		expect(scope).toBeDefined();
	});
});
