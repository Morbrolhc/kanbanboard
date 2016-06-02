'use strict';

describe('Controller: TaskCtrl', function () {

  // load the controller's module
  beforeEach(module('kanbanboardFrontendApp'));

  var ctrl, scope, $uibModalInstance;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    $uibModalInstance = jasmine.createSpyObj('$uibModalInstance', ['close', 'dismiss']);
    ctrl = $controller('TaskCtrl', {
      $scope: scope,
      $uibModalInstance: $uibModalInstance,
      param: {card: {boardId: '0', name: 'test', duedate:'2016-10-1', state:'TODO', assigned: [], createdby: {username: 'test'}},
               cardCategory: 'TODO', boardmembers: []},
      // place here mocked dependencies
    });
  }));
  
  it('should have a controller and scope', function () {
		expect(ctrl).toBeDefined();
		expect(scope).toBeDefined();
	});
});
