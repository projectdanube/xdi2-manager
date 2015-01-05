'use strict';

_.mixin(_.string.exports());

var CloudDashboardApp = angular.module("CloudDashboardApp", [
    "ngRoute",
    "ngResource",
    "ui.bootstrap",
    "angularFileUpload",
    "CloudDashboardApp.controllers",
    "CloudDashboardApp.services",
    "CloudDashboardApp.directives"
]);


CloudDashboardApp.config(['$routeProvider', '$resourceProvider', '$httpProvider', '$provide',
  function ($routeProvider, $resourceProvider, $httpProvider, $provide) {
        $routeProvider.
        when('/', {
            controller: 'DashboardCtrl',
            templateUrl: 'dashboard.html'
        }).
        when('/profile', {
            controller: 'ProfileCtrl',
            templateUrl: 'profile.html'
        }).
        when('/connections', {
            controller: 'ConnectionsCtrl',
            templateUrl: 'connections.html'
        }).
        when('/cards', {
            controller: 'CardsCtrl',
            templateUrl: 'cards.html'
        }).
        when('/cards/new', {
            controller: 'CardDetailsCtrl',
            templateUrl: 'cardDetails.html'
        }).
        when('/cards/edit/:cardId', {
            controller: 'CardDetailsCtrl',
            templateUrl: 'cardDetails.html'
        }).
        when('/connectionTemplates', {
            controller: 'ConnectionTemplatesCtrl',
            templateUrl: 'connectionTemplates.html'
        }).
        when('/keys', {
            controller: 'KeysCtrl',
            templateUrl: 'keys.html'
        }).
        when('/cloudStatus', {
            controller: 'CloudStatusCtrl',
            templateUrl: 'cloudStatus.html'
        }).
        otherwise({
            redirectTo: '/'
        });

        // Don't strip trailing slashes from calculated URLs
        $resourceProvider.defaults.stripTrailingSlashes = false;

        // Handle with session expiration, forces user to login again.
        $httpProvider.interceptors.push(function ($q, $window) {
            return {
                'response': function (response) {
                    //this string should be in login.jsp
                    if (angular.isString(response.data) && response.data.indexOf('4d0bafeb-5b4c-4cbb-865e-d6211de5174e-login') != -1)
                        $window.location.reload();
                    return response;
                }
            };
        });

}]);