'use strict';

/**
 * @ngdoc service
 * @name rbcembarkAdminApp.alert
 * @description
 * # alert
 * Provider of the rbcembarkAdminApp
 */
angular.module('rbcembarkAdminApp').component('alert', {
  templateUrl: 'views/components/alert.html',
  controllerAs:'alertCtrl',
  bindings: {
    ngModel:'='
  },
  controller:function($scope,$rootScope, localize) {

    this.ngModel = this;

    $scope.showAlert = false;
    $scope.alertLevel = -1;
    $scope.alertText = '';
    $scope.alertLevelLabel = '';

    this.showSuccessAlert = function(message) {
      $scope.showAlert = true;
      $scope.alertLevel = 0;
      $scope.alertText = message;
      $scope.alertLevelLabel =localize.getLocalizedString('_Success_');
    };

    this.showWarningAlert = function(message) {
      $scope.showAlert = true;
      $scope.alertLevel = 1;
      $scope.alertText = message;
      $scope.alertLevelLabel =localize.getLocalizedString('_Warning_');
    };

    this.showErrorAlert = function(message) {
      $scope.showAlert = true;
      $scope.alertText = message;
      $scope.alertLevel = 2;
      $scope.alertLevelLabel =localize.getLocalizedString('_Error_');
    };

    this.hideAlert = function() {
      $scope.showAlert = false;
      $scope.alertLevel = -1;
      $scope.alertText = '';
      $scope.alertLevelLabel = '';
    };

  }
});
