'use strict';

/**
 * @ngdoc function
 * @name kanbanboardFrontendApp.controller:AboutCtrl
 * @description
 * # AboutCtrl
 * Controller of the kanbanboardFrontendApp
 */
angular.module('kanbanboardFrontendApp')
  .controller('AboutCtrl', ['restconnector', '$translate', function (restconnector, $translate) {
    this.language = 'DE';
    
    this.changeLanguage = function() {
        var language = restconnector.getLanguage();
        if(language === undefined) {
          return;
        }
        language = angular.lowercase(language);
        if($translate.use() !== language) {
            $translate.use(language);
        } 
    };
    
    if(restconnector.getToken() !== undefined) {
      this.changeLanguage();
    }
  }]);
