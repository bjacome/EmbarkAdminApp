'use strict';

/**
 * @ngdoc function
 * @name rbcembarkAdminApp.controller:EmbarkUsersCtrl
 * @description
 * # EmbarkUsersCtrl
 * Controller of the rbcembarkAdminApp
 */
angular.module('rbcembarkAdminApp')
  .controller('EmbarkUsersCtrl', ['$scope', '$log', '$rootScope', 'restService', 'localize', 'cfpLoadingBar', '$timeout', '$window','$filter',
  function ($scope, $log, $rootScope, restService, localize, cfpLoadingBar, $timeout, $window, $filter) {
    $scope.formLabel = '';
    $scope.gridLabel = '_SelectForUserRows_';
    $scope.userGridloading = false;
    $scope.importFile = '';
    $scope.embarkUsersGridApi = {};
    $scope.roleTitlesGridApi = {};
    $scope.selectedCohortID = null;
    $scope.selectedRoleID = null;
    $scope.environments = [];
    $scope.roles = [];
    $scope.removeRow = false;
    $scope.userSelectedCohort = {};
    $scope.userSelectedRole = {};
    $scope.userSelectedCohort.name = '';
    $scope.userSelectedRole.name = '';
    $scope.newUserForm = {};
    $scope.newTaskGroupForm = {};
    $scope.newTaskForm = {};
    $scope.statuses = [];
    $scope.cohortGroups = [];
    $scope.modalFormLabel = '';
    $scope.cohortNotSelected = true;
    var istreeCollapsed = true;
    var isEtreeCollapsed = true;
    $scope.collapseOrExpandCButtonLabel = '_ExpandTree_';
    // Setting of the Embark Users grid
    $scope.embarkUsersGridOptions = {
      'enableColumnMenus': false,
      'enableRowSelection': true,
      'modifierKeysToMultiSelect': false,
      'multiSelect': false,
      'enableRowHeaderSelection': false,
      'columnDefs': [
         { 'name': 'lastName', 'field': 'lastName', 'cellEditableCondition': false, 'displayName': 'Last Name','width':'25%'},
         { 'name': 'firstName', 'field': 'firstName', 'cellEditableCondition': false, 'displayName': 'First Name','width':'25%'},
         { 'name': 'email', 'field': 'email', 'cellEditableCondition': false, 'displayName': 'Email','width':'40%'},
         { 'name': '', 'field': 'checkMark', 'cellEditableCondition': false, 'cellTemplate': '<button class="btn btn-primary btn-xs glyphicon glyphicon-remove removeButton" data-show="tip" data-placement="auto left" title="Remove User" data-toggle="modal" data-target=".bs-example-modal-dl" ng-click="grid.appScope.DeleteEmbarkUser(row)"></button>', 'enableHiding': false}
       ],
       'data': []
     };
     $scope.embarkUsersGridOptions.enableHorizontalScrollbar = 0;
    cfpLoadingBar.start();
    restService
    .setData({})
    .setService('api/rest/session/roles')
    .doPost()
    .then(function(result) {
       $scope.roles = result.data;
       $timeout(function () {
         $scope.$broadcast('angular-ui-tree:collapse-all');
         $scope.collapseOrExpandButtonLabel = '_ExpandTree_';
       });
    })
    .then(function(){

      restService.setService('api/rest/session/environments').doPost().then(function(result) {
        cfpLoadingBar.complete();
        $scope.environments = result.data;
        $scope.$broadcast('roles-loaded');
      });
    });
    //Watcher for New/Edit User form data change
    $scope.$watch('forms.newUserForm', function(form) {
      if(form) {
        $scope.newUserForm = form;
      }
    });
    //Watcher for taskGroupForm data change
    $scope.$watch('forms.taskGroupForm', function(form) {
      if(form) {
        $scope.newTaskGroupForm = form;
      }
    });
    //Watcher for taskForm data change
    $scope.$watch('forms.taskForm', function(form) {
      if(form) {
        $scope.newTaskForm = form;
      }
    });
    //Import Modal
    $scope.importModalCheck = function() {
      if (!$scope.isFormEdited()) {
        $scope.formLabel = '';
        if ($scope.userSelectedCohort._id === undefined) {
          $rootScope.alert.showWarningAlert(localize.getLocalizedString('_SelectCohortFirstImport_'));
          return;
        }else if ($scope.environmentName === null) {
          $rootScope.alert.showWarningAlert(localize.getLocalizedString('_SelectEnvironmentImport_'));
          return;
        } else if ($scope.userSelectedCohort._id !== undefined && $scope.environmentName !== null) {
          $('#importModal').modal('show');
        }
      }else {
        $rootScope.alert.showWarningAlert(localize.getLocalizedString('_UserFormEditWarning_'));
      }
    };

    //To show the Add New Embark Environments form
    $scope.showNewUserForm = function() {
      if (!$scope.isFormEdited()) {
        if ($scope.userSelectedCohort._id === undefined) {
          $rootScope.alert.showWarningAlert(localize.getLocalizedString('_SelectCohortFirstNewUser_'));
          return;
        }else if ($scope.environmentName === null) {
          $rootScope.alert.showWarningAlert(localize.getLocalizedString('_SelectEnvironmentNewUser_'));
          return;
        } else if ($scope.userSelectedCohort._id !== undefined && $scope.environmentName !== null) {
          $scope.email = '';
          $scope.firstName = '';
          $scope.lastName = '';
          $scope.startDate = ($scope.userSelectedCohort.startDate !== undefined) ? $scope.userSelectedCohort.startDate : null;
          $scope.disableDate = ($scope.userSelectedCohort.disableDate !== undefined) ? $scope.userSelectedCohort.disableDate : null;
          $scope.location = '';
          $scope.password = '';
          $scope.offerStatus = null;
          $scope.lastLogin = '';
          $scope.roleTitlesGridOptions.data = ($scope.userSelectedCohort.title !== undefined) ? angular.copy($scope.userSelectedCohort.title) : [];
          $scope.preNewTitlesGridData = angular.copy($scope.roleTitlesGridOptions.data);
          $scope.cohortGroups = ($scope.userSelectedCohort.checklist !== undefined) ? angular.copy($scope.userSelectedCohort.checklist) : [];
          $scope.preNewCohortGroups = angular.copy($scope.cohortGroups);
          $timeout(function () {
            $scope.collapseOrExpandCButtonLabel = '_ExpandTree_';
            angular.element(document.getElementById('ECohortsTree')).scope().$broadcast('angular-ui-tree:collapse-all');
          });
          $scope.formLabel = '_NewUserTitle_';
        }
      }else {
        $rootScope.alert.showWarningAlert(localize.getLocalizedString('_UserFormEditWarning_'));
      }
    };
    //Checking if form is edited
    $scope.isFormEdited = function() {
      if($scope.formLabel === '_NewUserTitle_') {
        //Form field dirtyCheck
        if ($rootScope.forms.newUserForm.modified) {
          return true;
        }
        //TitlesGrid dirtyCheck
        if (!angular.equals($scope.preNewTitlesGridData, $scope.roleTitlesGridOptions.data)) {
          return true;
        }

        //Tree dirtyCheck
        if (!angular.equals($scope.preNewCohortGroups, $scope.cohortGroups)) {
          return true;
        }

        return false;
      } else if ($scope.formLabel === '_EditUserTitle_') {
        //Form field dirtyCheck
        if ($rootScope.forms.newUserForm.$dirty) {
          return true;
        }
        //TitlesGrid dirtyCheck
        if (!angular.equals($scope.preEditTitlesGridData, $scope.roleTitlesGridOptions.data)) {
          return true;
        }

        //Tree dirtyCheck
        if (!angular.equals($scope.preEditCohortGroups, $scope.cohortGroups)) {
          return true;
        }
        return false;
      } else {
        //No Forms open
        return false;
      }
    };
    //Saving the New User form
    $scope.saveUser = function(uForm) {
      var validateForm = $scope.validate(uForm);
      if (validateForm) {
        cfpLoadingBar.start();
        var userData = {};
        var successAlert = null;
        if($scope.formLabel === '_NewUserTitle_') {
          userData.firstName = uForm.firstName.$modelValue;
          userData.lastName = uForm.lastName.$modelValue;
          userData.startDate = uForm.startDate.$modelValue;
          userData.email = uForm.email.$modelValue;
          userData.temporalPassword = uForm.userPassword.$modelValue;
          userData.offerStatus = uForm.offerStatus.$modelValue;
          userData.roleId = $scope.userSelectedCohort.roleName;
          userData.cohortId = $scope.userSelectedCohort.name;
          userData.cohort = $scope.roleTitlesGridOptions.data;
          userData.journeyStatusList = $scope.cohortGroups;
          if (uForm.userLocation.$viewValue.length > 0) {userData.location = uForm.userLocation.$modelValue;}
          if (uForm.disableDate.$viewValue !== 'Invalid date' && uForm.disableDate.$viewValue !== null) {userData.disabledDate = uForm.disableDate.$modelValue;}
          successAlert = localize.getLocalizedString('_UserAdded_');
        } else {
          userData._id = $scope._idUser;
          userData._rev = $scope._revUser;
          userData.firstName = uForm.firstName.$modelValue;
          userData.lastName = uForm.lastName.$modelValue;
          userData.startDate = uForm.startDate.$modelValue;
          userData.email = uForm.email.$modelValue;
          if (uForm.userPassword.$viewValue.length > 0) {
            userData.temporalPassword = uForm.userPassword.$modelValue;
          }
          userData.offerStatus = uForm.offerStatus.$modelValue;
          userData.roleId = $scope.userSelectedCohort.roleName;
          userData.cohortId = $scope.userSelectedCohort.name;
          userData.cohort = $scope.roleTitlesGridOptions.data;
          userData.journeyStatusList = $scope.cohortGroups;
          if (uForm.userLocation.$viewValue.length > 0) {userData.location = uForm.userLocation.$modelValue;}
          if (uForm.disableDate.$viewValue !== 'Invalid date' && uForm.disableDate.$viewValue !== null) {userData.disabledDate = uForm.disableDate.$modelValue;}
          else if (uForm.disableDate.$viewValue === 'Invalid date' || uForm.disableDate.$viewValue === null) {
            userData.disabledDate = null;
          }
          successAlert = localize.getLocalizedString('_UserUpdated_');
        }
        var sendUser = {
          "data": {
            "environmentId": $scope.environmentName._id,
            "user": userData
          }
        };
        restService
        .setData(angular.toJson(sendUser,true))
        .setService('api/rest/embark/save')
        .doPost()
        .then(function(response) {
          cfpLoadingBar.complete();
          if(response !== undefined && response.status === "success") {
            $scope.formLabel = '';
            loadUsers();
          }
        });
      }
    };

    $scope.validate = function(uForm) {
      var message = '';
      var invalidEmailMessage = '';
      var invalidPasswordMessage = '';
      var passwordInvalidMessages = ['','','',''];
      var passwordValid = true;
      var emailValid = true;
      var firstNameValid = true;
      var lastNameValid = true;
      var startDateValid = true;
      var offerStatusSelected = true;
      var titlesGridValid = true;
      var invalidTitlesGridMessage = '';
      var titlesGridInvalidMessages = ['',''];
      var emailCheck = function(email) {
        var validEmailPattern = /^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$/i;
        if (!validEmailPattern.test(email)) {
          return false;
        }
        return true;
      };
      var charCheck = function(password) {
        var charCheckPass = 4;
        var upperCharPattern = /[A-Z]/;
        var lowerCharPattern = /[a-z]/;
        var numberCharPattern = /[0-9]/;
        var specialCharPattern = /[!@#$%^&*()+=\-[\]\\';,./{}|":<>?~_]/;
        if (!upperCharPattern.test(password)) {
            charCheckPass--;
        }
        if (!lowerCharPattern.test(password)) {
            charCheckPass--;
        }
        if (!numberCharPattern.test(password)) {
            charCheckPass--;
        }
        if (!specialCharPattern.test(password)) {
            charCheckPass--;
        }
        if (charCheckPass < 3) {
          return false;
        }
        return true;
      };
      $rootScope.alert.hideAlert();
      if(uForm.email.$error.required){
        if(message.length>0){ message += localize.getLocalizedString('_CommaLabel_'); }
        message += localize.getLocalizedString('_EmailMissing_');
        emailValid = false;
      }else if (!emailCheck(uForm.email.$viewValue)) {
        invalidEmailMessage = localize.getLocalizedString('_InvalidEmail_');
        emailValid = false;
      }
      if(uForm.firstName.$error.required){
        if(message.length>0){ message += localize.getLocalizedString('_CommaLabel_'); }
        message += localize.getLocalizedString('_FirstNameMissing_');
        firstNameValid = false;
      }
      if(uForm.lastName.$error.required){
        if(message.length>0){ message += localize.getLocalizedString('_CommaLabel_'); }
        message += localize.getLocalizedString('_LastNameMissing_');
        lastNameValid = false;
      }
      if(uForm.startDate.$viewValue === 'Invalid date'){
        if(message.length>0){ message += localize.getLocalizedString('_CommaLabel_'); }
        message += localize.getLocalizedString('_StartDateMissing_');
        startDateValid = false;
      }
      if ($scope.formLabel === '_NewUserTitle_') {
        if(uForm.userPassword.$error.required){
          if(message.length>0){ message += localize.getLocalizedString('_CommaLabel_'); }
          message += localize.getLocalizedString('_PasswordRequired_');
          passwordInvalidMessages[0] = localize.getLocalizedString('_PasswordFullRequirements_');
          passwordValid = false;
        }else {
          if (uForm.userPassword.$viewValue.indexOf(' ')>=0 ) {
            passwordInvalidMessages[1] = localize.getLocalizedString('_PasswordSpaceAlert_');
            passwordValid = false;
          }
          if ((uForm.userPassword.$viewValue.length<=6)) {
            passwordInvalidMessages[2] = localize.getLocalizedString('_PasswordLengthAlert_');
            passwordValid = false;
          }
          if (!charCheck(uForm.userPassword.$viewValue)) {
            passwordInvalidMessages[3] = localize.getLocalizedString('_PasswordCharacterCheckAlert_');
            passwordValid = false;
          }
        }
      }else if ($scope.formLabel === '_EditUserTitle_') {
        if (uForm.userPassword.$viewValue.length > 0) {
          if (uForm.userPassword.$viewValue.indexOf(' ')>=0 ) {
            passwordInvalidMessages[1] = localize.getLocalizedString('_PasswordSpaceAlert_');
            passwordValid = false;
          }
          if ((uForm.userPassword.$viewValue.length<=6)) {
            passwordInvalidMessages[2] = localize.getLocalizedString('_PasswordLengthAlert_');
            passwordValid = false;
          }
          if (!charCheck(uForm.userPassword.$viewValue)) {
            passwordInvalidMessages[3] = localize.getLocalizedString('_PasswordCharacterCheckAlert_');
            passwordValid = false;
          }
        }
      }
      if (!passwordValid) {
        for (var i = 0; i < passwordInvalidMessages.length; i++) {
          if (passwordInvalidMessages[i] !== '') {
            invalidPasswordMessage += passwordInvalidMessages[i];
          }
        }
      }
      if(uForm.offerStatus.$viewValue === null){
        if(message.length>0){ message += localize.getLocalizedString('_CommaLabel_'); }
        message += localize.getLocalizedString('_OfferStatusMissing_');
        offerStatusSelected = false;
      }
      for (var i = 0; i < $scope.roleTitlesGridOptions.data.length; i++) {
        if ($scope.roleTitlesGridOptions.data[i].text.trim().length <= 0 || $scope.roleTitlesGridOptions.data[i].lang.trim().length <= 0) {
          titlesGridInvalidMessages[0] = localize.getLocalizedString('_TitlesGridBlankFieldAlert_');
          titlesGridValid = false;
          break;
        }
      }
      if ($scope.roleTitlesGridOptions.data.length%2 !== 0) {
        titlesGridInvalidMessages[1] = localize.getLocalizedString('_TitlesGridEN_FRAlert_');
        titlesGridValid = false;
      } else {
        var en = 0;
        for (var i = 0; i < $scope.roleTitlesGridOptions.data.length; i++) {
          if ($scope.roleTitlesGridOptions.data[i].lang === 'EN') {
            en++;
          }
        }
        if (en !== $scope.roleTitlesGridOptions.data.length/2) {
          titlesGridInvalidMessages[1] = localize.getLocalizedString('_TitlesGridEN_FRAlert_');
          titlesGridValid = false;
        }
      }
      if (!titlesGridValid) {
        for (var i = 0; i < titlesGridInvalidMessages.length; i++) {
          if (titlesGridInvalidMessages[i] !== '') {
            invalidTitlesGridMessage += titlesGridInvalidMessages[i];
          }
        }
      }
      if(message.length>0){
        message += localize.getLocalizedString('_Required_');
      }
      if(message.length>0 || !emailValid || !passwordValid || !titlesGridValid){
       $rootScope.alert.showErrorAlert(message+invalidEmailMessage+invalidPasswordMessage+invalidTitlesGridMessage);
       return false;
      }
      return true;
    };
    //Cancel the New User form
    $scope.cancelUser = function() {
      $rootScope.alert.hideAlert();
      $scope.formLabel = '';
    };
    //Export User
    $scope.exportUser = function() {
      if (!$scope.isFormEdited()) {
        $scope.formLabel = '';
        cfpLoadingBar.start();
        restService
        .setService('api/rest/embark/export')
        .setData({"data": {"environmentId": $scope.environmentName._id, "roleId": $scope.userSelectedRole._id, "cohortId": $scope.userSelectedCohort._id}})
        .doDownload().then(function(){
          cfpLoadingBar.complete();
        });
      }else {
        $rootScope.alert.showWarningAlert(localize.getLocalizedString('_UserFormEditWarning_'));
      }
    };
    //Import User
    $scope.importUser = function(form) {
      var file = $scope.importFile;
      cfpLoadingBar.start();
      restService.setData4Form(
        {
          "environmentId": $scope.environmentName._id,
          "roleId": $scope.userSelectedRole._id,
          "cohortId": $scope.userSelectedCohort._id,
          "file":$scope.importFile});
        restService.setHeader('Content-Type', undefined);
        restService.setTransformRequest(angular.identity);
        restService.setService('api/rest/embark/import');
        restService.doPost().then(function(response) {
          if(response !== undefined && response.status === "success") {
            $scope.userGridloading = true;
            var envCohortData = {
              "roleId": $scope.userSelectedRole._id,
              "cohortId": $scope.userSelectedCohort._id,
              "environmentId": $scope.environmentName._id,
              "page": 0,
              "pageSize": 200
            };
            var formattedEnvCohortData = {
              "data": envCohortData
            };
            restService.setData(angular.toJson(formattedEnvCohortData,true));
            restService.setHeader('Content-Type', 'application/json;charset=utf-8');
            restService.setService('api/rest/embark/users');
            restService.doPost().then(function(response) {
              if(response !== undefined && response.status === "success") {
                cfpLoadingBar.complete();
                $scope.gridLabel = '_NoUserRows_';
                if(response.data === undefined){
                  $scope.embarkUsersGridOptions.data = [];
                  $scope.userGridloading = false;
                }else {
                  $scope.embarkUsersGridOptions.data = response.data;
                  $scope.userGridloading = false;
                }
                $scope.formLabel = '';
                $rootScope.alert.hideAlert();
                $rootScope.alert.showSuccessAlert(localize.getLocalizedString('_UsersImported_'));
                $timeout(function(){
                  $rootScope.alert.hideAlert();
                },1500);
              } else {
                cfpLoadingBar.complete();
                $rootScope.alert.showErrorAlert(localize.getLocalizedString('_UsersImportError_'));
              }
            });
          } else {
            var user = '',
            msg = localize.getLocalizedString('_EmbarkUserNotImported_'),
            iuser = '',
            imsg = '';
            if(angular.isDefined(response.data) && angular.isDefined(response.data.results)) {
              for(var usr in response.data.results) {
                if(user.length>0) user += ' , ';
                if(response.data.results[usr].status.indexOf('exists')>0) {
                  msg = localize.getLocalizedString('_EmbarkUserAlreadyExists_');
                  user += response.data.results[usr].email;
                } else if(response.data.results[usr].status.indexOf('Valid')>0) {
                  imsg = localize.getLocalizedString('_UserImported_');
                  iuser += response.data.results[usr].email;
                }
              }
            }
            var ErrorImportMsg = iuser + imsg + user + msg;
            cfpLoadingBar.complete();
            $rootScope.alert.showErrorAlert(ErrorImportMsg);
          }
        });
    };
    //Select Role Logic
    $scope.setSelectedRole = function(role) {
      if (!$scope.isFormEdited()) {
        $scope.cohortNotSelected = true;
        $scope.formLabel = '';
        $scope.selectedRoleID = role.$$hashKey.slice(role.$$hashKey.indexOf(':')+1);
        $scope.selectedCohortID = null;
        $scope.embarkUsersGridOptions.data = [];
        $scope.environmentName = null;
        $scope.userSelectedRole = role;
        $scope.userSelectedCohort = {};
        $scope.userSelectedCohort.name = '';
      }else {
        $rootScope.alert.showWarningAlert(localize.getLocalizedString('_UserFormEditWarning_'));
      }
    };
    //Select Cohort Logic
    $scope.setSelectedCohort = function(role, cohort) {
      if (!$scope.isFormEdited()) {
        $scope.cohortNotSelected = false;
        $scope.formLabel = '';
        $scope.selectedRoleID = role.$$hashKey.slice(role.$$hashKey.indexOf(':')+1);
        $scope.selectedCohortID = cohort.$$hashKey.slice(cohort.$$hashKey.indexOf(':')+1);
        $scope.embarkUsersGridOptions.data = [];
        $scope.environmentName = null;
        $scope.userSelectedCohort = cohort;
        $scope.userSelectedRole = role;
        if ($scope.userSelectedRole.statuses === undefined) {
          $scope.statuses = [];
        } else {
          $scope.statuses = $scope.userSelectedRole.statuses;
        }
      } else {
        $rootScope.alert.showWarningAlert(localize.getLocalizedString('_UserFormEditWarning_'));
      }
    };

    var loadUsers = function() {
      cfpLoadingBar.start();
      $scope.userGridloading = true;
      var envCohortData = {
        'roleId': $scope.userSelectedRole._id,
        'cohortId': $scope.userSelectedCohort._id,
        'environmentId': $scope.environmentName._id,
        'page': 0,
        'pageSize': 200
      };
      var formattedEnvCohortData = {
        'data': envCohortData
      };
      restService
      .setData(angular.toJson(formattedEnvCohortData,true))
      .setService('api/rest/embark/users')
      .doPost()
      .then(function(response) {
        cfpLoadingBar.complete();
        if(response !== undefined && response.status === "success") {
          $rootScope.alert.hideAlert();
          $scope.removeRow = false;
          $scope.gridLabel = '_NoUserRows_';
          if(response.data === undefined){
            $scope.embarkUsersGridOptions.data = [];
            $scope.userGridloading = false;
          }else {
            $scope.embarkUsersGridOptions.data = response.data;
            $scope.userGridloading = false;
          }
        }
      });
    };
    //Environment list change function
    $scope.environmentChanged = function() {
      if (!$scope.isFormEdited()) {
        $scope.formLabel = '';
        if($scope.userSelectedCohort._id === undefined){
          $rootScope.alert.showWarningAlert(localize.getLocalizedString('_SelectCohortFirst_'));
          return;
        } else {
          loadUsers();
        }
      }else {
        $rootScope.alert.showWarningAlert(localize.getLocalizedString('_UserFormEditWarning_'));
      }
    };
    $scope.collapseOrExpandButtonLabel = '_CollapseTree_';


    // To collapse or expand the whole tree
    $scope.collapseOrExpandAll  = function(target, flag) {
      var scope = angular.element(document.getElementById(target)).scope();
      switch(flag) {
        case 'Main':
          if (istreeCollapsed) {
            $scope.collapseOrExpandButtonLabel = '_CollapseTree_';
            scope.$broadcast('angular-ui-tree:expand-all');
          } else {
            $scope.collapseOrExpandButtonLabel = '_ExpandTree_';
            scope.$broadcast('angular-ui-tree:collapse-all');
          }
          istreeCollapsed = !istreeCollapsed;
        break;

        case 'Cohort':
          if (isEtreeCollapsed) {
            $scope.collapseOrExpandCButtonLabel = '_CollapseTree_';
            scope.$broadcast('angular-ui-tree:expand-all');
          } else {
            $scope.collapseOrExpandCButtonLabel = '_ExpandTree_';
            scope.$broadcast('angular-ui-tree:collapse-all');
          }
          isEtreeCollapsed = !isEtreeCollapsed;
        break;
      }

    };

    //Check if Task Groups have child tasks so we can display Expand/Collapse button
    //Check if Task Groups have child tasks so we can display Expand/Collapse button
    $scope.haveChilds = function() {

      var cr = $scope.cohortGroups;
        for(var tk in cr) {
          if(angular.isDefined(cr[tk].taskList) && cr[tk].taskList.length > 0) {
              return true;
          }
        }
      return false;
    };

    //To add a new node to a ui-tree
    $scope.newTask = function(taskGroup) {
      $scope.selectedTaskParent = taskGroup.$$hashKey.slice(taskGroup.$$hashKey.indexOf(':')+1);
      var task = {
        "key":'',
        "label": [],
        "dueDate": null
      };
      $scope.taskName = angular.copy(task.key);
      $scope.taskGroupLabelsGridOptions.data = angular.copy(task.label);
      $scope.dueDate = angular.copy(task.dueDate);
      $scope.modalFormLabel = localize.getLocalizedString('_NewTaskModalLabel_');
    };

    //Delete grid rows
    $scope.DeleteEmbarkUser = function(row) {
      $scope.removeRow = true;
      $scope.deleteGrid = 'embarkUsersGridOptions';
      $scope.deleteRow = row;
    };

    $scope.cancelDelete = function() {
      $scope.removeRow = false;
    };

    //Delete grid rows
    $scope.Delete = function(grid, row) {
      grid = grid || $scope.deleteGrid;
      switch (grid) {
        case 'embarkUsersGridOptions':
          var row = $scope.deleteRow;
          $scope.formLabel = '';
           cfpLoadingBar.start();
           var deleteEnvRow = {
             "data": {
               "environmentId": $scope.environmentName._id,
               "_id": row.entity._id,
               "_rev": row.entity._rev
             }
           };
           restService
           .setData(angular.toJson(deleteEnvRow,true))
           .setService('api/rest/embark/delete')
           .doPost()
           .then(function(response) {
             cfpLoadingBar.complete();
             if(response !== undefined && response.status === "success") {
               var index = $filter('getIndexById')($scope.embarkUsersGridOptions.data, row.entity._id);
               $scope.embarkUsersGridOptions.data.splice(index,1);
               $scope.removeRow = false;
               $scope.deleteGrid = undefined;
               $scope.deleteRow = undefined;
             }
           });
          break;
        case 'roleTitlesGridOptions':
          var index = $scope[grid].data.indexOf(row.entity);
          $scope[grid].data.splice(index, 1);
          break;
        case 'taskGroupGroupLabelsGridOptions':
          var index = $scope[grid].data.indexOf(row.entity);
          $scope[grid].data.splice(index, 1);
          break;
        case 'taskGroupLabelsGridOptions':
          var index = $scope[grid].data.indexOf(row.entity);
          $scope[grid].data.splice(index, 1);
          break;
      }
    };
    //Loads a new Task Group object into the appropriate Task Group Tree
    $scope.newTaskGroup = function() {
      var tree = {
        "key":'',
        "label": [],
        "taskList": []
      };
      var taskGroup = tree;
      $scope.taskGroupName = angular.copy(taskGroup.key);
      $scope.taskGroupGroupLabelsGridOptions.data = angular.copy(taskGroup.label);
      $scope.modalFormLabel = localize.getLocalizedString('_NewTaskGroupModalLabel_');
    };
    //Embark Users Grid Api
     $scope.embarkUsersGridOptions.onRegisterApi = function(gridApi) {
       //set gridApi on scope of Embark Users Grid
       $scope.embarkUsersGridApi = gridApi;
       //Click event on one of the rows of the embark users grid
       $scope.embarkUsersGridApi.selection.on.rowSelectionChanged($scope,function(row){
         if (!$scope.isFormEdited()) {
           if (!$scope.removeRow) {
             cfpLoadingBar.start();
             var reqUserData = {
               "data": {
                 "_id": row.entity._id,
                 "environmentId": $scope.environmentName._id
               }
             };
             restService
             .setData(angular.toJson(reqUserData,true))
             .setService('api/rest/embark/user')
             .doPost()
             .then(function(response) {
               cfpLoadingBar.complete();
               if(response !== undefined && response.status === "success") {

                 $scope._idUser = response.data._id;
                 $scope._revUser = response.data._rev;
                 $scope.email = response.data.email;
                 $scope.firstName = response.data.firstName;
                 $scope.lastName = response.data.lastName;
                 $scope.startDate = response.data.startDate;
                 $scope.disableDate = (response.data.disabledDate !== undefined) ? response.data.disabledDate : null;
                 $scope.location = (response.data.location !== undefined) ? response.data.location : '';
                 $scope.password = '';
                 if (response.data.offerStatus !== undefined) {
                   for (var i = 0; i < $scope.statuses.length; i++) {
                     if ($scope.statuses[i] === response.data.offerStatus) {
                       $scope.statusIndex = i;
                       break;
                     }
                   }
                   $scope.offerStatus = $scope.statuses[$scope.statusIndex];
                 }
                 $scope.lastLogin = (response.data.lastLogin !== undefined) ? response.data.lastLogin : '';
                 $scope.roleTitlesGridOptions.data = (response.data.cohort !== undefined) ? angular.copy(response.data.cohort) : [];
                 $scope.preEditTitlesGridData = angular.copy($scope.roleTitlesGridOptions.data);
                 $scope.cohortGroups = (response.data.journeyStatusList !== undefined) ? angular.copy(response.data.journeyStatusList) : [];
                 $scope.preEditCohortGroups = angular.copy($scope.cohortGroups);

                 $scope.formLabel = '_EditUserTitle_';
                 $timeout(function () {
                   $scope.collapseOrExpandCButtonLabel = '_ExpandTree_';
                   angular.element(document.getElementById('ECohortsTree')).scope().$broadcast('angular-ui-tree:collapse-all');
                 });
               }
             });
           }
         }else {
           $rootScope.alert.showWarningAlert(localize.getLocalizedString('_UserFormEditWarning_'));
         }
       });
     };
     //Grid Adjustment functions
     $scope.GridAdjust = function(gridOps, id) {
       if ($scope[gridOps].data.length > 16){
         angular.element(document.getElementById(id)).css('height', (((16 + 1)*30)+5) + 'px');
       }else {
         angular.element(document.getElementById(id)).css('height', ((($scope[gridOps].data.length + 1)*30)+5) + 'px');
       }
     };
     // Setting of the title grid
     $scope.roleTitlesGridOptions = {
       'enableColumnMenus': false,
       'allowCellFocus':true,
       'enableCellEditOnFocus': true,
       'gridMenuShowHideColumns':false,
       'columnDefs': [
          { 'name': 'text', 'field': 'text', 'cellEditableCondition': true, 'enableCellEditOnFocus': true, 'enableCellEdit': true,'displayName': 'Title', 'width':'70%' },
          { 'name': 'lang', 'field': 'lang', 'enableCellEdit': true, 'displayName': 'Language', editDropdownValueLabel: 'lang', editDropdownOptionsArray: [{id: 'EN',lang: 'EN'}, {id: 'FR',lang: 'FR'}], cellFilter: 'mapLangLabel', editableCellTemplate: 'ui-grid/dropdownEditor', 'width':'20%'},
          { 'name': '', 'field': 'pseudoField', 'cellEditableCondition': false, 'cellTemplate': '<button class="btn btn-primary btn-xs glyphicon glyphicon-remove removeButton" data-show="tip" data-placement="auto left" title="Remove Title" ng-click="grid.appScope.Delete(\'roleTitlesGridOptions\',row)"></button>', 'enableHiding': false, 'enableCellEdit':false}
        ]
     };
     $scope.roleTitlesGridOptions.enableHorizontalScrollbar = 0;

      // Setting of the taskGroupGroupLabels grid
      $scope.taskGroupGroupLabelsGridOptions = {
        'enableColumnMenus': false,
        'allowCellFocus':true,
        'enableCellEditOnFocus': true,
        'gridMenuShowHideColumns':false,
        'columnDefs': [
          {'name': 'text', 'field': 'text', 'cellEditableCondition': true, 'enableCellEditOnFocus': true, 'enableCellEdit': true, 'displayName': 'Label', 'width':'70%' },
          {'name': 'lang', 'field': 'lang', 'enableCellEdit': true, 'displayName': 'Language', editDropdownValueLabel: 'lang', editDropdownOptionsArray: [{id: 'EN',lang: 'EN'}, {id: 'FR',lang: 'FR'}], cellFilter: 'mapLangLabel', editableCellTemplate: 'ui-grid/dropdownEditor', 'width':'20%'},
          {'name': '', 'field': 'pseudoField', 'cellEditableCondition': false, 'cellTemplate': '<button class="btn btn-primary btn-xs glyphicon glyphicon-remove removeButton" data-show="tip" data-placement="auto left" title="Remove Label" ng-click="grid.appScope.Delete(\'taskGroupGroupLabelsGridOptions\',row)"></button>', 'enableHiding': false, 'enableCellEdit':false}
        ]
      };
      $scope.taskGroupGroupLabelsGridOptions.enableHorizontalScrollbar = 0;

      // Setting of the taskGroupLabels grid
      $scope.taskGroupLabelsGridOptions = {
        'enableColumnMenus': false,
        'allowCellFocus':true,
        'enableCellEditOnFocus': true,
        'gridMenuShowHideColumns':false,
        'columnDefs': [
          {'name': 'text', 'field': 'text', 'cellEditableCondition': true, 'enableCellEditOnFocus': true, 'enableCellEdit': true, 'displayName': 'Label', 'width':'70%' },
          {'name': 'lang', 'field': 'lang', 'enableCellEdit': true, 'displayName': 'Language', editDropdownValueLabel: 'lang', editDropdownOptionsArray: [{id: 'EN',lang: 'EN'}, {id: 'FR',lang: 'FR'}], cellFilter: 'mapLangLabel', editableCellTemplate: 'ui-grid/dropdownEditor', 'width':'20%'},
          {'name': '', 'field': 'pseudoField', 'cellEditableCondition': false, 'cellTemplate': '<button class="btn btn-primary btn-xs glyphicon glyphicon-remove removeButton" data-show="tip" data-placement="auto left" title="Remove Label" ng-click="grid.appScope.Delete(\'taskGroupLabelsGridOptions\',row)"></button>', 'enableHiding': false, 'enableCellEdit':false}
        ]
      };
      $scope.taskGroupLabelsGridOptions.enableHorizontalScrollbar = 0;

      //To add a new row to Roles Titles grid
      $scope.addTitleRow = function() {
        if ($scope.roleTitlesGridOptions.data.length === 0) {
          $scope.roleTitlesGridOptions.data = [{
            'lang': '',
            'text': ''
          }];
          var temp = $scope.roleTitlesGridOptions.data.length-1;
          $timeout(function(){
            $scope.roleTitlesGridApi.cellNav.scrollToFocus($scope.roleTitlesGridOptions.data[temp], $scope.roleTitlesGridOptions.columnDefs[0]);
          },200);
        }else {
          var gridLength = $scope.roleTitlesGridOptions.data.length;
          if($scope.roleTitlesGridOptions.data[gridLength-1].text.length > 0 && $scope.roleTitlesGridOptions.data[gridLength-1].lang.length > 0) {
            $scope.roleTitlesGridOptions.data.push({
              'lang': '',
              'text': ''
            });
            var temp = $scope.roleTitlesGridOptions.data.length-1;
            $timeout(function(){
              $scope.roleTitlesGridApi.cellNav.scrollToFocus($scope.roleTitlesGridOptions.data[temp], $scope.roleTitlesGridOptions.columnDefs[0]);
            },200);
          } else {
            $rootScope.alert.showWarningAlert(localize.getLocalizedString('_FinishFinalGridEntry_'));
          }
        }
      };

      $scope.roleTitlesGridOptions.onRegisterApi = function(gridApi){
        //set gridApi on scope of Role Titles Grid
        $scope.roleTitlesGridApi = gridApi;
      };

      angular.element(document).ready(function () {
        $rootScope.alert.hideAlert();
        $("body").tooltip({ selector: '[data-show=tip]' });
      });
        //To load an existing task group to the form
        $scope.loadTaskGroup = function(taskGroup) {
          $scope.selectedTaskGroup = taskGroup.$$hashKey.slice(taskGroup.$$hashKey.indexOf(':')+1);
          $scope.taskGroupName = angular.copy(taskGroup.key);
          $scope.taskGroupGroupLabelsGridOptions.data = angular.copy(taskGroup.label);
          $scope.modalFormLabel = localize.getLocalizedString('_EditTaskGroupModalLabel_');
        };
        //To load an existing task to the form
        $scope.loadTask = function(task, group) {
          $scope.selectedTask = task.$$hashKey.slice(task.$$hashKey.indexOf(':')+1);
          $scope.selectedTaskParent = group.$$hashKey.slice(group.$$hashKey.indexOf(':')+1);
          $scope.taskName = angular.copy(task.key);
          $scope.taskGroupLabelsGridOptions.data = angular.copy(task.label);
          $scope.dueDate = angular.copy(task.dueDate);
          $scope.modalFormLabel = localize.getLocalizedString('_EditTaskModalLabel_');
        };
      //Validate Task Entry
      $scope.validateTask = function(form) {
        var message = '';
        var dueDateValid = true;
        var taskGridValid = true;
        var taskGridBlankFieldMessage = '';
        $rootScope.alert.hideAlert();
        if(form.taskName.$error.required){
          if(message.length>0){ message += localize.getLocalizedString('_CommaLabel_'); }
          message += localize.getLocalizedString('_TaskKeyMissing_');
        }
        if(form.dueDate.$viewValue === 'Invalid date' || form.dueDate.$viewValue === null){
          if(message.length>0){ message += localize.getLocalizedString('_CommaLabel_'); }
          message += localize.getLocalizedString('_DueDateMissing_');
          dueDateValid = false;
        }
        if ($scope.taskGroupLabelsGridOptions.data.length % 2 === 0) {
          if ($scope.taskGroupLabelsGridOptions.data.length < 2) {
            taskGridBlankFieldMessage = localize.getLocalizedString('_TaskGroupLabelsGridNotEnoughLabels_');
            taskGridValid = false;
          }else if ($scope.taskGroupLabelsGridOptions.data.length === 2) {
            for (var i = 0; i < $scope.taskGroupLabelsGridOptions.data.length; i++) {
              if ($scope.taskGroupLabelsGridOptions.data[i].text.trim().length <= 0 || $scope.taskGroupLabelsGridOptions.data[i].lang.trim().length <= 0) {
                taskGridBlankFieldMessage = localize.getLocalizedString('_TaskGroupLabelsGridBlankFieldAlert_');
                taskGridValid = false;
                break;
              }
            }
            var en = 0;
            for (var i = 0; i < $scope.taskGroupLabelsGridOptions.data.length; i++) {
              if ($scope.taskGroupLabelsGridOptions.data[i].lang === 'EN') {
                en++;
              }
            }
            if (en === 0 || en === 2) {
              taskGridBlankFieldMessage = localize.getLocalizedString('_TaskGroupLabelsGridNotEnoughLabels_');
              taskGridValid = false;
            }
          }
        } else {
          taskGridBlankFieldMessage = localize.getLocalizedString('_TaskGroupLabelsGridEN_FRAlert_');
          taskGridValid = false;
        }
        if(message.length>0){
          message += localize.getLocalizedString('_Required_');
        }
        if(message.length>0 || !taskGridValid){
         $window.alert(message+taskGridBlankFieldMessage);
         return false;
        }
        return true;
      };
      //Saves the values on the form to the current task displayed
      $scope.saveTask = function(form) {
        var validTask = $scope.validateTask(form);
        if (validTask) {
          if ($scope.modalFormLabel === localize.getLocalizedString('_NewTaskModalLabel_')) {
            var index = null;
            for (var i = 0; i < $scope.cohortGroups.length; i++) {
              if ($scope.cohortGroups[i].$$hashKey.slice($scope.cohortGroups[i].$$hashKey.indexOf(':')+1) === $scope.selectedTaskParent) {
                index = i;
                break;
              }
            }
            $scope.cohortGroups[index].taskList = $scope.cohortGroups[index].taskList || [];
            $scope.cohortGroups[index].taskList.push({
              "key": $scope.taskName,
              "label": $scope.taskGroupLabelsGridOptions.data,
              "dueDate": $scope.dueDate
            });
          } else if ($scope.modalFormLabel === localize.getLocalizedString('_EditTaskModalLabel_')) {
            var indexTask = null;
            var indexParent = null;
            for (var i = 0; i < $scope.cohortGroups.length; i++) {
              if ($scope.cohortGroups[i].$$hashKey.slice($scope.cohortGroups[i].$$hashKey.indexOf(':')+1) === $scope.selectedTaskParent) {
                indexParent = i;
                break;
              }
            }
            for (var i = 0; i < $scope.cohortGroups[indexParent].taskList.length; i++) {
              if ($scope.cohortGroups[indexParent].taskList[i].$$hashKey.slice($scope.cohortGroups[indexParent].taskList[i].$$hashKey.indexOf(':')+1) === $scope.selectedTask) {
                indexTask = i;
                break;
              }
            }
            $scope.cohortGroups[indexParent].taskList[indexTask].key = $scope.taskName;
            $scope.cohortGroups[indexParent].taskList[indexTask].label = $scope.taskGroupLabelsGridOptions.data;
            $scope.cohortGroups[indexParent].taskList[indexTask].dueDate = $scope.dueDate;
          }
          $scope.haveChilds();
          $('#taskModal').modal('hide');
        }
      };
      //To add a new row to a Task Labels grid
      $scope.addTaskRow = function(){
        if ($scope.taskGroupLabelsGridOptions.data.length === 0) {
          $scope.taskGroupLabelsGridOptions.data = [{
            "text": '',
            "lang": ''
          }];
          var temp = $scope.taskGroupLabelsGridOptions.data.length-1;
          $timeout(function(){
            $scope.taskGroupLabelsGridApi.cellNav.scrollToFocus($scope.taskGroupLabelsGridOptions.data[temp], $scope.taskGroupLabelsGridOptions.columnDefs[0]);
          },200);
        }else if ($scope.taskGroupLabelsGridOptions.data.length < 2) {
          var gridLength = $scope.taskGroupLabelsGridOptions.data.length;
          if($scope.taskGroupLabelsGridOptions.data[gridLength-1].text.length > 0 && $scope.taskGroupLabelsGridOptions.data[gridLength-1].lang.length > 0) {
            $scope.taskGroupLabelsGridOptions.data.push({
              'lang': '',
              'text': ''
            });
            var temp = $scope.taskGroupLabelsGridOptions.data.length-1;
            $timeout(function(){
              $scope.taskGroupLabelsGridApi.cellNav.scrollToFocus($scope.taskGroupLabelsGridOptions.data[temp], $scope.taskGroupLabelsGridOptions.columnDefs[0]);
            },200);
          }else {
            $window.alert(localize.getLocalizedString('_FinishFinalGridEntry_'));
          }
        }else if ($scope.taskGroupLabelsGridOptions.data.length === 2) {
          $window.alert(localize.getLocalizedString('_GridEntryLimitReachedAlert_'));
        }
      };
      //To add a new row to a Task Group Labels grid
      $scope.addTaskGroupRow = function(){
        if ($scope.taskGroupGroupLabelsGridOptions.data.length === 0) {
          $scope.taskGroupGroupLabelsGridOptions.data = [{
            "text": '',
            "lang": ''
          }];
          var temp = $scope.taskGroupGroupLabelsGridOptions.data.length-1;
          $timeout(function(){
            $scope.taskGroupGroupLabelsGridApi.cellNav.scrollToFocus($scope.taskGroupGroupLabelsGridOptions.data[temp], $scope.taskGroupGroupLabelsGridOptions.columnDefs[0]);
          },200);
        }else if ($scope.taskGroupGroupLabelsGridOptions.data.length < 2) {
          var gridLength = $scope.taskGroupGroupLabelsGridOptions.data.length;
          if($scope.taskGroupGroupLabelsGridOptions.data[gridLength-1].text.length > 0 && $scope.taskGroupGroupLabelsGridOptions.data[gridLength-1].lang.length > 0) {
            $scope.taskGroupGroupLabelsGridOptions.data.push({
              'lang': '',
              'text': ''
            });
            var temp = $scope.taskGroupGroupLabelsGridOptions.data.length-1;
            $timeout(function(){
              $scope.taskGroupGroupLabelsGridApi.cellNav.scrollToFocus($scope.taskGroupGroupLabelsGridOptions.data[temp], $scope.taskGroupGroupLabelsGridOptions.columnDefs[0]);
            },200);
          }else {
            $window.alert(localize.getLocalizedString('_FinishFinalGridEntry_'));
          }
        }else if ($scope.taskGroupGroupLabelsGridOptions.data.length === 2) {
          $window.alert(localize.getLocalizedString('_GridEntryLimitReachedAlert_'));
        }
      };
      //Validate TaskGroup Entry
      $scope.validateTaskGroup = function(form) {
        var message = '';
        var taskGroupGridValid = true;
        var taskGroupGridBlankFieldMessage = '';
        $rootScope.alert.hideAlert();
        if(form.taskGroupName.$error.required){
          if(message.length>0){ message += localize.getLocalizedString('_CommaLabel_'); }
          message += localize.getLocalizedString('_TaskGroupKeyMissing_');
        }
        if ($scope.taskGroupGroupLabelsGridOptions.data.length % 2 === 0) {
          if ($scope.taskGroupGroupLabelsGridOptions.data.length < 2) {
            taskGroupGridBlankFieldMessage = localize.getLocalizedString('_TaskGroupLabelsGridNotEnoughLabels_');
            taskGroupGridValid = false;
          }else if ($scope.taskGroupGroupLabelsGridOptions.data.length === 2) {
            for (var i = 0; i < $scope.taskGroupGroupLabelsGridOptions.data.length; i++) {
              if ($scope.taskGroupGroupLabelsGridOptions.data[i].text.trim().length <= 0 || $scope.taskGroupGroupLabelsGridOptions.data[i].lang.trim().length <= 0) {
                taskGroupGridBlankFieldMessage = localize.getLocalizedString('_TaskGroupLabelsGridBlankFieldAlert_');
                taskGroupGridValid = false;
                break;
              }
            }
            var en = 0;
            for (var i = 0; i < $scope.taskGroupGroupLabelsGridOptions.data.length; i++) {
              if ($scope.taskGroupGroupLabelsGridOptions.data[i].lang === 'EN') {
                en++;
              }
            }
            if (en === 0 || en === 2) {
              taskGroupGridBlankFieldMessage = localize.getLocalizedString('_TaskGroupLabelsGridNotEnoughLabels_');
              taskGroupGridValid = false;
            }
          }
        }else {
          taskGroupGridBlankFieldMessage = localize.getLocalizedString('_TaskGroupLabelsGridEN_FRAlert_');
          taskGroupGridValid = false;
        }
        if(message.length>0){
          message += localize.getLocalizedString('_Required_');
        }
        if(message.length>0 || !taskGroupGridValid){
         $window.alert(message+taskGroupGridBlankFieldMessage);
         return false;
        }
        return true;
      };
      //Saves the values on the form to the current task group displayed
      $scope.saveTaskGroup = function(form) {
        var validTaskGroup = $scope.validateTaskGroup(form);
        if (validTaskGroup) {
          if ($scope.modalFormLabel === localize.getLocalizedString('_NewTaskGroupModalLabel_')) {
            $scope.cohortGroups.push({
              "key": $scope.taskGroupName,
              "label": $scope.taskGroupGroupLabelsGridOptions.data,
              "taskList": []
            });
          } else if ($scope.modalFormLabel === localize.getLocalizedString('_EditTaskGroupModalLabel_')) {
            var index = null;
            for (var i = 0; i < $scope.cohortGroups.length; i++) {
              if ($scope.cohortGroups[i].$$hashKey.slice($scope.cohortGroups[i].$$hashKey.indexOf(':')+1) === $scope.selectedTaskGroup) {
                index = i;
                break;
              }
            }
            $scope.cohortGroups[index].key = $scope.taskGroupName;
            $scope.cohortGroups[index].label = $scope.taskGroupGroupLabelsGridOptions.data;
          }
          $('#taskGroupModal').modal('hide');
        }
      };
      $scope.taskGroupGroupLabelsGridOptions.onRegisterApi = function(gridApi){
        //set gridApi on scope of Offer Statuses Grid
        $scope.taskGroupGroupLabelsGridApi = gridApi;
      };
      $scope.taskGroupLabelsGridOptions.onRegisterApi = function(gridApi){
        //set gridApi on scope of Offer Statuses Grid
        $scope.taskGroupLabelsGridApi = gridApi;
      };
  }])
  .directive('showOnLoad', function() {
    return {
      restrict: 'A',
      link: function($scope,elem) {
        elem.hide();
        $scope.$on('roles-loaded', function() {
          elem.show();
        });
      }
    };
  })
  .directive('dateTimePicker', function() {
    return {
      restrict: "A",
      require: "ngModel",
      link: function (scope, element, attrs, ngModelCtrl) {
        var parent = angular.element(element).parent();
        var dtp = parent.datetimepicker({
          format: "YYYY-MM-DD",
          ignoreReadonly: true,
          showClear: true,
          useCurrent: false,
          showTodayButton: true,
          widgetPositioning: {
            horizontal: 'auto',
            vertical: 'bottom'
          }
        });
        dtp.on("dp.change", function (e) {
          if(element[0].name === 'startDate') {
            if (!e.date) {
              angular.element(document.getElementById('startDate')).parent().data("DateTimePicker").clear();
              ngModelCtrl.$setViewValue(moment('').format("YYYY-MM-DD"));
              angular.element(document.getElementById('disableDate')).parent().data("DateTimePicker").minDate(false);
              scope.$apply();
            } else {
              ngModelCtrl.$setViewValue(moment(e.date).format("YYYY-MM-DD"));
              angular.element(document.getElementById('disableDate')).parent().data("DateTimePicker").minDate(e.date);
              scope.$apply();
            }
          } else if (element[0].name === 'disableDate') {
            if (!e.date) {
              angular.element(document.getElementById('disableDate')).parent().data("DateTimePicker").clear();
              ngModelCtrl.$setViewValue(moment('').format("YYYY-MM-DD"));
              angular.element(document.getElementById('startDate')).parent().data("DateTimePicker").maxDate(false);
              scope.$apply();
            } else {
              if (moment(e.date).isAfter(angular.element(document.getElementById('startDate'))[0].value)) {
                ngModelCtrl.$setViewValue(moment(e.date).format("YYYY-MM-DD"));
                angular.element(document.getElementById('startDate')).parent().data("DateTimePicker").maxDate(e.date);
                scope.$apply();
              }else {
                angular.element(document.getElementById('disableDate')).parent().data("DateTimePicker").clear();
                ngModelCtrl.$setViewValue(moment('').format("YYYY-MM-DD"));
                angular.element(document.getElementById('startDate')).parent().data("DateTimePicker").maxDate(false);
                scope.$apply();
              }
            }
          } else if (element[0].name === 'dueDate') {
            if (!e.date) {
              angular.element(document.getElementById('dueDate')).parent().data("DateTimePicker").clear();
              ngModelCtrl.$setViewValue(moment('').format("YYYY-MM-DD"));
              scope.$apply();
            } else {
              ngModelCtrl.$setViewValue(moment(e.date).format("YYYY-MM-DD"));
              scope.$apply();
            }
          }
        });
      }
    };
  })
  .directive('fileModel', ['$parse', function ($parse) {
      return {
         restrict: 'A',
         link: function(scope, element, attrs) {
            var model = $parse(attrs.fileModel);
            var modelSetter = model.assign;
            element.bind('change', function(){
               scope.$apply(function(){
                  modelSetter(scope, element[0].files[0]);
               });
            });
         }
      };
   }])
  .filter('mapLangLabel', function() {
    var langHash = {
      'EN': 'EN',
      'FR': 'FR'
    };
    return function(input) {
      if (!input) {
        return '';
      } else {
        return langHash[input];
      }
    };
  });
