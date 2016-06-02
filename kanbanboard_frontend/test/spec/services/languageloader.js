'use strict';

describe('Service: languageLoader', function () {

  // load the service's module
  beforeEach(module('kanbanboardFrontendApp'));

  // instantiate service
  var languageLoader;
  beforeEach(inject(function (_languageLoader_) {
    languageLoader = _languageLoader_;
  }));

  it('should load language EN', function () {
    expect(languageLoader({key: 'en'})).toBeDefined();
  });

});
