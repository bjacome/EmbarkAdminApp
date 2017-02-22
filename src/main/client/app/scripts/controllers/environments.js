'use strict';

/**
 * @ngdoc function
 * @name rbcembarkAdminApp.controller:EnvironmentsCtrl
 * @description
 * # EnvironmentsCtrl
 * Controller of the rbcembarkAdminApp
 */
angular.module('rbcembarkAdminApp')
  .controller('EnvironmentsCtrl',['$scope', '$log', '$rootScope', 'restService', 'localize','cfpLoadingBar', '$timeout', 'uiGridConstants',
  function ($scope, $log, $rootScope, restService, localize, cfpLoadingBar, $timeout, uiGridConstants) {

    $scope.formLabel = '';
    $scope.removeRow = false;
    $scope.loading = true;
    $scope.environments = null;
    $scope.newEnvironmentForm = {};
    $scope.credentialsForm = {};

    restService.setData({});
    restService.setService('api/rest/admin/environment/retrieve-all');
    cfpLoadingBar.start();
    restService.doPost().then(function(response) {
      cfpLoadingBar.complete();
      $scope.loading = false;
      if (response !== undefined && response.status === "success") {
        $scope.embarkEnvironmentsGridOptions.data = response.data;
      }
    });

    // Setting of the Environments grid
    $scope.embarkEnvironmentsGridOptions = {
      'enableHorizontalScrollbar': uiGridConstants.scrollbars.NEVER,
      'enableColumnMenus': false,
      'enableRowSelection': true,
      'modifierKeysToMultiSelect': false,
      'multiSelect': false,
      'enableRowHeaderSelection': false,
      'columnDefs': [
         { 'name': 'name', 'field': 'name', 'cellEditableCondition': false, 'displayName': 'Name', 'width': '40%'},
         { 'name': 'location', 'field': 'location', 'cellEditableCondition': false, 'displayName': 'Location','width':'50%'},
         { 'name': '', 'field': 'checkMark', 'cellEditableCondition': false, 'cellTemplate': '<button class="btn btn-primary btn-xs glyphicon glyphicon-remove removeButton" data-toggle="modal" data-target=".bs-example-modal-dl" data-show="tip" data-placement="auto left" title="Remove Environment" ng-click="grid.appScope.Delete(\'embarkEnvironmentsGridOptions\',row)"></button>', 'enableHiding': false}
       ]};
     //Fetch Environment Grid
     $scope.fetchGrid = function(){
       $scope.loading = true;
       restService.setData({});
       restService.setService('api/rest/admin/environment/retrieve-all');
       restService.doPost().then(function(response) {
         cfpLoadingBar.complete();
         $scope.loading = false;
         if (response !== undefined && response.status === "success") {
           $scope.embarkEnvironmentsGridOptions.data = response.data;
         }
       });
     };
     //Watchers for forms data change
     $scope.$watch('forms.newEnvironmentForm', function(form) {
       if(form) {
         $scope.newEnvironmentForm = form;
       }
     });
     $scope.$watch('forms.credentialsForm', function(form) {
       if(form) {
         $scope.credentialsForm = form;
       }
     });

     //Grid Adjustment functions
     $scope.GridAdjust = function(gridOps, id) {
       if ($scope[gridOps].data.length > 10){
         angular.element(document.getElementById(id)).css('height', (((10 + 1)*30)+5) + 'px');
       }else {
         angular.element(document.getElementById(id)).css('height', ((($scope[gridOps].data.length + 1)*30)+5) + 'px');
       }
     };
     //To show the Add New Embark Environments form
     $scope.showNewEnvironmentForm = function(eForm, cForm) {
       if ($scope.formLabel === '_NewEnvironmentTitle_') {
         if (!eForm.modified && !cForm.modified) {
           $scope.formLabel = '_NewEnvironmentTitle_';
           $scope.environmentName = '';
           $scope.environmentLocation = '';
           $scope.username = '';
           $scope.password = '';
           $scope.hostName = '';
           $scope.port = '';
           $scope.environmentURL = '';
         }else {
           $rootScope.alert.showWarningAlert(localize.getLocalizedString('_EditEnvironmentWarning_'));
         }
       }else if ($scope.formLabel === '_EditEnvironment_') {
         if (!eForm.$dirty && !cForm.$dirty) {
           $scope.formLabel = '_NewEnvironmentTitle_';
           $scope.environmentName = '';
           $scope.environmentLocation = '';
           $scope.username = '';
           $scope.password = '';
           $scope.hostName = '';
           $scope.port = '';
           $scope.environmentURL = '';
         }else {
           $rootScope.alert.showWarningAlert(localize.getLocalizedString('_EditEnvironmentWarning_'));
         }
       }else {
         $scope.formLabel = '_NewEnvironmentTitle_';
         $scope.environmentName = '';
         $scope.environmentLocation = '';
         $scope.username = '';
         $scope.password = '';
         $scope.hostName = '';
         $scope.port = '';
         $scope.environmentURL = '';
       }
     };
     //Saving the Environment
     $scope.saveEnvironment = function(eForm,cForm) {
       //Remaining logic To be implemented
       var validateForm = $scope.validate(eForm,cForm);
       if (validateForm) {
         cfpLoadingBar.start();
         if($scope.formLabel === '_NewEnvironmentTitle_') {
           var envData = {
             "name": eForm.environmentName.$modelValue,
             "location": eForm.environmentLocation.$modelValue,
             "url": cForm.environmentURL.$modelValue,
             "host": cForm.hostName.$modelValue,
             "port": cForm.port.$modelValue,
             "userName": cForm.username.$modelValue,
             "password": cForm.password.$modelValue
           };
           var successAlert = localize.getLocalizedString('_EnvironmentAdded_');
         } else {
           var envData = {
             "_id": $scope._id,
             "_rev": $scope._rev,
             "name": eForm.environmentName.$modelValue,
             "location": eForm.environmentLocation.$modelValue,
             "url": cForm.environmentURL.$modelValue,
             "host": cForm.hostName.$modelValue,
             "port": cForm.port.$modelValue,
             "userName": cForm.username.$modelValue,
             "password": cForm.password.$modelValue
           };
           var successAlert = localize.getLocalizedString('_EnvironmentUpdated_');
         }
         var formattedEnvData = {
           "data": envData
         };
         restService.setData(angular.toJson(formattedEnvData,true));
         restService.setHeader('Content-Type', 'application/json;charset=utf-8');
         restService.setService('api/rest/admin/environment/save');
         restService.doPost().then(function(response) {
           if(response !== undefined && response.status === "success") {
             $scope.fetchGrid();
             $scope.formLabel = '';
             $rootScope.alert.hideAlert();
             $rootScope.alert.showSuccessAlert(successAlert);
             $timeout(function(){
               $rootScope.alert.hideAlert();
             },3000);
           } else {
             $rootScope.alert.showErrorAlert(localize.getLocalizedString('_Error_'));
           }
         });
        }
     };

     $scope.validate = function(eForm,cForm) {
       var message = '';
       var portMessage = '';
       var urlMessage = '';
       $rootScope.alert.hideAlert();
       if(eForm.environmentName.$error.required){
         message = localize.getLocalizedString('_EnvironmentNameMissing_');
       }
       if (eForm.environmentLocation.$error.required) {
        if(message.length>0){ message += localize.getLocalizedString('_CommaLabel_'); }
        message += localize.getLocalizedString('_EnvironmentLocationMissing_');
       }
       if (cForm.username.$error.required) {
        if(message.length>0){ message += localize.getLocalizedString('_CommaLabel_'); }
        message += localize.getLocalizedString('_UserNameMissing_');
       }
       if (cForm.password.$error.required) {
        if(message.length>0){ message += localize.getLocalizedString('_CommaLabel_'); }
        message += localize.getLocalizedString('_PasswordMissing_');
       }
       if (cForm.hostName.$error.required) {
        if(message.length>0){ message += localize.getLocalizedString('_CommaLabel_'); }
        message += localize.getLocalizedString('_HostMissing_');
       }
       if (cForm.port.$error.required) {
        if(message.length>0){ message += localize.getLocalizedString('_CommaLabel_'); }
        message += localize.getLocalizedString('_PortMissing_');
       } else if (cForm.port.$error.number) {
          portMessage += localize.getLocalizedString('_InvalidPort_');
       }
       if (cForm.environmentURL.$error.required) {
        if(message.length>0){ message += localize.getLocalizedString('_CommaLabel_'); }
        message += localize.getLocalizedString('_UrlMissing_');
       } else if (cForm.environmentURL.$error.url) {
          urlMessage += localize.getLocalizedString('_InvalidUrl_');
       }
       if(message.length>0){
         message += localize.getLocalizedString('_Required_');
       }
       if(message.length>0 || portMessage.length>0 || urlMessage.length>0){
        $rootScope.alert.showErrorAlert(message+portMessage+urlMessage);
        return false;
       }
       return true;
     };
     //Close the Environment
     $scope.closeEnvironment = function() {
       //Remaining logic To be implemented
       $rootScope.alert.hideAlert();
       $scope.formLabel = '';
     };
     angular.element(document).ready(function () {
       $rootScope.alert.hideAlert();
       $("body").tooltip({ selector: '[data-show=tip]' });
     });
     //Delete grid rows
     $scope.Delete = function(grid, row) {
       $scope.removeRow = true;
       $scope.delMe = grid;
       $scope.delIndex = row;
     };
     //Cancel Deletion
     $scope.cancelDelete = function() {
       $scope.removeRow = false;
     };

   //Confirmed delete Admin User
   $scope.confirmedDelete = function() {
     var row = $scope.delIndex;
      $scope.removeRow = true;
       cfpLoadingBar.start();
       var deleteEnvRow = {
         "_id": row.entity._id,
         "_rev": row.entity._rev
       };
       var formattedDeleteEnvRow = {
         "data": deleteEnvRow
       };
       restService.setData(angular.toJson(formattedDeleteEnvRow,true));
       restService.setHeader('Content-Type', 'application/json;charset=utf-8');
       restService.setService('api/rest/admin/environment/delete');
       restService.doPost().then(function(response) {
         cfpLoadingBar.complete();
         if(response !== undefined && response.status === "success") {
           $scope.fetchGrid();
           $scope.formLabel = '';
           $rootScope.alert.hideAlert();
           $rootScope.alert.showSuccessAlert(localize.getLocalizedString('_EnvironmentRemoved_'));
           $timeout(function(){
             $rootScope.alert.hideAlert();
           },3000);
           $scope.removeRow = false;
         } else {
           $rootScope.alert.showErrorAlert(localize.getLocalizedString('_Error_'));
         }
       });

     };


     $scope.embarkEnvironmentsGridOptions.onRegisterApi = function(gridApi){
       //set gridApi on scope of Embark Environments Grid
       $scope.embarkEnvironmentsGridApi = gridApi;
       $scope.embarkEnvironmentsGridApi.selection.on.rowSelectionChanged($scope,function(row){
         if(!$scope.removeRow) {
           if($scope.formLabel === '_NewEnvironmentTitle_'){
             if (!$rootScope.forms.newEnvironmentForm.$dirty && !$rootScope.forms.credentialsForm.$dirty) {
               $scope.environmentName = row.entity.name;
               $scope.environmentLocation = row.entity.location;
               $scope.username = row.entity.userName;
               $scope.password = row.entity.password;
               $scope.hostName = row.entity.host;
               $scope.port = row.entity.port;
               $scope.environmentURL = row.entity.url;
               $scope.formLabel = '_EditEnvironment_';
               $scope._id = row.entity._id;
               $scope._rev = row.entity._rev;
             }else {
               $rootScope.alert.showWarningAlert(localize.getLocalizedString('_EditEnvironmentWarning_'));
             }
           }else if ($scope.formLabel === '_EditEnvironment_') {
             if (!$rootScope.forms.newEnvironmentForm.$dirty && !$rootScope.forms.credentialsForm.$dirty) {
               $scope.environmentName = row.entity.name;
               $scope.environmentLocation = row.entity.location;
               $scope.username = row.entity.userName;
               $scope.password = row.entity.password;
               $scope.hostName = row.entity.host;
               $scope.port = row.entity.port;
               $scope.environmentURL = row.entity.url;
               $scope.formLabel = '_EditEnvironment_';
               $scope._id = row.entity._id;
               $scope._rev = row.entity._rev;
             }else {
               $rootScope.alert.showWarningAlert(localize.getLocalizedString('_EditEnvironmentWarning_'));
             }
           }else {
                $scope.environmentName = row.entity.name;
                $scope.environmentLocation = row.entity.location;
                $scope.username = row.entity.userName;
                $scope.password = row.entity.password;
                $scope.hostName = row.entity.host;
                $scope.port = row.entity.port;
                $scope.environmentURL = row.entity.url;
                $scope.formLabel = '_EditEnvironment_';
                $scope._id = row.entity._id;
                $scope._rev = row.entity._rev;
          }
        }
       });
     };

}]);
