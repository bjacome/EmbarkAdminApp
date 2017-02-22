'use strict';

/**
 * @ngdoc function
 * @name rbcembarkAdminApp.controller:AdminUsersCtrl
 * @description
 * # AdminUsersCtrl
 * Controller of the rbcembarkAdminApp
 */
angular.module('rbcembarkAdminApp')
.controller('AdminUsersCtrl',['$scope', '$rootScope', 'restService', 'localize','cfpLoadingBar', function ($scope, $rootScope, restService, localize, cfpLoadingBar, $modal) {
    $scope.formLabel = '';
    $scope.removeRow = false;
    $scope.loading = true;
    $scope.adminForm = {};
    $scope.assignedEmbarkGridDataChanged = [];
    $scope.delMe = null;
    $scope.delIndex = null;
    $scope.del = false;
    //Left Admin Users grid
    $scope.adminUsersGrid = {
      'enableRowSelection': true,
      'enableRowHeaderSelection': false,
      'enableColumnMenus': false,
      'multiSelect': false,
      'modifierKeysToMultiSelect': false,
      'columnDefs': [
        { 'name': 'name', 'field': 'fullName', 'enableHiding': false, 'width': '45%' },
        { 'name': 'email', 'field': 'email', 'enableHiding': false, 'width': '45%' },
        { 'name': '', 'field': 'checkMark', 'cellEditableCondition': false, 'cellTemplate': '<button class="btn btn-primary btn-xs glyphicon glyphicon-remove removeButton" data-toggle="modal" data-target=".bs-example-modal-dl" ng-click="grid.appScope.Delete(\'adminUsersGrid\',row)"></button>', 'enableHiding': false}
      ]
    };
      $scope.adminUsersGrid.enableHorizontalScrollbar = 0;

      //Fetch/refresh admin user Grid
      $scope.fetchGrid = function(){
        $rootScope.alert.hideAlert();
        $scope.loading = true;
        restService.setData({});
        restService.setService('api/rest/admin/adminUser/retrieve-all');
        restService.doPost().then(function(response) {
          cfpLoadingBar.complete();
          $scope.loading = false;
          if (response !== undefined && response.code === 200) {
            $scope.adminUsersGrid.data = response.data;
          }
        });
      };

      //Refresh the grid with admin users retreived from rest service
      cfpLoadingBar.start();
      $scope.fetchGrid();

      //Delete admin user
      $scope.Delete = function(grid, row) {
        if($scope.isDirtyWarning()) {
          return
        }
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
      $scope.removeRow = false;
      cfpLoadingBar.start();
      var row = $scope.delIndex;
      var deleteAdminRow = {
        "_id": row.entity._id,
        "_rev": row.entity._rev
      };
      var formattedDeleteAdminRow = {
        "data": deleteAdminRow
      };
      restService.setData(angular.toJson(formattedDeleteAdminRow,true));
      restService.setHeader('Content-Type', 'application/json;charset=utf-8');
      restService.setService('api/rest/admin/adminUser/delete');


      restService.doPost().then(function(response) {
        cfpLoadingBar.complete();
        $scope.loading = false;

        if(response !== undefined && response.code === 200) {
          $scope.formLabel = '';
          $rootScope.alert.showSuccessAlert(localize.getLocalizedString('_AdminUserRemoved_'));
          for(var k in $scope.adminUsersGrid.data) {
            if($scope.adminUsersGrid.data[k]._id === row.entity._id) {
              $scope.adminUsersGrid.data.splice(k,1);
            }
          }
          $scope.removeRow = false;
        } else {
          $rootScope.alert.showErrorAlert(localize.getLocalizedString('_AdminUserRemoveError_'));
        }
      });
      $scope.delMe = null;
      $scope.delIndex = null;
    };

    //Poulate Edit form with selected user row
    $scope.adminUsersGrid.onRegisterApi = function( gridApi ) {
      $scope.gridApi = gridApi;
        gridApi.selection.on.rowSelectionChanged($scope,function(row){
          if(!$scope.removeRow) {
              $scope.editAdminUser(row.entity);
          }
        });
    };

    //show warning if form is already open with modified data
    $scope.isDirtyWarning = function() {
      $rootScope.alert.hideAlert();
      //show warning if form is already open with modified data
      if($scope.formLabel !== '' && ($scope.adminForm.$dirty || $scope.isRoleGridDataChanged())) {
        $rootScope.alert.showWarningAlert(localize.getLocalizedString('_EditAdminWarning_'));
        return true;
      }
      return false;
    };

    //Add New Admin User, empty and default values
    $scope.addAdminUser = function() {

      if($scope.isDirtyWarning()) {
        return
      }

      $scope.assignedEmbarkRolesGrid.data = [];
      $scope._id = null;
      $scope._rev = null;
      $scope.fullname = {};
      $scope.email = {};
      $scope.password = {};
      $scope.Superuser = {value: 'No'};
      $scope.formLabel = '_NewAdminUserTitle_';
    };

    //scope access to admin users form
    $scope.$watch('forms.addAdminUserForm', function(form) {
      if(form) {
        $scope.adminForm = form;
      }
    });

    //Edit New Admin User
    $scope.editAdminUser = function(entity) {

      if($scope.isDirtyWarning()) {
        return
      }

      //save the grid data before any change
      $scope.assignedEmbarkGridDataChanged = entity.roles;
      //populate the form and show Edit label
      $scope.populateForm(angular.copy(entity));
      $scope.formLabel = '_EditAdminUserTitle_';
    };

    //Populate the form with select edit row
    $scope.populateForm = function(data) {
      var suser = data.superUser? 'Yes' : 'No';
      $scope._id = data._id;
      $scope._rev = data._rev;
      $scope.fullname = {value: data.fullName};
      $scope.email = {value: data.email};
      $scope.password = {};
      $scope.Superuser = {value: suser};
      $scope.assignedEmbarkRolesGrid.data = data.roles;
    };

    //Assigned Roles grid
      $scope.assignedEmbarkRolesGrid = { enableRowSelection: true, enableRowHeaderSelection: false, enableColumnMenus: false };
      $scope.assignedEmbarkRolesGrid.multiSelect = false;
      $scope.assignedEmbarkRolesGrid.modifierKeysToMultiSelect = false;
      $scope.assignedEmbarkRolesGrid.columnDefs = [
          { name: 'role', field: 'role', enableHiding: false, width: '45%' },
          { name: 'title', field: 'title', enableHiding: false, width: '45%' },
          { name: '', field: 'checkMark', cellEditableCondition: false, cellTemplate: '<button class="btn btn-primary btn-xs glyphicon glyphicon-remove removeButton" ng-click="grid.appScope.deleteRole(\'assignedEmbarkRolesGrid\',row)"></button>', enableHiding: false}
        ];
      $scope.assignedEmbarkRolesGrid.enableHorizontalScrollbar = 0;
    //Delete Assigned Roles grid row
    $scope.deleteRole = function(grid, row) {
      var index = $scope[grid].data.indexOf(row.entity);
      $scope[grid].data.splice(index, 1);
    };

    //Popup screen grid for select
    $scope.embarkRolesGrid = { enableRowSelection: true, enableRowHeaderSelection: false, enableColumnMenus: false };
    $scope.embarkRolesGrid.multiSelect = false;
    $scope.embarkRolesGrid.modifierKeysToMultiSelect = false;
    $scope.embarkRolesGrid.noUnselect = true;
    $scope.embarkRolesGrid.columnDefs = [
        { name: 'roleId', displayName: 'Role ID', field: 'name', enableHiding: false, width: '40%'  },
        { name: 'roleName', field: 'roleName', enableHiding: false,  width: '60%'}
      ];
      $scope.embarkRolesGrid.enableHorizontalScrollbar = 0;
      //Rest service parameters for retreiving roles
      restService.setData({});
      restService.setService('api/rest/admin/role/retrieve-all');
      restService.doPost().then(function(response) {
        $scope.loading = false;
        if (response !== undefined) {
          $scope.embarkRolesGrid.data = $scope.getRoles4Form(response.data);
        }
      });

      //Extract the Role ID & RoleName from rest result, need to implement EN/FR selection
      $scope.getRoles4Form = function(restData) {
        var roles = [];
        for (var record in restData) {
          var text = restData[record].title !== undefined? restData[record].title[0].text:''; //choosed 0: EN for now
          roles.push({ "name": restData[record].name, "roleName": text});
        }
        return roles;
      };

      //Fill Assign role grid from popup
      $scope.embarkRolesGrid.onRegisterApi = function( gridApi ) {
        $scope.gridApi = gridApi;
          gridApi.selection.on.rowSelectionChanged($scope,function(row){
            $scope.AssignRole(row.entity.name, row.entity.roleName);
          });
      };

      //Grid Adjustment functions
      $scope.GridAdjust = function(gridOps, id) {
        if(!angular.isDefined($scope[gridOps].data)) {
          $scope[gridOps].data = [];
        }

        if ($scope[gridOps].data.length > 16){
          angular.element(document.getElementById(id)).css('height', (((16 + 1)*30)+5) + 'px');
        }else {
          angular.element(document.getElementById(id)).css('height', ((($scope[gridOps].data.length + 1)*30)+5) + 'px');
        }
      };

    //Check if Assign Embark Roles Grid data changed
    $scope.isRoleGridDataChanged = function() {
      return !angular.equals($scope.assignedEmbarkRolesGrid.data,$scope.assignedEmbarkGridDataChanged)
    }

    //Save Admin User, validate form data with alerts, submit data
    $scope.saveAdminUser = function(form) {
      //hide any previuos alerts
      $rootScope.alert.hideAlert();
      var validateForm = $scope.validate(form);
      var rolesData = [];
      var successMsg = localize.getLocalizedString('_NewAdminUserCreatedSuccess_');
      //Check if the form is modified
      if(!form.$dirty && !$scope.isRoleGridDataChanged()) {
        $rootScope.alert.showSuccessAlert(localize.getLocalizedString("_EditAdminUserSuccess_"));
        return
      }
      //submit the form Values
      if(validateForm) {
        //Rest service parameters for adding admin user
      var isSuper =  form.sUser.$viewValue === 'No'? false : true;
      var addUserdata = {};

      var data = {
            "_id": $scope._id,
            "_rev": $scope._rev,
            "email": form.email.$viewValue,
            "fullName": form.fullname.$viewValue,
            "superUser": isSuper,
            "temporaryPassword":form.password.$viewValue
        };

        var erroMsg = localize.getLocalizedString('_NewAdminUserCreatedError_')
        if(data._id !== null) {
          erroMsg = localize.getLocalizedString('_EditAdminUserError_');
          successMsg = localize.getLocalizedString('_EditAdminUserSuccess_');
        }
        //prepare roles array
        if(!isSuper) {
          if($scope.assignedEmbarkRolesGrid.data.length > 0) {
            for (var key in $scope.assignedEmbarkRolesGrid.data) {
              rolesData.push({ 'role': $scope.assignedEmbarkRolesGrid.data[key].role});
            }
            data.roles = rolesData;
          }
        }
        addUserdata.data = data;
        restService.setData(angular.toJson(addUserdata));
        restService.setService('api/rest/admin/adminUser/save');
        //Call rest service to add new admin user
        cfpLoadingBar.start();
         restService.doPost().then(function(response) {
          cfpLoadingBar.complete();
          $scope.loading = false;
          if (response.data !== undefined && response.code === 200) {
            $scope.formLabel = '';
            $rootScope.alert.showSuccessAlert(successMsg);
            //update grid
            if(data._id !== null) {
              //loop to find the updated the record then remove it
              for(var k in $scope.adminUsersGrid.data) {
                if($scope.adminUsersGrid.data[k]._id === response.data._id) {
                  $scope.adminUsersGrid.data.splice(k,1);
                }
              }
            }

              var upData = {
                "_id": response.data._id,
                "_rev": response.data._rev,
                "email":response.data.email,
                "fullName":response.data.fullName,
                "superUser":response.data.superUser
              };

              if(angular.isDefined(response.data.roles) && response.data.roles !== null && !response.data.superUser) {
                upData.roles = response.data.roles;
              }
              $scope.adminUsersGrid.data.push(upData);
            $scope.formLabel = '';

          } else {
            $rootScope.alert.showErrorAlert(erroMsg);
          }
        });
      }
    };


    //Validate the form controls
    $scope.validate = function(form) {
      var message = '',
      superUsermsg = '',
      Passmsg = '',
      Emailmsg = '',
      Duplicatemsg = '';
      //hide any previuos alerts
      $rootScope.alert.hideAlert();

        if(form.fullname.$invalid) {
          message = localize.getLocalizedString('_fullNameLabel_');
        }
        if(form.email.$invalid) {
          if(message.length>0){ message += ', '; }
          message += localize.getLocalizedString('_emailLabel_');
        } else {
          var validEmailPattern = /^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$/i;
          if ( !validEmailPattern.test(form.email.$viewValue)) {
            Emailmsg = localize.getLocalizedString('_InvalidEmailFormatError_');
          } else {
            for(var au in $scope.adminUsersGrid.data ) {
              if($scope._id === null && form.email.$viewValue === $scope.adminUsersGrid.data[au].email) {
                Duplicatemsg = localize.getLocalizedString('_AdminUserExist_');
              }
            }
          }
        }

        //Validate password only on adding New Admin User, _id is null
        if($scope._id == null) {
          if(form.password.$invalid) {
            if(message.length>0){ message += ', '; }
            message += localize.getLocalizedString('_passwordLabel_');
          } else {
            if(form.password.$valid && ((form.password.$viewValue.indexOf(' ') >= 0 || form.password.$viewValue.length <= 6 || $scope.passCheckFail(form.password.$viewValue)))){
              if(message.length>0){ message += ', '; }
              Passmsg = localize.getLocalizedString('_ErrorPassword_');
            }
          }
        }
        if(form.sUser.$viewValue === 'No' && (angular.isUndefined($scope.assignedEmbarkRolesGrid.data) || $scope.assignedEmbarkRolesGrid.data.length === 0)){
           superUsermsg = localize.getLocalizedString('_ErrorRoleNonSuperuser_');
        }
        if(message.length>0){
          message += localize.getLocalizedString('_Required_');
        }
        if(message.length>0 || superUsermsg.length>0 || Emailmsg.length > 0 || Passmsg.length >0 || Duplicatemsg.length>0){
          //Display clustered alert messages
          $rootScope.alert.showErrorAlert(message + Passmsg + Emailmsg + superUsermsg + Duplicatemsg);
          return false;
        }
        return true;
    };

    //Check if password doesn't comply with policy - (c)
    $scope.passCheckFail = function(pass) {
      var charCheckPass = 4;

            var upperCharPattern = /[A-Z]/;
            if (!upperCharPattern.test(pass)) {
                charCheckPass--;
            }

            var lowerCharPattern = /[a-z]/;
            if (!lowerCharPattern.test(pass)) {
                charCheckPass--;
            }

            var numberCharPattern = /[0-9]/;
            if (!numberCharPattern.test(pass)) {
                charCheckPass--;
            }

            var specialCharPattern = /[!@#$%^&*()+=\-[\]\\';,./{}|":<>?~_]/;
            if (!specialCharPattern.test(pass)) {
                charCheckPass--;
            }

            if (charCheckPass < 3) {
                return true;
            } else { return false; }
    };

    //Cancel Admin User
    $scope.cancelAdminUser = function() {
      //hide any previuos alerts
      $rootScope.alert.hideAlert();
      $scope.formLabel = '';
    };

    //Assign a Role to current admin user
    $scope.AssignRole = function(roleID, roleName) {
        //AlreadyIn to avoid inserting already exist row
        var alreadyIn = false;
        if(angular.isDefined($scope.assignedEmbarkRolesGrid.data)) {
          var gridLength = $scope.assignedEmbarkRolesGrid.data.length;
          //check if the row is already in the grid
          for(var key in $scope.assignedEmbarkRolesGrid.data) {
            if($scope.assignedEmbarkRolesGrid.data[key].role == roleID ) {
              alreadyIn = true;
            }
          }
        } else {
          var gridLength = 0;
          $scope.assignedEmbarkRolesGrid.data = [];
        }
        //add the row
        if(!alreadyIn) {
            $scope.assignedEmbarkRolesGrid.data.push({
              'role': roleID,
              'title': roleName
            });
          }
    };

    //Default Assign role grid data
    $scope.assignedEmbarkRolesGrid.data = [];

    //Preparing and Using Modal to show the popup window for selecting roles
    this.showModal = false;
    this.showView = false;
    this.counter = 1;
    this.toggleDialog = function () {
        this.showModal = !this.showModal;
    };
    this.toggleView = function () {
        this.showView = !this.showView;
    };
    this.changeDisplay = function () {
        this.counter++;
    };


}]);
