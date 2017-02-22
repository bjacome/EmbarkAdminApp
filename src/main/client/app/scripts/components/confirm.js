/**
 * @ngdoc service
 * @name rbcembarkAdminApp.alert
 * @description
 * # alert
 * Provider of the rbcembarkAdminApp
 */
angular.module('rbcembarkAdminApp').component('confirm', {
  templateUrl: 'views/components/confirmForm.html',
  controllerAs:'comfirmCtrl',
  bindings: {
    onConfirm : '&',
    onCancel: '&'
  },
  transclude:true,
  controller:['$scope',function($scope) {
    var ctrl = this;
    $scope.confirm = function() {
      if (angular.isFunction(ctrl.onConfirm)) {
        $('#confirmModal').modal('hide');
        ctrl.onConfirm();
      }
    };
    $scope.cancel = function() {
      if (angular.isFunction(ctrl.onCancel)) {
        ctrl.onCancel();
      }
    };
  }]
});
