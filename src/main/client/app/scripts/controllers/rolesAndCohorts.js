'use strict';

/**
 * @ngdoc function
 * @name rbcembarkAdminApp.controller:RolesAndCohortsCtrl
 * @description
 * # RolesAndCohortsCtrl
 * Controller of the rbcembarkAdminApp
 */
angular.module('rbcembarkAdminApp')
  .filter('mapLang', function() {
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
  })
  .controller('RolesAndCohortsCtrl', ['$scope', '$rootScope', 'restService', 'cfpLoadingBar', 'localize', '$filter', '$timeout', '$window', function ($scope, $rootScope, restService, cfpLoadingBar, localize, $filter, $timeout, $window) {

    //These variables affect the layout of the page.
    $scope.collapseOrExpandButtonLabel = '_ExpandTree_';
    $scope.collapseOrExpandRButtonLabel = '_ExpandTree_';
    $scope.collapseOrExpandCButtonLabel = '_ExpandTree_';
    $scope.formLabel = '';
    $scope.modalFormLabel = 'New Task Group';
    $scope.newRoleButtonLabel = '_NewRole_';
    //These are the variables that the inputs on the forms are bound to.
    $scope.roleNameValue = {};
    $scope.taskGroupNameValue = {};
    $scope.taskIDValue = {};
    $scope.startDate = null;
    $scope.disableDate = null;
    $scope.dueDate = null;
    $scope.role = {};
    $scope.currentForm = {};
    $scope.newTaskGroupForm = {};
    $scope.newTaskForm = {};
    // 0 represents collapsed, 1 is expanded
    var istreeCollapsed = true;

    //Trees that are used in the controller
    $scope.roles = [];
    $scope.roleGroups = [];
    $scope.cohortGroups = [];
    $scope.currentTaskGroup = [];
    $scope.currentTree = [];

    $scope.restReady = false; //is the rest service busy
    $scope.loading = true;
    $scope.editMode = false;

    $scope.ROLE = 'Role';
    $scope.COHORT = 'Cohort';
    $scope.mode = null;

    $scope.delMe = null;
    $scope.delIndex = null;
    $scope.delRC = null;

    //fetch roles from rest service
    $scope.fetchRoles = function() {
      $rootScope.alert.hideAlert();
      $scope.loading = true;
      cfpLoadingBar.start();
      restService
      .setData({})
      .setService('api/rest/admin/role/retrieve-all')
      .doPost()
      .then(function(response) {
        cfpLoadingBar.complete();
        $scope.loading = false;
        //enable buttons
        $scope.restReady = true;
        if (angular.isDefined(response) && response.code === 200) {
          $scope.roles = response.data;
          if ($scope.hasRoles()) {
            istreeCollapsed = true;
            $timeout(function() {
              $scope.collapseOrExpandAll('mainTree','Main');
            });
          }
        }
      });
    };

    $scope.hasRoles = function() {
      return angular.isArray($scope.roles) && $scope.roles.length > 0;
    };

    //scope access to forms
    $scope.$watch('forms.newRoleForm', function(form) {
      if(form) {
        $scope.currentForm = form;
      }
    });
    $scope.$watch('forms.newCohortForm', function(form) {
      if(form) {
        $scope.currentForm = form;
      }
    });
    //Watcher for taskGroupForm data change
    $scope.$watch('forms.newTaskGroupForm', function(form) {
      if(form) {
        $scope.newTaskGroupForm = form;
      }
    });
    //Watcher for taskForm data change
    $scope.$watch('forms.newTaskForm', function(form) {
      if(form) {
        $scope.newTaskForm = form;
      }
    });


    //poplaute the page
    cfpLoadingBar.start();
    $scope.fetchRoles();

    //Select Role Logic
    $scope.setSelectedRole = function(role) {
      $scope.role = role;
      $scope.newRoleButtonLabel = '_NewRole_';
      $scope.selectedRoleID = role.$$hashKey.slice(role.$$hashKey.indexOf(':')+1);
      $scope.selectedCohortID = null;
    };
    //Select Cohort Logic
    $scope.setSelectedCohort = function(role, cohort) {
      $scope.role = role;
      $scope.selectedRoleID = role.$$hashKey.slice(role.$$hashKey.indexOf(':')+1);
      $scope.selectedCohortID = cohort.$$hashKey.slice(cohort.$$hashKey.indexOf(':')+1);
    };

    //To dismiss current form
    $scope.cancelForm = function() {
      $rootScope.alert.hideAlert();
      $scope.formLabel = '';
    };

    // To collapse or expand the whole tree
    $scope.collapseOrExpandAll  = function(target, flag) {
      switch(flag) {
        case 'Main':
        if (istreeCollapsed) {
          $scope.collapseOrExpandButtonLabel = '_ExpandTree_';
        } else {
          $scope.collapseOrExpandButtonLabel = '_CollapseTree_';
        }
        break;

        case 'Role':
        if (istreeCollapsed) {
          $scope.collapseOrExpandRButtonLabel = '_ExpandTree_';
        } else {
          $scope.collapseOrExpandRButtonLabel = '_CollapseTree_';
        }
        break;

        case 'Cohort':
        if (istreeCollapsed) {
          $scope.collapseOrExpandCButtonLabel = '_ExpandTree_';
        } else {
          $scope.collapseOrExpandCButtonLabel = '_CollapseTree_';
        }
        break;
      }

      var scope = angular.element(document.getElementById(target)).scope();
      if (!angular.isUndefined(scope)) {
        if (istreeCollapsed) {
          scope.$broadcast('angular-ui-tree:collapse-all');
        } else {
          scope.$broadcast('angular-ui-tree:expand-all');
        }
        istreeCollapsed = !istreeCollapsed;
      }
    };

    //show warning if form is already open with modified data
    $scope.isDirtyWarning = function() {
      $rootScope.alert.hideAlert();
      if($scope.formLabel === '') {
        return false;
      }
      //Check for Edited Role or Cohort
      var isEdited = false;
      if($scope.formLabel === localize.getLocalizedString('_NewRoleTitle_') || $scope.formLabel === localize.getLocalizedString('_EditRoleTitle_')) {
        isEdited = $scope.isEditedRole();
      } else if($scope.formLabel === localize.getLocalizedString('_NewCohortTitle_') || $scope.formLabel === localize.getLocalizedString('_EditCohortTitle_')) {
        isEdited = $scope.isEditedCohort();
      }
      //show warning if form is already open with modified data
      if($scope.formLabel !== '' && ($scope.currentForm.$dirty || isEdited ) ) {
        $rootScope.alert.showWarningAlert(localize.getLocalizedString('_EditRoleCohortWarning_'));
        return true;
      }
      return false;
    };

    //Checks if the cohort form has been Edited without being saved
    $scope.isEditedCohort = function() {

      var cohort = $scope.currentCohort;

      if($scope.isEqualGridToArray($scope.cohortTitlesGridOptions.data, cohort.title) &&
        $scope.isEqualGridToArray($scope.cohortGroups, cohort.taskGroupTree)) {
          return false;
      }

      return true;
    };

    //Checks if the role form has been Edited without being saved
    $scope.isEditedRole = function() {
      var role = $scope.currentRole;

      if($scope.roleNameValue.value === role.name &&
          $scope.isEqualGridToArray($scope.roleTitlesGridOptions.data, role.title) &&
          $scope.isEqualGridToArray($scope.offerStatusesGridOptions.data, role.offerStatuses) &&
          $scope.isEqualGridToArray($scope.roleGroups, role.taskGroupTree)) {
            return false;
      }

      return true;
    };

    //Checks if the displayed grid's data array is equal to the item's stored array
    $scope.isEqualGridToArray = function(gridData, array) {

      if(angular.isUndefined(gridData)) {
        gridData = [];
      }
      if(angular.isUndefined(array)) {
        array = [];
      }

      return angular.equals(gridData,array);

    };

    //Validates a cohort form
    $scope.isValidCohort = function(form) {
      var message = '';
      var Titlemsg = '';
      var TaskDueDatemsg = '';
      $rootScope.alert.hideAlert();

      if(form.cohortName.$invalid){
        message = localize.getLocalizedString('_cohortKey_'); //'A Cohort Key';
      }

      if(form.startDate.$invalid || form.startDate.$modelValue === null){
        if(message.length>0){ message += ', '; }
        message += localize.getLocalizedString('_StartDateMissing_');
      }
      if($scope.cohortTitlesGridOptions.data.length === 0 || $scope.invalidGrid($scope.cohortTitlesGridOptions.data)) {
        Titlemsg = localize.getLocalizedString('_ErrorTitleRowRequired_');
      }
      if(angular.isDefined($scope.cohortGroups) && $scope.cohortGroups.length > 0 ) {
        for(var tGroup in $scope.cohortGroups) {
          if(angular.isDefined($scope.cohortGroups[tGroup].taskList) && $scope.cohortGroups[tGroup].taskList.length > 0 ) {
            for(var tsk in $scope.cohortGroups[tGroup].taskList) {

              if(angular.isUndefined($scope.cohortGroups[tGroup].taskList[tsk].dueDate) || $scope.cohortGroups[tGroup].taskList[tsk].dueDate === null) {
                TaskDueDatemsg = localize.getLocalizedString('_ErrorTaskMissingDueDate_');
              }
            }
          }
        }
      }

      if(message.length>0){
        message += localize.getLocalizedString('_Required_');
      }
      if(message.length>0 || Titlemsg.length>0 || TaskDueDatemsg.length>0){
        //Display clustered alert messages
        $rootScope.alert.showErrorAlert(message + Titlemsg + TaskDueDatemsg);
        return false;
      }
      return true;
    };

    //Validates a role form
    $scope.isValidRole = function(form) {
      var message = '';
      var Titlemsg = '';
      var Statusmsg = '';
      $rootScope.alert.hideAlert();

      if(form.roleId.$invalid){
        message = localize.getLocalizedString('_roleKey_');
      }

      if($scope.roleTitlesGridOptions.data.length === 0 || $scope.invalidGrid($scope.roleTitlesGridOptions.data)) {
        Titlemsg = localize.getLocalizedString('_ErrorTitleRowRequired_');
      }
      if($scope.offerStatusesGridOptions.data.length === 0 || $scope.invalidStatusGrid($scope.offerStatusesGridOptions.data)){
        Statusmsg = localize.getLocalizedString('_ErrorStatusRowRequired_');
      }

      if(message.length>0){
        message += localize.getLocalizedString('_Required_');
      }
      if(message.length>0 || Titlemsg.length>0 || Statusmsg.length>0){
        //Display clustered alert messages
        $rootScope.alert.showErrorAlert(message + Titlemsg + Statusmsg);
        return false;
      }
      return true;
    };

    //Validate title grid for non-empty, one EN & one FR row
    $scope.invalidStatusGrid = function(gridData) {
      for(var i in gridData) {
        if(gridData[i].text.trim() === '') {
          return true;
        }
      }
      return false;
    };

    //Validate title grid for non-empty, one EN & one FR row
    $scope.invalidGrid = function(gridData) {
      var nullStr = 0;
      var enStr = 0;
      var frStr = 0;
      for(var i in gridData) {
        if(gridData[i].text.trim() === '' || gridData[i].lang.trim() === '') {
          nullStr++;
        }
        if(gridData[i].lang === 'EN') {
          enStr++;
        }
        if(gridData[i].lang === 'FR') {
          frStr++;
        }
      }
      if(nullStr > 0 || enStr < 1 || frStr < 1) {
        return true;
      }
      return false;
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
      if(form.dueDate && (form.dueDate.$viewValue === 'Invalid date' || form.dueDate.$viewValue === null)){
        if(message.length>0){ message += localize.getLocalizedString('_CommaLabel_'); }
        message += localize.getLocalizedString('_DueDateMissing_');
        dueDateValid = false;
      }
      if ($scope.taskGroupLabelsGridOptions.data.length % 2 === 0) {
        if ($scope.taskGroupLabelsGridOptions.data.length < 2) {
          taskGridBlankFieldMessage = localize.getLocalizedString('_TaskGroupLabelsGridNotEnoughLabelsAlert_');
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
            taskGridBlankFieldMessage = localize.getLocalizedString('_TaskGroupLabelsGridNotEnoughLabelsAlert_');
            taskGridValid = false;
          }
        }
      }else {
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

    //Validate TaskGroup Entry
    $scope.validateTaskGroup = function(form) {
      var message = '';
      var taskGroupGridValid = true;
      var taskGroupGridBlankFieldMessage = '';

      $rootScope.alert.hideAlert();
      if(form.taskGroupGroupName.$error.required){
        if(message.length>0){ message += localize.getLocalizedString('_CommaLabel_'); }
        message += localize.getLocalizedString('_TaskGroupKeyMissing_');
      }
      if ($scope.taskGroupGroupLabelsGridOptions.data.length % 2 === 0) {
        if ($scope.taskGroupGroupLabelsGridOptions.data.length < 2) {
          taskGroupGridBlankFieldMessage = localize.getLocalizedString('_TaskGroupLabelsGridNotEnoughLabelsAlert_');
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
            taskGroupGridBlankFieldMessage = localize.getLocalizedString('_TaskGroupLabelsGridNotEnoughLabelsAlert_');
            taskGroupGridValid = false;
          }
        }
      } else {
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

    //To load an existing cohort to the form
    $scope.loadCohort = function(cohort) {

      if($scope.isDirtyWarning()) {
        return;
      }

      $scope.mode = $scope.COHORT;

      $scope.currentCohort = cohort;

      if(angular.isUndefined(cohort._id)) {
        $scope.formLabel = localize.getLocalizedString('_NewCohortTitle_');
      } else {
        $scope.formLabel = localize.getLocalizedString('_EditCohortTitle_');
      }
      $scope.cohortNameValue = {value: angular.copy(cohort.name)};

      $scope.startDate = angular.copy(cohort.startDate);
      $scope.disableDate = angular.copy(cohort.disableDate);
      $scope.cohortTitlesGridOptions.data = angular.copy(cohort.title);
      $scope.cohortGroups = angular.copy(cohort.checklist);
      $scope.currentCohort.taskGroupTree = angular.copy(cohort.checklist);
    };

    //To load an existing role to the form (Add New or Edit)
    $scope.loadRole = function(role) {

      if($scope.isDirtyWarning()) {
        return;
      }

      $scope.mode = $scope.ROLE;

      $scope.currentRole = role;

      if(angular.isUndefined(role._id)) {
        $scope.formLabel = localize.getLocalizedString('_NewRoleTitle_');
      } else {
        $scope.formLabel = localize.getLocalizedString('_EditRoleTitle_');
      }

      $scope.roleNameValue = {value: angular.copy(role.name)};
      $scope.roleTitlesGridOptions.data = angular.copy(role.title);
      //prepare statuses array, add text tag
      if(angular.isDefined(role.statuses)) {
        var statuses = [];
        for(var status in role.statuses) {
          statuses.push({"text": role.statuses[status]});
        }
        $scope.offerStatusesGridOptions.data = angular.copy(statuses);
      }

      $scope.roleGroups = angular.copy(role.checklist);
      $scope.currentRole.taskGroupTree = angular.copy(role.checklist);
      $scope.currentRole.offerStatuses = $scope.offerStatusesGridOptions.data;

    };

    //check if task has no due date
    $scope.isUndated = function(task) {
      if(angular.isDefined(task.dueDate)) {
        if(task.dueDate === null) {
          return true;
        } else {
          return false;
        }
      } else {
        return true;
      }
    }
    //clear due date field (work around datepicker not bind to ng-model bug)
    $scope.clearDate = function() {
      $timeout(function() {
        var dueDate = document.getElementById('dueDate');
        if (angular.isDefined(dueDate)) {
          if (angular.isDefined(angular.element(dueDate).parent().data("DateTimePicker")))
            angular.element(dueDate).parent().data("DateTimePicker").clear();
        }
      }, 0, false);
    };

    //To load an new task to the form
    $scope.loadTask = function(task) {
      $scope.clearDate();

      task = task || {};
      //$scope.currentTask = task;
      $scope.taskNameValue = {value : ''};

      $scope.dueDate = null;
      $scope.taskGroupLabelsGridOptions.data = [];
      $scope.modalFormLabel = localize.getLocalizedString('_NewTaskModalLabel_');

      $scope.editMode = false;
    };

    //To load an existing task to the form
    $scope.editTask = function(task) {

      $scope.modalFormLabel = localize.getLocalizedString('_EditTaskModalLabel_');
      $scope.taskNameValue = {value : angular.copy(task.key)};
      //$scope.dueDate = angular.copy(task.dueDate);
      if(document.getElementById('dueDate') !== null)
      document.getElementById('dueDate').value = task.dueDate||null;
      if(angular.isDefined($scope.forms.newTaskForm.dueDate)) {
        $scope.forms.newTaskForm.dueDate.$setViewValue(task.dueDate||null);
        $scope.forms.newTaskForm.dueDate.$setPristine(true);
      }

      $scope.taskGroupLabelsGridOptions.data = angular.copy(task.label);
      $scope.currentTask = task;
      $scope.editMode = true;
    };

    //To load a new task group to the form
    $scope.loadTaskGroup = function(taskGroup) {
      $scope.taskGroupNameValue.value = '';
      $scope.taskGroupGroupLabelsGridOptions.data = [];
      $scope.modalFormLabel = localize.getLocalizedString('_NewTaskGroupModalLabel_');
      $scope.currentTaskGroup = taskGroup;
      $scope.editMode = false;
    };

    //To load an existing task group to the form
    $scope.editTaskGroup = function(taskGroup) {
      $scope.modalFormLabel = localize.getLocalizedString('_EditTaskGroupModalLabel_');
      $scope.taskGroupNameValue.value = angular.copy(taskGroup.key);
      $scope.taskGroupGroupLabelsGridOptions.data = angular.copy(taskGroup.label);

      $scope.currentTaskGroup = taskGroup;
      $scope.editMode = true;
    };

    //To add a new role
    $scope.newRole = function(scope) {
      scope = scope || [];
      if($scope.isDirtyWarning()) {
        return;
      }
      $scope.roles = scope;
        scope.push({
          name:'',
          title:[],
          statuses: [],
          checklist: []
        });
        $scope.loadRole(scope[scope.length-1]);
        scope.splice(scope.length-1,1);
    };

    //To add a new Task to a task group
    $scope.newSubTask = function(scope) {

            if(angular.isUndefined(scope.$modelValue.taskList)) {
                  scope.$modelValue.taskList = [];
            }
            var nodeData = scope.$modelValue.taskList;
            nodeData.push({
              key: null,
              label: [],
              dueDate: null
            });
            $scope.currentTaskGroup = scope.$modelValue;

            $scope.currentTask = nodeData[nodeData.length-1];

            $scope.loadTask(nodeData[nodeData.length-1]);
            nodeData.splice(nodeData.length-1,1);
      };

      //To add a new Cohort to a role
      $scope.newSubCohort = function(scope) {

        if(angular.isUndefined(scope.cohorts)) {
          scope.cohorts = [];
        }
        $scope.currentRole = scope;
        var nodeData = scope.cohorts;

              if($scope.isDirtyWarning()) {
                return;
              }
              //remove _id from checklist, new record should not have _id
              if(angular.isDefined(scope.checklist) && scope.checklist.length > 0)
              var cchecklist = $scope.removeId(scope.checklist);

              nodeData.push({
                role: scope._id,
                name: scope.name,
                startDate: null,
                disableDate: null,
                title: scope.title,
                checklist: cchecklist
              });
              $scope.loadCohort(nodeData[nodeData.length-1]);
              nodeData.splice(nodeData.length-1,1);

        };

      // remove _id's from json array
      $scope.removeId = function(array) {
        for (var i = 0; i < array.length; i++) {
          if(angular.isDefined(array[i]["_id"])) delete array[i]["_id"];
            if(angular.isDefined(array[i]["taskList"]) && array[i]["taskList"].length > 0)
              $scope.removeId(array[i]["taskList"]);
        }
        return array;
      };

    //Loads a new Task Group object into the appropriate Task Group Tree
    $scope.newTaskGroup = function(string) {
      var tree;
      if(string === 'Role') {
        if(angular.isUndefined($scope.roleGroups)) {
          $scope.roleGroups = [];
        }
        tree = $scope.roleGroups;
        $scope.currentTree = $scope.roleGroups;
      } else {
        if(angular.isUndefined($scope.cohortGroups)) {
          $scope.cohortGroups = [];
        }
        tree = $scope.cohortGroups;
        $scope.currentTree = $scope.cohortGroups;
      }
      $scope.loadTaskGroup(tree);
    };

    //Saves the values on the form to the current task displayed
    $scope.saveTask = function(form) {

      var validTask = $scope.validateTask(form);
      if (validTask) {
        var task = $scope.currentTask;
        task.key = angular.copy(form.taskName.$modelValue);
        if(angular.isDefined(form.dueDate))
        task.dueDate = angular.copy(form.dueDate.$modelValue||'');
        task.label = angular.copy($scope.taskGroupLabelsGridOptions.data);

        if(!$scope.editMode) {
          $scope.currentTaskGroup.taskList.push({"key": task.key, "dueDate": task.dueDate, "label": task.label});
          $scope.currentTask = task;
        }
        $scope.haveChilds();
        $('#taskModal').modal('hide');
      }
    };

    //Saves the values on the form to the current task group displayed
    $scope.saveTaskGroup = function(form) {

      var validTaskGroup = $scope.validateTaskGroup(form);
      if (validTaskGroup) {
        var taskGroup = $scope.currentTaskGroup;

        taskGroup.key = angular.copy($scope.taskGroupNameValue.value);
        taskGroup.label = angular.copy($scope.taskGroupGroupLabelsGridOptions.data);

        if(!$scope.editMode) {
          $scope.currentTree.push({
            key:taskGroup.key,
            label: taskGroup.label,
            taskList: taskGroup.taskList
        });
        $scope.currentTaskGroup = taskGroup;
        }
        $('#taskGroupModal').modal('hide');
      }
    };


    //Submits the cohort form if the role has valid changes that can be submitted
    $scope.submitCohort = function(form) {
      if($scope.isValidCohort(form)) {
        $rootScope.alert.hideAlert();
        $scope.cohortNameValue = {value: form.cohortName.$modelValue};
        $scope.startDate = form.startDate.$modelValue;
        $scope.disableDate = form.disableDate.$modelValue;

        var addCohortdata = {};
        var edit = false;
        var cohort = $scope.currentCohort;
        cohort.cohortName = angular.copy($scope.cohortNameValue.value);
        cohort.startDate = angular.copy($scope.startDate);
        cohort.disableDate = angular.copy($scope.disableDate);
        cohort.title = angular.copy($scope.cohortTitlesGridOptions.data);
        cohort.checklist = angular.copy($scope.cohortGroups);

        var data = {
              'role': cohort.role,
              'name': cohort.cohortName,
              'title': cohort.title,
              'startDate': cohort.startDate,
              'disableDate': cohort.disableDate
          };

          if (!angular.isUndefined(cohort.checklist) && cohort.checklist.length > 0) {
            data.checklist = cohort.checklist;
          }

          var successMsg = localize.getLocalizedString('_NewCohortCreatedSuccess_');
          var errorMsg = localize.getLocalizedString('_NewCohortCreatedError_');
          //on edit mode add _id & _rev
          if(angular.isDefined(cohort._id)) {
            edit = true;
            data._id = cohort._id;
            data._rev = cohort._rev;
            successMsg = localize.getLocalizedString('_EditCohortSuccess_');
            errorMsg = localize.getLocalizedString('_EditCohortError_');
          }

          addCohortdata.data = data;

          //Call rest service to add new cohort
          restService.setData(angular.toJson(addCohortdata));
          restService.setService('api/rest/admin/cohort/save');

          cfpLoadingBar.start();
           restService.doPost().then(function(response) {
            cfpLoadingBar.complete();
            $scope.loading = false;

            if (angular.isDefined(response.data) && response.code === 200) {

              //add new Cohort to cohorts tree otherwise update current cohort
              if(!edit) {
                $scope.currentRole.cohorts.push({
                  "_id": response.data._id,
                  "_rev": response.data._rev,
                  "role": response.data.role,
                  "name": response.data.name,
                  "title": response.data.title,
                  "startDate": response.data.startDate,
                  "disableDate": response.data.disableDate,
                  "checklist": response.data.checklist
                });
              } else {
                $scope.currentCohort._rev = response.data._rev;
                $scope.currentCohort.name = response.data.name;
                $scope.currentCohort.title = response.data.title;
                $scope.currentCohort.startDate = response.data.startDate;
                $scope.currentCohort.disableDate = response.data.disableDate;
                $scope.currentCohort.checklist = response.data.checklist;
              }

              //Collapse tree
              istreeCollapsed = true;
              $timeout(function() {
                $scope.collapseOrExpandAll('mainTree','Main');
              });
              $scope.formLabel = '';
              $rootScope.alert.showSuccessAlert(successMsg);
            }
          });

      }
    };

    //Submits the role form if the role has valid changes that can be submitted
    $scope.submitRole = function(form) {
      if($scope.isValidRole(form)) {
        $rootScope.alert.hideAlert();
        var addRoledata = {};
        var edit = false;
        var role = $scope.currentRole;
        role.roleName = angular.copy($scope.roleNameValue.value);
        role.roleTitles = angular.copy($scope.roleTitlesGridOptions.data);
        role.offerStatuses = angular.copy($scope.offerStatusesGridOptions.data);
        role.taskGroupTree = angular.copy($scope.roleGroups);
        //prepare statuses array, remove text tag
        var statuses = [];
        if(angular.isDefined(role.offerStatuses)) {
          for(var status in role.offerStatuses) {
            statuses.push(role.offerStatuses[status].text);
          }
        }

        var data = {
              'name': role.roleName,
              'title': role.roleTitles ,
              'statuses': statuses
          };

          if (!angular.isUndefined(role.taskGroupTree) && role.taskGroupTree.length > 0) {
            data.checklist =  role.taskGroupTree;
          }

          var successMsg = localize.getLocalizedString('_NewRoleCreatedSuccess_');
          var errorMsg = localize.getLocalizedString('_NewRoleCreatedError_');
          //on edit mode add _id & _rev
          if(angular.isDefined(role._id)) {
            edit = true;
            data._id = role._id;
            data._rev = role._rev;
            successMsg = localize.getLocalizedString('_EditRoleSuccess_');
            errorMsg = localize.getLocalizedString('_EditRoleError_');
          }

          addRoledata.data = data;
          //Call rest service to add new role
          restService.setData(angular.toJson(addRoledata));
          restService.setService('api/rest/admin/role/save');

          cfpLoadingBar.start();
           restService.doPost().then(function(response) {
            cfpLoadingBar.complete();
            $scope.loading = false;
            if (angular.isDefined(response.data) && response.code === 200) {

              //add new Role to roles tree otherwise update current role
              if(!edit) {
                $scope.roles.push({
                  "_id": response.data._id,
                  "_rev": response.data._rev,
                  "name": response.data.name,
                  "title": response.data.title,
                  "cohorts": [],
                  "statuses": response.data.statuses,
                  "checklist": response.data.checklist
                });
              } else {
                $scope.currentRole._rev = response.data._rev;
                $scope.currentRole.name = response.data.name;
                $scope.currentRole.title = response.data.title;
                $scope.currentRole.statuses = response.data.statuses;
                $scope.currentRole.checklist = response.data.checklist;
                $scope.currentRole.cohorts = angular.isDefined(role.cohorts)? role.cohorts:[];
              }

              //Collapse tree
              istreeCollapsed = true;
              $timeout(function() {
                $scope.collapseOrExpandAll('mainTree','Main');
              });

              $scope.formLabel = '';
              $rootScope.alert.showSuccessAlert(successMsg);
            }
          });
      }
    };

    //Check if Task Groups have child tasks so we can display Expand/Collapse button
    $scope.haveChilds = function() {

      var cr = null;
      if($scope.formLabel === 'Edit Role' || $scope.formLabel === 'New Role') {
        cr = $scope.roleGroups;
      } else {
        cr = $scope.cohortGroups;
      }

        for(var tk in cr) {
          if(angular.isDefined(cr[tk].taskList) && cr[tk].taskList.length > 0) {
              return true;
          }
        }
      return false;
    };

    //Grid Properties and Functions

    // Setting of the title grid
    $scope.roleTitlesGridOptions = {
      'enableColumnMenus': false,
      'allowCellFocus':true,
      'enableCellEditOnFocus': true,
      'gridMenuShowHideColumns':false,
      'rowTemplate': '<div><div ng-repeat="col in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ui-grid-cell></div></div>',
      'columnDefs': [
         {'name': 'text', 'enableCellEdit': true, 'displayName': 'Title' },
         {'name': 'lang', 'enableCellEdit': true, 'displayName': 'Lang.', 'editDropdownValueLabel': 'lang', 'editDropdownOptionsArray': [{'id': 'EN','lang': 'EN'}, {'id': 'FR','lang': 'FR'}], 'cellFilter': 'mapLang', 'editableCellTemplate': 'ui-grid/dropdownEditor', 'width': 70},
         {'name': '', 'field': 'checkMark', 'cellTemplate': '<button class="btn btn-primary btn-xs glyphicon glyphicon-remove removeButton" ng-click="grid.appScope.Delete(\'roleTitlesGridOptions\',row)"></button>', 'enableHiding': false, 'enableCellEdit':false, 'width': 35}
       ]
     };
     $scope.roleTitlesGridOptions.enableHorizontalScrollbar = 0;

     // Setting of the offerStatus grid
     $scope.offerStatusesGridOptions = {
       'enableColumnMenus': false,
       'allowCellFocus':true,
       'enableCellEditOnFocus': true,
       'gridMenuShowHideColumns':false,
       'columnDefs': [
         {'name': 'text', 'enableCellEdit': true,'displayName': 'Status' },
         {'name': '', 'field': 'checkMark', 'cellTemplate': '<button class="btn btn-primary btn-xs glyphicon glyphicon-remove removeButton" ng-click="grid.appScope.Delete(\'offerStatusesGridOptions\',row)"></button>', 'enableHiding': false, 'enableCellEdit':false, 'width': 35}
       ]
     };
     $scope.offerStatusesGridOptions.enableHorizontalScrollbar = 0;

     // Setting of the cohortTitle grid
     $scope.cohortTitlesGridOptions = {
       'enableColumnMenus': false,
       'allowCellFocus':true,
       'enableCellEditOnFocus': true,
       'gridMenuShowHideColumns':false,
       'columnDefs': [
         {'name': 'text', 'enableCellEdit': true, 'displayName': 'Title' },
         {'name': 'lang', 'enableCellEdit': true, 'displayName': 'Lang.', editDropdownValueLabel: 'lang', editDropdownOptionsArray: [{id: 'EN',lang: 'EN'}, {id: 'FR',lang: 'FR'}], cellFilter: 'mapLang', editableCellTemplate: 'ui-grid/dropdownEditor', 'width': 70},
         {'name': '', 'field': 'checkMark', 'cellTemplate': '<button class="btn btn-primary btn-xs glyphicon glyphicon-remove removeButton" ng-click="grid.appScope.Delete(\'cohortTitlesGridOptions\',row)"></button>', 'enableHiding': false, 'enableCellEdit':false, 'width': 35}
       ]
     };
     $scope.cohortTitlesGridOptions.enableHorizontalScrollbar = 0;

     // Setting of the taskGroupGroupLabels grid
     $scope.taskGroupGroupLabelsGridOptions = {
       'enableColumnMenus': false,
       'allowCellFocus':true,
       'enableCellEditOnFocus': true,
       'gridMenuShowHideColumns':false,
       'columnDefs': [
         {'name': 'text', 'enableCellEdit': true, 'displayName': 'Label' },
         {'name': 'lang', 'enableCellEdit': true, 'displayName': 'Lang.', editDropdownValueLabel: 'lang', editDropdownOptionsArray: [{id: 'EN',lang: 'EN'}, {id: 'FR',lang: 'FR'}], cellFilter: 'mapLang', editableCellTemplate: 'ui-grid/dropdownEditor', 'width': 70},
         {'name': '', 'field': 'checkMark', 'cellTemplate': '<button class="btn btn-primary btn-xs glyphicon glyphicon-remove removeButton" ng-click="grid.appScope.Delete(\'taskGroupGroupLabelsGridOptions\',row)"></button>', 'enableHiding': false, 'enableCellEdit':false, 'width': 35}
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
         {'name': 'text', 'enableCellEdit': true, 'displayName': 'Label' },
         {'name': 'lang', 'enableCellEdit': true, 'displayName': 'Lang.', editDropdownValueLabel: 'lang', editDropdownOptionsArray: [{id: 'EN',lang: 'EN'}, {id: 'FR',lang: 'FR'}], cellFilter: 'mapLang', editableCellTemplate: 'ui-grid/dropdownEditor', 'width': 70},
         {'name': '', 'field': 'checkMark', 'cellTemplate': '<button class="btn btn-primary btn-xs glyphicon glyphicon-remove removeButton" ng-click="grid.appScope.Delete(\'taskGroupLabelsGridOptions\',row)"></button>', 'enableHiding': false, 'enableCellEdit':false, 'width': 35}
       ]
     };
     $scope.taskGroupLabelsGridOptions.enableHorizontalScrollbar = 0;

    //To add a new row to a grid
    $scope.addRow = function(grid){
      var gridOptions;
      var gridApi;
      switch(grid){
        case 'roleTitlesGrid':
          gridOptions = $scope.roleTitlesGridOptions;
          gridApi = $scope.roleTitlesGridApi;
          break;
        case 'offerStatusesGrid':
          gridOptions = $scope.offerStatusesGridOptions;
          gridApi = $scope.offerStatusesGridApi;
          break;
        case 'cohortTitlesGrid':
          gridOptions = $scope.cohortTitlesGridOptions;
          gridApi = $scope.cohortTitlesGridApi;
          break;
        case 'taskGroupGroupLabelsGrid':
          gridOptions = $scope.taskGroupGroupLabelsGridOptions;
          gridApi = $scope.taskGroupGroupLabelsGridApi;
          break;
        case 'taskGroupLabelsGrid':
          gridOptions = $scope.taskGroupLabelsGridOptions;
          gridApi = $scope.taskGroupLabelsGridApi;
          break;
      }
      var gridLength = gridOptions.data.length;
      var temp = gridLength - 1;

        if (grid === 'offerStatusesGrid') {

          gridOptions.data.push({
            'text': ''
          });
          var temp = gridOptions.data.length-1;
          setTimeout(function() {
            gridApi.cellNav.scrollToFocus(gridOptions.data[temp], gridOptions.columnDefs[0]);
          },150);

        } else {

          if (gridLength === 0) {
            gridOptions.data = [{
              'lang': '',
              'text': ''
            }];
            var temp = gridOptions.data.length-1;
            $timeout(function(){
              gridApi.cellNav.scrollToFocus(gridOptions.data[temp], gridOptions.columnDefs[0]);
            },150);
          } else {
            if(angular.isUndefined(gridOptions.data[gridLength-1].text)) {
              gridOptions.data[gridLength-1].text = '';
            }
            if(angular.isUndefined(gridOptions.data[gridLength-1].lang)) {
              gridOptions.data[gridLength-1].lang = '';
            }

            if(gridOptions.data[gridLength-1].text.length > 0 && gridOptions.data[gridLength-1].lang.length > 0) {
              gridOptions.data.push({
                'lang': '',
                'text': ''
              });
              var temp = gridOptions.data.length-1;
              $timeout(function(){
                gridApi.cellNav.scrollToFocus(gridOptions.data[temp], gridOptions.columnDefs[0]);
              },150);
            } else {
              $window.alert(localize.getLocalizedString('_FinishFinalGridEntry_'));
            }

          }

        }

    };


    //Grid Adjustment functions
    $scope.GridAdjust = function(gridOps, id) {
      if(!angular.isDefined($scope[gridOps].data)) {
        $scope[gridOps].data = [];
      }
      if ($scope[gridOps].data.length > 5){
        angular.element(document.getElementById(id)).css('height', (((5 + 1)*30)+5) + 'px');
      }else {
        angular.element(document.getElementById(id)).css('height', ((($scope[gridOps].data.length + 1)*30)+20) + 'px');
      }
    };

    //Remove Selected Rows on grid
    $scope.Delete = function(grid, row){
      var index = $scope[grid].data.indexOf(row.entity);
      $scope[grid].data.splice(index, 1);
    };

    //Delete role or cohort from tree
    $scope.removeMe = function(me, type, index) {

      if($scope.isDirtyWarning()) {
        return;
      }

      $scope.delMe = me;
      $scope.delIndex = index;
      $scope.delRC = type;

      if(type === 'role') {
        $scope.modalFormLabel = localize.getLocalizedString('_RoleDeleteModalLabel_');
      } else {
        $scope.modalFormLabel = localize.getLocalizedString('_CohortDeleteModalLabel_');
      }
    };

    $scope.confirmedDelete = function() {
      if(angular.isUndefined($scope.delMe) || angular.isUndefined($scope.delIndex) || angular.isUndefined($scope.delRC)) {
        return;
      }
      var me = $scope.delMe;
      var type = $scope.delRC;
      var index = $scope.delIndex;

      var successMsg = '';
      var errorMsg = '';

      if(type === 'role') {
        //messages
          successMsg = localize.getLocalizedString('_RoleRemoved_');
          errorMsg = localize.getLocalizedString('_RoleRemoveError_');
      } else {
        //messages
         successMsg = localize.getLocalizedString('_CohortRemoved_');
         errorMsg = localize.getLocalizedString('_CohortRemoveError_');
      }

      //delete payload
      var removeData = {};
      var data = {
            "_id": me._id,
            "_rev": me._rev
      };

      removeData.data = data;
      //Call rest service to remove role or cohort
      restService.setData(angular.toJson(removeData));
      restService.setService('api/rest/admin/'+type+'/delete');


      cfpLoadingBar.start();
       restService.doPost().then(function(response) {
        cfpLoadingBar.complete();
        $scope.loading = false;

        if (angular.isDefined(response) && response.code === 200) {

          //Collapse tree
          istreeCollapsed = true;
          $timeout(function() {
            $scope.collapseOrExpandAll('mainTree','Main');
          });
          $rootScope.alert.showSuccessAlert(successMsg);
          //remove deleted node from tree
          if(type === 'role') {
            $scope.roles.splice(index, 1);
          } else {
            $scope.role.cohorts.splice(index, 1);
          }

        }
      });
      $scope.delMe = null;
      $scope.delIndex = null;
      $scope.delRC = null;
    };

    $scope.roleTitlesGridOptions.onRegisterApi = function(gridApi){
      //set gridApi on scope of Role Titles Grid
      $scope.roleTitlesGridApi = gridApi;
    };

    $scope.offerStatusesGridOptions.onRegisterApi = function(gridApi){
      //set gridApi on scope of Offer Statuses Grid
      $scope.offerStatusesGridApi = gridApi;
    };

    $scope.cohortTitlesGridOptions.onRegisterApi = function(gridApi){
      //set gridApi on scope of Offer Statuses Grid
      $scope.cohortTitlesGridApi = gridApi;
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
          minDate: moment(new Date().toJSON().slice(0, 10)),
          widgetPositioning: {
            horizontal: 'auto',
            vertical: 'bottom'
          }
        });
        dtp.on("dp.change", function (e) {
          if(element[0].name === 'startDate') {
            if (!e.date) {
              angular.element(document.getElementById('startDate')).parent().data("DateTimePicker").clear();
              ngModelCtrl.$setViewValue(moment(new Date().toJSON().slice(0, 10)).format("YYYY-MM-DD"));
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
              ngModelCtrl.$setViewValue(moment(new Date().toJSON().slice(0, 10)).format("YYYY-MM-DD"));
              angular.element(document.getElementById('startDate')).parent().data("DateTimePicker").maxDate(false);
              scope.$apply();
            } else {
              ngModelCtrl.$setViewValue(moment(e.date).format("YYYY-MM-DD"));
              angular.element(document.getElementById('startDate')).parent().data("DateTimePicker").maxDate(e.date);
              scope.$apply();
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
  });
