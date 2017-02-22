'use strict';

/**
 * @ngdoc service
 * @name rbcembarkAdminApp.restService
 * @description
 * # restService
 * Provider of the rbcembarkAdminApp
 */
angular.module('rbcembarkAdminApp')
  .provider('restService',function() {
    var _baseUrl = '';


    var _configRequest = {
      'method': 'POST',
      'data': {},
      'withCredentials': true,
      'headers': { 'Content-Type': 'application/json;charset=utf-8' }
    };


    this.setBaseUrl = function(url) {
      _baseUrl = url;
    };

    var encode4DataForm = function (data) {
      var formData = new FormData();
      for (var key in data) {
        formData.append(key, data[key]);
      }
      return formData;
    };

    var encode4form = function (data) {
        var result = '';
        var first = true;
        for (var key in data) {
            if (first) {
                first = false;
            }
            else {
                result += '&';
            }
            result += (key + '=' + data[key]);
        }
        return result;
    };

    this.$get = [ '$http','$rootScope','routeService','$timeout',
    function($http, $rootScope,routeService,$timeout) {
      return {
        configRequest: angular.copy(_configRequest),
        setHeader: function(headerAtt, value) {
          this.configRequest.headers[headerAtt] = value;
          return this;
        },
        setWithCredential: function(withCrd){
          this.configRequest.withCredentials = withCrd;
          return this;
        },
        setData: function(data) {
          this.configRequest.data = data;
          return this;
        },
        setData4EncodedForm: function (data) {
            this.configRequest.data = encode4form(data);
            return this;
        },
        setData4Form: function (data) {
          this.configRequest.data = encode4DataForm(data);
          return this;
        },
        setTransformRequest: function(transformRequest) {
          this.configRequest.transformRequest = transformRequest;
          return this;
        },
        setService: function(url) {
          this.configRequest.url = _baseUrl+url;
          return this;
        },
        setErrorHandler: function(errorHandler) {
          this.configRequest.errorHandler = errorHandler;
          return this;
        },
        doDownload:function(success, error) {
          var configRequestAux =  angular.extend({},this.configRequest);
          this.configRequest = angular.copy(_configRequest);
          return $http(configRequestAux).then(function(response) {

            var fileName = response.headers('content-disposition');

            if (fileName) {
              var n = fileName.toLowerCase().lastIndexOf("=")+1;
              fileName = fileName.substring(n).replace(/["']+/g, '');
            }

            var hiddenElement = document.createElement('a');
            hiddenElement.href = 'data:'+response.headers('Content-Type')+',' + encodeURI(response.data);
            hiddenElement.target = '_blank';
            hiddenElement.download = fileName;
            hiddenElement.click();
            if (typeof success === 'function') {
              return success(response);
            } else {
              return response;
            }
          }, function(response) {
            if (configRequestAux.errorHandler !== undefined) {
              configRequestAux.errorHandler(response);
            } else {
              if (response.status === 401) {
                if (configRequestAux.url.indexOf('login') === -1 ) {
                  $timeout(function() {routeService.path('login');},200);
                }
              } else if (response.data !== null && !angular.isUndefined(response.data.message)) {
                $rootScope.alert.showErrorAlert(response.data.message);
              }
              response.code = response.status;
              if (typeof error === 'function') {
                return success(response);
              } else {
                return response;
              }
            }
          });
        },
        doPost:function(success, error) {
          var configRequestAux =  angular.extend({},this.configRequest);
          this.configRequest = angular.copy(_configRequest);
          return $http(configRequestAux).then(function(response){
            if (response.data !== undefined ) {
              response = response.data;
            }
            if (typeof success === 'function') {
              return success(response);
            } else {
              return response;
            }
          }, function(response) {
            if (configRequestAux.errorHandler !== undefined) {
              configRequestAux.errorHandler(response);
            } else {
              if (response.status === 401) {
                if (configRequestAux.url.indexOf('login') === -1 ) {
                  $timeout(function() {routeService.path('login');},200);
                }
              } else if (response.data !== null && !angular.isUndefined(response.data.message)) {
                $rootScope.alert.showErrorAlert(response.data.message);
              }
              response.code = response.status;
              if (typeof error === 'function') {
                return success(response);
              } else {
                return response;
              }
            }
          });
        },
        doGet:function(success, error) {
          this.configRequest.method = 'GET';
          return this.doPost(success, error);
        }
      };
    }];
  });
