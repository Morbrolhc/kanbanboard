'use strict';

describe('Controller: ConfirmCtrl', function () {

  // load the controller's module
  beforeEach(module('kanbanboardFrontendApp'));

  var ctrl, scope, $uibModalInstance;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    $uibModalInstance = jasmine.createSpyObj('$uibModalInstance', ['close', 'dismiss']);
    ctrl = $controller('ConfirmCtrl', {
      $scope: scope,
      $uibModalInstance: $uibModalInstance,
      param: {title: 'test', question: 'question?'},
      // place here mocked dependencies
    });
  }));
  
  it('should have a controller and scope', function () {
		expect(ctrl).toBeDefined();
		expect(scope).toBeDefined();
	});
  
});
