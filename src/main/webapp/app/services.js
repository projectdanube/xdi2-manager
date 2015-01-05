'use strict';

var services = angular.module('CloudDashboardApp.services', ['ngResource']);

services.factory('Profile', ['$resource',
  function ($resource) {
        return $resource('api/1.0/cloud/profile/');
  }]);

services.factory('Connection', ['$resource',
  function ($resource) {
        return $resource('api/1.0/cloud/connections/:id', {
            id: '@id'
        });
  }]);

services.factory('ConnectionTemplate', ['$resource',
  function ($resource) {
        return $resource('api/1.0/cloud/connectionTemplates/:id', {
            id: '@id'
        });
  }]);

services.factory('Card', ['$resource',
  function ($resource) {
        return $resource('api/1.0/cloud/cards/:id', {
            id: '@id'
        });
  }]);

services.factory('Key', ['$resource',
  function ($resource) {
        return $resource('api/1.0/cloud/keys/:keyType/', {
            keyType: '@keyType'
        });
  }]);


// Utils functions
function readablizeBytes(bytes) {
    var s = ['B', 'kB', 'MB', 'GB', 'TB', 'PB'];
    var e = Math.floor(Math.log(bytes) / Math.log(1024));
    return (bytes / Math.pow(1024, e)).toFixed(0) + " " + s[e];
}