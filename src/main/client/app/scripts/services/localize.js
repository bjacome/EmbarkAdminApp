'use strict';

/**
 * @ngdoc factory and filter
 * @name onboardingApp.localize
 * @description
 * # localize
 * Factory and Filter in the onboardingApp for handling localization
 * based on an article from http://codingsmackdown.tv/blog/2013/04/23/localizing-your-angularjs-apps-update/
 */
angular.module('rbcembarkAdminApp')
    .factory('localize', ['$http', '$rootScope', '$window', '$filter', function ($http, $rootScope, $window, $filter) {
        var localize = {
            language:'',  // no language by default
            dictionary:[], // dictionary to hold the key/value pairs
            url: undefined, // url to the resource file to load
            resourceFileLoaded:false, // flag indicating resource file has been loaded

            initLocalizedResources:function () {
                var url =  'resources/labels.js';
                $http({ method:'GET', url:url, cache:false }).success(localize.successCallback).error(function () {

                    $http({ method:'GET', url:url, cache:false }).success(localize.successCallback);
                });
            },
            successCallback:function (data) {
                // load the dictionary with the key/value text from the resource file
                localize.dictionary = data;
                localize.resourceFileLoaded = true;
                $rootScope.$broadcast('localizeResourcesUpdated');
            },
            getLocalizedString: function(key) {
                // retrieve the localized text for a given key
                var result = '';
                try{
                    if ((localize.dictionary !== []) && (localize.dictionary.length > 0)) {
                        var entry = $filter('filter')(localize.dictionary, function(element) { return element.key === key; })[0];
                        result = entry.value;
                    }
                } catch(e){
                    console.error('"key" =>"' + key + '" is not found',e);
                }
                return result;
            }
        };

        localize.initLocalizedResources();

        return localize;
    } ])
    .filter('i18n', ['localize', function (localize) {
        return function (input) {
            return localize.getLocalizedString(input);
        };
    }])
    .directive('i18n', ['localize', function(localize){
        var i18nDirective = {
            restrict: 'EAC',
            updateText:function(elm, token){
                var values = token.split('|');
                if (values.length >= 1) {
                    var tag = localize.getLocalizedString(values[0]);
                    if ((tag !== null) && (tag !== undefined) && (tag !== '')) {
                        if (values.length > 1) {
                            for (var index = 1; index < values.length; index++) {
                                var target = '{' + (index - 1) + '}';
                                tag = tag.replace(target, values[index]);
                            }
                        }
                        elm.text(tag);
                    }
                }
            },
            link:function (scope, elm, attrs) {
                // trigger text update on update of broadcast
                scope.$on('localizeResourcesUpdated', function() {
                    i18nDirective.updateText(elm, attrs.i18n);
                });
                attrs.$observe('i18n', function () {
                    i18nDirective.updateText(elm, attrs.i18n);
                });
            }
        };

        return i18nDirective;
    }])
    .directive('i18nAttr', ['localize', function (localize) {
        var i18nAttrDirective = {
            restrict: 'EAC',
            updateText:function(elm, token){
                var values = token.split('|');
                var tag = localize.getLocalizedString(values[0]);
                if ((tag !== null) && (tag !== undefined) && (tag !== '')) {
                    if (values.length > 2) {
                        for (var index = 2; index < values.length; index++) {
                            var target = '{' + (index - 2) + '}';
                            tag = tag.replace(target, values[index]);
                        }
                    }
                    elm.attr(values[1], tag);
                }
            },
            link: function (scope, elm, attrs) {
                // trigger text update on update of broadcast
                scope.$on('localizeResourcesUpdated', function() {
                    i18nAttrDirective.updateText(elm, attrs.i18nAttr);
                });
                attrs.$observe('i18nAttr', function (value) {
                    i18nAttrDirective.updateText(elm, value);
                });
            }
        };
        return i18nAttrDirective;
    }])
    .run(['localize',  function (localize) {
        localize.initLocalizedResources();
    }]);
