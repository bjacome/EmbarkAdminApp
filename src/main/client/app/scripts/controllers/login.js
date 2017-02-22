'use strict';
/**
 * @ngdoc overview
 * @name rbcembarkAdminApp
 * @description
 * # LoginCtrl
 *
 * Login Controller of  the application.
 */
angular
  .module('rbcembarkAdminApp')
  .controller('LoginCtrl', function ($rootScope, $scope, $location, $cookies, user, routeService, localize, cfpLoadingBar) {
    console.log('LoginCtrl controller');
        // set default page mode
        if (typeof $rootScope.loginMode === 'undefined') {
            $rootScope.loginMode = 'login';
        }

        $scope.changePasswordConfirm = false;

        $scope.authenticatedUserName = '';

        // form model data
        $scope.loginFormData = {};
        $scope.changePasswordFormData = {};

        $cookies.remove('authenticatedUser');
        $rootScope.user = undefined;
        // login the user and retrieve their profile
        // if passwordStatus is Invalid, redirect them to Change Password, otherwise redirect to home
        function loginUser(loginFormData) {



            var inputValid = true;
            // validate username provided
            if (inputValid && (typeof loginFormData.loginId === 'undefined' || loginFormData.loginId.trim() === '')) {
                $scope.loginIdPlaceholder = '_BlankLoginIdError_';
                $scope.loginIdErrorClass = 'inputFieldError';
                inputValid = false;
            }

            // validate the loginId format - case insensitive and formatted like 'email@domain.com'
            var validEmailPattern = /^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$/i;
            if (inputValid && !validEmailPattern.test(loginFormData.loginId)) {
                $scope.loginIdErrorClass = 'inputFieldError';
                $scope.showErrors = true;
                $scope.loginErrorMessage = localize.getLocalizedString('_InvalidEmailFormatError_');
                inputValid = false;
            }

            // validate loginPassword provided
            if (inputValid && (typeof loginFormData.loginPassword === 'undefined' || loginFormData.loginPassword.trim() === '')) {
                $scope.loginPasswordPlaceholder = '_BlankPasswordError_';
                $scope.loginPasswordErrorClass = 'inputFieldError';
                inputValid = false;
            }

            if (inputValid) {

              // start progress bar
              cfpLoadingBar.start();
              user.loginUser(loginFormData)
              .then(function (response) {
                // stop progress bar
                cfpLoadingBar.complete();
                if (response.code === 200 ) {

                  if (response.data !== undefined && response.data.isSuperuser !== undefined) {
                    $scope.loggeduser = response.data;
                    // put user first name into scope
                    $scope.authenticatedUserName = response.data.name;
                    if (!response.data.isPasswordValid) {
                      $rootScope.loginMode = 'changePassword';
                    } else {
                      redirectToHome();
                    }
                  }

                } else {
                  $scope.showErrors = true;
                  $scope.loginErrorMessage = response.data.message;
                }
              });

            }
        }

        // execute a password change request
        function changePassword(changePasswordFormData) {

          var inputValid = true;
          // clear validation styles
            $scope.oldPasswordErrorClass = '';
            $scope.newPasswordErrorClass = '';
            $scope.confirmNewPasswordErrorClass = '';

            // validate old password provided
            if (inputValid && (typeof changePasswordFormData.oldPassword === 'undefined' || changePasswordFormData.oldPassword.trim() === '')) {
                $scope.oldPasswordPlaceholder = '_BlankOldPasswordError_';
                $scope.oldPasswordErrorClass = 'inputFieldError';
                inputValid = false;
            }

            // validate new password provided
            if (inputValid && (typeof changePasswordFormData.newPassword === 'undefined' || changePasswordFormData.newPassword.trim() === '')) {
                $scope.newPasswordPlaceholder = '_BlankNewPasswordError_';
                $scope.newPasswordErrorClass = 'inputFieldError';
                inputValid = false;
            }

            // validate new password confirmation provided
            if (inputValid && (typeof changePasswordFormData.confirmNewPassword === 'undefined' || changePasswordFormData.confirmNewPassword.trim() === '')) {
                $scope.confirmNewPasswordPlaceholder = '_BlankConfirmNewPasswordError_';
                $scope.confirmNewPasswordErrorClass = 'inputFieldError';
                inputValid = false;
            }

            // valid new password and confirmation password match
            if (inputValid && changePasswordFormData.newPassword !== changePasswordFormData.confirmNewPassword) {
                $scope.showErrors = true;
                $scope.changePasswordErrorMessage = '_PasswordMismatch_';
                $scope.newPasswordErrorClass = 'inputFieldError';
                $scope.confirmNewPasswordErrorClass = 'inputFieldError';
                inputValid = false;
            }

            // validate new password follows requirements

            // a) must not contain space
            if (inputValid && changePasswordFormData.newPassword.indexOf(' ') >= 0) {
                $scope.showErrors = true;
                $scope.changePasswordErrorMessage = '_PasswordRequirements_';
                $scope.newPasswordErrorClass = 'inputFieldError';
                $scope.confirmNewPasswordErrorClass = 'inputFieldError';
                inputValid = false;
            }

            // b) must be at least 7 characters in length
            if (inputValid && changePasswordFormData.newPassword.length <= 6) {
                $scope.showErrors = true;
                $scope.changePasswordErrorMessage = '_PasswordRequirements_';
                $scope.newPasswordErrorClass = 'inputFieldError';
                $scope.confirmNewPasswordErrorClass = 'inputFieldError';
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
                $scope.showErrors = true;
                $scope.changePasswordErrorMessage = '_PasswordRequirements_';
                $scope.newPasswordErrorClass = 'inputFieldError';
                $scope.confirmNewPasswordErrorClass = 'inputFieldError';
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
                          $scope.changePasswordConfirm = true;
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
        }

        function resetPassword(resetPasswordFormData) {
          resetPasswordFormData=null;
        }

        // clear login error message
        function clearLoginErrorMessage() {
            $scope.loginIdErrorClass = '';
            $scope.loginPasswordErrorClass = '';
            $scope.showErrors = false;
            $scope.loginErrorMessage = localize.getLocalizedString('_BlankMessage_');
        }

        // clear change password error message
        function clearChangePasswordErrorMessage() {}

        // clear reset password error message
        function clearResetPasswordErrorMessage() {}

        // redirect user to home page
        function redirectToHome() {

          $rootScope.loginMode = 'login';
          $rootScope.user = $scope.loggeduser;
          $rootScope.isSuperuser = $rootScope.user.isSuperuser;
          $cookies.putObject('authenticatedUser', $rootScope.user);
          routeService.addRouteFromArray($rootScope.user.routes);
          routeService.setOtherwise('/homepage');
          // clear the password field
          $scope.loginPassword = '';
          $location.path('/');
        }

        // redirect user to main login page
        function redirectToLogin() {}

        // redirect user to reset password page
        function redirectToResetPassword() {}

        // logout user and return home
        function logoutUser() {
          user.logoutUser().then(function() {
            $scope.authenticatedUserName = '';
            $scope.loginPassword = '';
            $rootScope.loginMode = 'login';
            routeService.removeRoutes();
            routeService.path('login');
          });
        }

        // field validation styles
        $scope.loginIdErrorClass = '';
        $scope.loginPasswordErrorClass = '';
        $scope.oldPasswordErrorClass = '';
        $scope.newPasswordErrorClass = '';
        $scope.confirmNewPasswordErrorClass = '';

        // default placeholder values
        $scope.loginIdPlaceholder = '_EmailInput_';
        $scope.loginPasswordPlaceholder = '_PasswordInput_';
        $scope.oldPasswordPlaceholder = '_OldPasswordInput_';
        $scope.newPasswordPlaceholder = '_NewPasswordInput_';
        $scope.confirmNewPasswordPlaceholder = '_NewPasswordConfirmInput_';

        // form error message
        $scope.changePasswordErrorMessage = '_BlankMessage_';
        $scope.loginErrorMessage = localize.getLocalizedString('_BlankMessage_');
        $scope.resetPasswordErrorMessage = '_BlankMessage_';
        $scope.showErrors = false;

        // form methods
        $scope.loginUser = loginUser;
        $scope.logoutUser = logoutUser;
        $scope.changePassword = changePassword;
        $scope.resetPassword = resetPassword;
        $scope.clearLoginErrorMessage = clearLoginErrorMessage;
        $scope.clearChangePasswordErrorMessage = clearChangePasswordErrorMessage;
        $scope.clearResetPasswordErrorMessage = clearResetPasswordErrorMessage;

        // user redirect methods
        $scope.redirectToHome = redirectToHome;
        $scope.redirectToLogin = redirectToLogin;
        $scope.redirectToResetPassword = redirectToResetPassword;

  });
