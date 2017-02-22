'use strict';

/**
 * @ngdoc service
 * @name rbcembarkAdminApp.menu
 * @description
 * # menu
 * Provider of the rbcembarkAdminApp
 */
angular.module('rbcembarkAdminApp').component('menu', {
  templateUrl: 'views/components/menu.html',
  bindings: {
    loggedUser: '='
  },
  controller:function($scope,$rootScope,$timeout,routeService,cfpLoadingBar, user, localize) {

    $scope.logout = function() {
      user.logoutUser().then(function() {
        routeService.removeRoutes();
        routeService.path('login');
      });
    };

    $scope.passwordAlert = {};

    $scope.title = routeService.getCurrent().label;
    $scope.icon = routeService.getCurrent().icon;

    if ($scope.title === undefined) {
      $scope.title = $rootScope.user.routes[0].label;
      $scope.icon = $rootScope.user.routes[0].icon;
    }

    $scope.goToView = function(route) {
      routeService.path(route.controllerAs);
      $scope.title = route.label;
      $scope.icon = route.icon;
    };

    $('#passwordModal').on('show.bs.modal', function (e) {
      $timeout(function(){
        $scope.passwordForm.reset();
        $scope.passwordAlert.hideAlert();
      });
    });

    $scope.changePassword = function(changePasswordFormData) {
      //$("#passwordModal").modal("hide");
      var inputValid = true;


        // validate old password provided
        if (inputValid && (typeof changePasswordFormData.oldPassword === 'undefined' || changePasswordFormData.oldPassword.trim() === '')) {
            $scope.passwordAlert.showErrorAlert(localize.getLocalizedString('_BlankOldPasswordError_'));
            inputValid = false;
        }

        // validate new password provided
        if (inputValid && (typeof changePasswordFormData.newPassword === 'undefined' || changePasswordFormData.newPassword.trim() === '')) {
            $scope.passwordAlert.showErrorAlert(localize.getLocalizedString('_BlankNewPasswordError_'));
            inputValid = false;
        }

        // validate new password confirmation provided
        if (inputValid && (typeof changePasswordFormData.confirmNewPassword === 'undefined' || changePasswordFormData.confirmNewPassword.trim() === '')) {
            $scope.passwordAlert.showErrorAlert(localize.getLocalizedString('_BlankConfirmNewPasswordError_'));
            inputValid = false;
        }

        // valid new password and confirmation password match
        if (inputValid && changePasswordFormData.newPassword !== changePasswordFormData.confirmNewPassword) {
            $scope.passwordAlert.showErrorAlert(localize.getLocalizedString('_PasswordMismatch_'));
            inputValid = false;
        }

        // validate new password follows requirements

        // a) must not contain space
        if (inputValid && changePasswordFormData.newPassword.indexOf(' ') >= 0) {
            $scope.passwordAlert.showErrorAlert(localize.getLocalizedString('_PasswordRequirements_'));
            inputValid = false;
        }

        // b) must be at least 7 characters in length
        if (inputValid && changePasswordFormData.newPassword.length <= 6) {
            $scope.passwordAlert.showErrorAlert(localize.getLocalizedString('_PasswordRequirements_'));
            inputValid = false;
        }

        // c) must contain 3/4 required characters (uppercase letter, lower case letter, number, special character)
        var charCheckPass = 4;

        var upperCharPattern = /[A-Z]/;
        if (inputValid && !upperCharPattern.test(changePasswordFormData.newPassword)) {
            charCheckPass--;
        }

        var lowerCharPattern = /[a-z]/;
        if (inputValid && !lowerCharPattern.test(changePasswordFormData.newPassword)) {
            charCheckPass--;
        }

        var numberCharPattern = /[0-9]/;
        if (inputValid && !numberCharPattern.test(changePasswordFormData.newPassword)) {
            charCheckPass--;
        }

        var specialCharPattern = /[!@#$%^&*()+=\-[\]\\';,./{}|":<>?~_]/;
        if (inputValid && !specialCharPattern.test(changePasswordFormData.newPassword)) {
            charCheckPass--;
        }

        if (charCheckPass < 3) {
            $scope.passwordAlert.showErrorAlert(localize.getLocalizedString('_PasswordRequirements_'));
            inputValid = false;
        }

        if (inputValid) {

            // start progress bar
            cfpLoadingBar.start();

            user.changePassword(changePasswordFormData.oldPassword, changePasswordFormData.newPassword)
                .then(function (data) {
                  // stop progress bar
                  cfpLoadingBar.complete();

                  if (data.status === 'success') {
                      $scope.passwordAlert.showSuccessAlert(localize.getLocalizedString('_PasswordChangedHeader_'));
                  } else {
                      if (data.code === 1000702) {
                          $scope.showErrors = true;
                          $scope.changePasswordErrorMessage = '_InvalidCredentials_';
                      } else {
                          $scope.showErrors = true;
                          $scope.changePasswordErrorMessage = '_UnknownError_';
                      }
                  }
              });
          }
    };


  }
});
