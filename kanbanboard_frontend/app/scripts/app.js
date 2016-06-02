'use strict';

/**
 * @ngdoc overview
 * @name kanbanboardFrontendApp
 * @description
 * # kanbanboardFrontendApp
 *
 * Main module of the application.
 */
angular
  .module('kanbanboardFrontendApp', [
    'ngAnimate',
    'ngAria',
    'ngCookies',
    'ngMessages',
    'ngRoute',
    'ngSanitize',
    'ngTouch',
    'ui.bootstrap',
    'angular-jwt',
    'dndLists',
    'ngFileUpload',
    'pascalprecht.translate',
    'ngTagsInput'
  ])
  .config(function ($routeProvider) {
    $routeProvider
      .when('/', {
        templateUrl: 'views/main.html',
        controller: 'MainCtrl',
        controllerAs: 'main'
      })
      .when('/about', {
        templateUrl: 'views/about.html',
        controller: 'AboutCtrl',
        controllerAs: 'about'
      })
      .when('/board', {
        templateUrl: 'views/board.html',
        controller: 'BoardCtrl',
        controllerAs: 'board'
      })
      .when('/login', {
        templateUrl: 'views/login.html',
        controller: 'LoginCtrl',
        controllerAs: 'login'
      })
      .when('/user', {
        templateUrl: 'views/user_details.html',
        controller: 'UserCtrl',
        controllerAs: 'userCtrl'
      })
      .when('/reset/:username/:token', {
        templateUrl: 'views/reset.html',
        controller: 'ResetCtrl',
        controllerAs: 'reset'
      })
      .when('/activation/:username/:token', {
        templateUrl: 'views/activation.html',
        controller: 'ActivationCtrl',
        controllerAs: 'activate'
      })
      .otherwise({
        templateUrl: 'views/login.html',
        controller: 'LoginCtrl',
        controllerAs: 'login'
      });
  })
  .config(function Config($httpProvider, jwtInterceptorProvider) {
    // Please note we're annotating the function so that the $injector works when the file is minified
    jwtInterceptorProvider.tokenGetter = ['restconnector', function(restconnector, jwtHelper) {
        return restconnector.getToken();
    }];
    //$httpProvider.interceptors.push('jwtInterceptor');
    
    $httpProvider.defaults.headers.post['Content-Type'] = 'application/json';
    $httpProvider.defaults.headers.put['Content-Type'] = 'application/json';
    
}).config(function ($translateProvider) {
    $translateProvider
      .preferredLanguage('de')
      .useSanitizeValueStrategy('sanitize')
      .useLoader('languageLoader');   

});
