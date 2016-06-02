'use strict';

describe('Controller: ActivationCtrl', function () {

  // load the controller's module
  beforeEach(module('kanbanboardFrontendApp'));

  var ctrl, scope, $location;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope, _$location_) {
    scope = $rootScope.$new();
    $location = _$location_;
    spyOn($location, 'path').and.returnValue('a/#/username/token');
    ctrl = $controller('ActivationCtrl', {
      $scope: scope,
      // place here mocked dependencies
    });
  }));

  it('should have a controller and scope', function () {
		expect(ctrl).toBeDefined();
		expect(scope).toBeDefined();
	});
  
  it('check initialization', function () {
    expect(ctrl.error).toBe('');
    expect(ctrl.activated).toBe(false);
	});
});
