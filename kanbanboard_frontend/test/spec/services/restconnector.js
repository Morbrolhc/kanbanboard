'use strict';

describe('Service: restconnector', function () {

  // load the service's module
  beforeEach(module('kanbanboardFrontendApp'));
  
  var token = "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvIiwic3ViIjoidXNlciIsImRpc3BsYXluYW1lIjoidXNlciIsImxhbmd1YWdlIjoiREUiLCJlbWFpbCI6InVzZXJAZXhhbXBsZS5jb20iLCJyb2xlIjpbeyJhdXRob3JpdHkiOiJST0xFX1VTRVIifV19.UXzKDWrY4RLhUIOI2SDzoOzoUjM-RgH8LUgheoUlkgE";

  // instantiate service
  var restconnector;
  beforeEach(inject(function (_restconnector_) {
    restconnector = _restconnector_;
  }));

  it('should be initialized', function () {
    expect(restconnector.message).toBe('');
    expect(restconnector.loading).toBe(false);
    expect(restconnector.error).toBe(false);
    expect(restconnector.token).toBe(undefined);
  });
  
  it('check checkToken funtion', function () {
    expect(restconnector.checkToken('')).toBe(false);
    expect(restconnector.checkToken(token)).toBe(true);
  });

});
