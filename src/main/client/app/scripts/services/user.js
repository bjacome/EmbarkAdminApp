'use strict';

/**
 * @ngdoc service
 * @name rbcembarkAdminApp.user
 * @description
 * # user
 * User service in the rbcembarkAdminApp.
 */


angular.module('rbcembarkAdminApp')
    .service('user', function (restService) {
      // log in user
      this.loginUser = function (loginData, errorHandler) {
        restService.setData4EncodedForm(loginData);
        restService.setHeader('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8');
        restService.setService('api/security/login');
        restService.setErrorHandler(errorHandler);
        restService.setWithCredential(false);
        return restService.doPost();
      };

      this.logoutUser = function() {
        restService.setService('api/security/logout');
        return restService.doPost();
      };

      this.changePassword = function(oldPassword, newPassword) {
        restService.setData({'data':{'oldPassword':oldPassword, 'newPassword': newPassword}});
        restService.setService('api/rest/session/updatePassword');
        return restService.doPost();
      };

    });
