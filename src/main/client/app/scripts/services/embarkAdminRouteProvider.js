'use strict';

/**
 * @ngdoc service
 * @name rbcembarkAdminApp.routeService
 * @description
 * # routeService
 * Provider of the rbcembarkAdminApp
 */
angular.module('rbcembarkAdminApp')
  .provider('routeService', function ($routeProvider, $locationProvider) {

    var routeProvider = $routeProvider;
    $locationProvider.html5Mode(false);
    return {
      init:function(){
        routeProvider
          .when('/login', {
            templateUrl: 'views/login.html',
            controller: 'LoginCtrl',
            controllerAs: 'login'
          })
          .otherwise({
            redirectTo: '/login'
          });
        },
        $get:['$location','$route',
        function($location, $route){
          return {
            addRouteFromArray: function(arrayOfRoutes) {
              angular.forEach(arrayOfRoutes,this.addRoute);
            },
            addRoute: function(route) {
              routeProvider.when('/'+route.controllerAs,route);
            },
            setOtherwise: function(redirectTo) {
              routeProvider.otherwise({
                redirectTo: redirectTo
              });
            },
            removeRoutes : function() {
              angular.forEach($route.routes,function(route,path){
                if (path !== '/login/' && path !== '/login') {
                  delete($route.routes[path]);
                }
              });
              this.setOtherwise('/login');
            },
            path: function(path) {
              $location.path(path);
            },
            getCurrent: function() {
              return $route.current;
            }
          };
        }]
      };
  });
