'use strict';

/**
 * @ngdoc overview
 * @name rbcembarkAdminApp
 * @description
 * # RBCEmbarkAdminApp
 *
 * Main module of the application.
 */
angular
  .module('rbcembarkAdminApp', [
    'ngAnimate',
    'ngAria',
    'ngCookies',
    'ngMessages',
    'ngResource',
    'ngRoute',
    'ngSanitize',
    'cfp.loadingBar',
    'ngTouch',
    'ui.tree',
    'ui.grid',
    'ui.grid.edit',
    'ui.grid.selection',
    'ui.grid.autoResize',
    'ui.grid.cellNav',
    'ui.bootstrap',
    'ngInputModified'
  ])
  .config(['routeServiceProvider', 'restServiceProvider', 'cfpLoadingBarProvider','$httpProvider',
  function (routeServiceProvider, restServiceProvider,cfpLoadingBarProvider, $httpProvider) {
    routeServiceProvider.init();
    restServiceProvider.setBaseUrl(uri);
    $httpProvider.defaults.withCredentials = true;
    // loading bar
    cfpLoadingBarProvider.includeSpinner = true;
    cfpLoadingBarProvider.spinnerTemplate = '<div><span class="fa fa-spinner load-overlay "><div class="center">Loading...</div></div>';
  }]).run(function($rootScope, $cookies, routeService) {
    $rootScope.user = $cookies.getObject('authenticatedUser');
    $rootScope.alert = {};
    $rootScope.forms = {};
    if ($rootScope.user !== undefined) {
      $rootScope.isSuperuser = $rootScope.user.isSuperuser;
      routeService.addRouteFromArray($rootScope.user.routes);
      routeService.setOtherwise('/homepage');
      //routeService.path('/');
    }
  }).filter('getIndexById', function() {
  return function(anArray, id) {
    if (angular.isArray(anArray)) {
      return anArray.findIndex(function(e) { return e._id === id});
    } else {
      return null;
    }
  }
});
