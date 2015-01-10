'use strict';

var controllers = angular.module("CloudDashboardApp.controllers", []);

controllers.controller("DefaultCtrl", ['$scope', '$http', '$rootScope',
  function ($scope, $http, $rootScope) {
        $scope.isDefined = function (value) {
            return angular.isDefined(value);
        };

        $scope.getAlert = function (value) {
            if ($scope[value]) {
                var tmp = $scope[value];
                $scope[value] = null;
                return tmp;
            }
            return null;
        };

        $scope.cardFieldLabels = {
            "firstName": "First Name",
            "lastName": "Last Name",
            "nickname": "Nickname",
            "gender": "Gender",
            "birthDate": "Birth Date",
            "nationality": "Nationality",
            "phone": "Phone",
            "mobilePhone": "Mobile Phone",
            "workPhone": "Work Phone",
            "email": "Email",
            "website": "Website",
            "address_street": "Street",
            "address_postalCode": "Postal Code",
            "address_locality": "Locality",
            "address_region": "Region",
            "address_country": "Country"
        };


        $rootScope.sessionProperties = {
            "cloudName": "",
            "cloudNumber": "",
            "xdiEndpointUrl": "",
            "environment": "",
            "cloudCardAppUrl": ""
        };
        $http.get('api/1.0/session/properties/')
            .success(function (data) {
                $rootScope.sessionProperties = data;
            })
            .error(function (data) {
                $rootScope.sessionProperties = {};
            });

  }]);

controllers.controller("DashboardCtrl", ['$scope', '$http',
  function ($scope, $http) {

        $http.get('api/1.0/cloud/connections/count/')
            .success(function (data) {
                $scope.connectionsCount = data;
            })
            .error(function (data) {
                $scope.connectionsCount = "N/A";
            });
        $http.get('api/1.0/cloud/cards/count/')
            .success(function (data) {
                $scope.cardsCount = data;
            })
            .error(function (data) {
                $scope.cardsCount = "N/A";
            });
        $http.get('api/1.0/cloud/stats/cloudNamesCount')
            .success(function (data) {
                $scope.cloudNamesCount = data;
            })
            .error(function (data) {
                $scope.cloudNamesCount = "N/A";
            });
        $http.get('api/1.0/cloud/stats/dependentsCount')
            .success(function (data) {
                $scope.dependentsCount = data;
            })
            .error(function (data) {
                $scope.dependentsCount = "N/A";
            });
  }]);

controllers.controller("ProfileCtrl", ['$scope', 'Profile', '$anchorScroll',
  function ($scope, Profile, $anchorScroll) {
        $scope.profileSection = 0;

        $scope.inProgress = "Loading...";
        $scope.profile = Profile.get(function (data) {
            $scope.inProgress = false;
        }, function (error) {
            $scope.error = "Error comunicating with the server, please try again.";
        });

        $scope.submit = function () {
            $scope.resetAlerts();
            $anchorScroll();

            $scope.inProgress = "Saving your Personal Profile";
            $scope.profile = Profile.save($scope.profile, function (data) {
                $scope.inProgress = false;
                $scope.success = "Your profile was updated successfully.";
            }, function (error) {
                $scope.error = "Error comunicating with the server, please try again.";
            });
        };

        $scope.resetAlerts = function () {
            $scope.success = false;
            $scope.error = false;
            $scope.newConnectionError = false;
        };
  }]);


controllers.controller("ConnectionsCtrl", ['$scope', 'Connection', '$modal', '$anchorScroll', '$http', '$timeout',

  function ($scope, Connection, $modal, $anchorScroll, $http, $timeout) {

        $scope.prepareNewConnection = function () {
            $scope.newConnection = {
                "raCloudName": null,
                "raCloudNumber": null,
                "requireSignature": false,
                "requireSecretToken": false,
                "permissions": [
                    []
                ]
            };
        };

        $scope.addPermission = function () {
            $scope.newConnection.permissions.push([]);
        };

        $scope.$watch('newConnection.raCloudName', function (newValue, oldValue) {
            if (newValue === oldValue) return;
            if (!angular.isDefined($scope.newConnection) || !$scope.newConnection.raCloudName) return;

            $timeout.cancel($scope.cloudNameVerifyTimeout);

            $scope.cloudNameVerifyTimeout = $timeout(function () {
                $scope.cloudNameError = false;
                $scope.cloudNameOk = false;
                $scope.newConnection.raCloudNumber = "";

                $http.get('api/1.0/discovery/' + $scope.newConnection.raCloudName)
                    .success(function (data) {
                        if (data.indexOf("uuid") < 0) {
                            $scope.cloudNameError = "This Cloud Name doesn't exist.";
                        } else {
                            $scope.newConnection.raCloudNumber = data;
                            $scope.cloudNameOk = true;
                        }
                    })
                    .error(function (data) {
                        $scope.cloudNameError = "Oops... please try again.";
                    });
            }, 250);
        });

        $scope.createConnection = function () {
            if (!angular.isDefined($scope.newConnection)) return;

            // permissions convertion to meet with server requirements 
            var permissions = {};
            var p = $scope.newConnection.permissions;
            for (var i = 0; i < p.length; i++) {
                var key = p[i].shift();
                permissions[key] = p[i].map(function (e) {
                    if (angular.isString(e)) {
                        return e;
                    }
                });
            }

            var conn = {
                "xdi": null,
                "raCloudName": $scope.newConnection.raCloudName,
                "raCloudNumber": $scope.newConnection.raCloudNumber,
                "type": "GENERIC",
                "requireSignature": $scope.newConnection.requireSignature,
                "requireSecretToken": $scope.newConnection.requireSecretToken,
                "permissions": permissions
            };

            $scope.resetAlerts();
            Connection.save(conn, function (data) {
                $scope.refreshConnections();
            }, function (data) {
                $scope.newConnectionError = data.data;
            });

        };

        $scope.delete = function (id) {

            var modalInstance = $modal.open({
                templateUrl: 'modal.html',
                controller: 'GenericModalCtrl',
                resolve: {
                    labels: function () {
                        return {
                            modalTitle: "Do you really want to delete this connection?",
                            modalBody: null,
                            modalOk: "Delete",
                            modalCancel: "Cancel"
                        };
                    }
                }
            });

            modalInstance.result.then(function (okValue) {
                //user clicked OK
                $scope.resetAlerts();
                $anchorScroll();
                Connection.delete({
                    id: id
                }, function (data) {
                    $scope.success = "Connection removed successfully";
                    $scope.refreshConnections();
                }, function (error) {
                    $scope.error = "Something went wrong while deleting the connection, please try again.";
                });
            }, function () { /*cancel*/ });


        };

        $scope.refreshConnections = function () {
            $scope.resetAlerts();
            $scope.newConnection = null;
            $scope.inProgress = true;
            $scope.connections = Connection.query(function (data) {
                $scope.connections = data;
                $scope.inProgress = false;
            }, function (error) {
                $scope.error = "Something went wrong while getting your connections, please try again.";
                $scope.inProgress = false;
            });

        };

        $scope.resetAlerts = function () {
            $scope.success = false;
            $scope.error = false;
            $scope.cloudNameOk = false;
        };


        $scope.refreshConnections();

  }]);



controllers.controller("ConnectionTemplatesCtrl", ['$scope', 'ConnectionTemplate', '$modal', '$anchorScroll', '$http', '$timeout',

  function ($scope, ConnectionTemplate, $modal, $anchorScroll, $http, $timeout) {

        $scope.prepareNewConnection = function () {
            $scope.newConnection = {
                "tag": null,
                "description": null,
                "requireSignature": false,
                "requireSecretToken": false,
                "permissions": [
                    []
                ]
            };
        };

        $scope.addPermission = function () {
            $scope.newConnection.permissions.push([]);
        };

        $scope.createConnection = function () {
            if (!angular.isDefined($scope.newConnection)) return;

            // permissions convertion to meet with server requirements 
            var permissions = {};
            var p = $scope.newConnection.permissions;
            for (var i = 0; i < p.length; i++) {
                var key = p[i].shift();
                permissions[key] = p[i].map(function (e) {
                    if (angular.isString(e)) {
                        return e;
                    }
                });
            }

            var conn = {
                "xdi": null,
                "tag": $scope.newConnection.tag,
                "description": $scope.newConnection.description,
                "requireSignature": $scope.newConnection.requireSignature,
                "requireSecretToken": $scope.newConnection.requireSecretToken,
                "permissions": permissions
            };

            $scope.resetAlerts();
            ConnectionTemplate.save(conn, function (data) {
                $scope.refreshConnections();
            }, function (data) {
                $scope.newConnectionError = data.data;
            });

        };

        $scope.delete = function (id) {

            var modalInstance = $modal.open({
                templateUrl: 'modal.html',
                controller: 'GenericModalCtrl',
                resolve: {
                    labels: function () {
                        return {
                            modalTitle: "Do you really want to delete this connection template?",
                            modalBody: null,
                            modalOk: "Delete",
                            modalCancel: "Cancel"
                        };
                    }
                }
            });

            modalInstance.result.then(function (okValue) {
                //user clicked OK
                $scope.resetAlerts();
                $anchorScroll();
                ConnectionTemplate.delete({
                    id: id
                }, function (data) {
                    $scope.success = "Connection template removed successfully";
                    $scope.refreshConnections();
                }, function (error) {
                    $scope.error = "Something went wrong while deleting the connection template, please try again.";
                });
            }, function () { /*cancel*/ });


        };

        $scope.refreshConnections = function () {
            $scope.resetAlerts();
            $scope.newConnection = null;
            $scope.inProgress = true;
            $scope.connections = ConnectionTemplate.query(function (data) {
                $scope.connections = data;
                $scope.inProgress = false;
            }, function (error) {
                $scope.error = "Something went wrong while getting your connection templates, please try again.";
            });

        };

        $scope.resetAlerts = function () {
            $scope.success = false;
            $scope.error = false;
        };


        $scope.refreshConnections();

  }]);

controllers.controller("CloudStatusCtrl", ['$scope', '$http',
  function ($scope, $http) {

        $scope.cloudStatus = {};

        $http.get('api/1.0/cloud/status/')
            .success(function (data) {
                $scope.cloudStatus.cloud = {
                    'success': data
                };
            })
            .error(function (data, status, headers, config) {
                $scope.cloudStatus.cloud = {
                    'error': data
                };
            });

        $http.get('api/1.0/discovery/status/')
            .success(function (data) {
                $scope.cloudStatus.discovery = {
                    'success': data
                };
            })
            .error(function (data, status, headers, config) {
                $scope.cloudStatus.discovery = {
                    'error': data
                };
            });

  }]);


controllers.controller("KeysCtrl", ['$scope', 'Key', '$anchorScroll', '$modal',
  function ($scope, Key, $anchorScroll, $modal) {

        $scope.refreshKeys = function () {
            Key.query({
                keyType: 'sig'
            }, function (data) {
                $scope.signatureKeys = data;
            }, function (error) {
                $scope.error = "Error comunicating with the server, please try again.";
            });

            Key.query({
                keyType: 'encrypt'
            }, function (data) {
                $scope.encryptKeys = data;
            }, function (error) {
                $scope.error = "Error comunicating with the server, please try again.";
            });
        };

        $scope.generateKeys = function (type) {

            var modalInstance = $modal.open({
                templateUrl: 'modal.html',
                controller: 'GenericModalCtrl',
                resolve: {
                    labels: function () {
                        return {
                            modalTitle: "Do you really want to generate a new key pair?",
                            modalBody: null,
                            modalOk: "Generate",
                            modalCancel: "Cancel"
                        };
                    }
                }
            });

            modalInstance.result.then(function (okValue) {
                //user clicked OK
                Key.save({
                    keyType: type
                }, function (data) {
                    $scope.refreshKeys();
                }, function (error) {
                    $scope.error = "Error comunicating with the server, please try again.";
                });
            }, function () { /*cancel*/ });
        };

        $scope.delete = function (type) {

            var modalInstance = $modal.open({
                templateUrl: 'modal.html',
                controller: 'GenericModalCtrl',
                resolve: {
                    labels: function () {
                        return {
                            modalTitle: "Do you really want to delete this key pair?",
                            modalBody: null,
                            modalOk: "Delete",
                            modalCancel: "Cancel"
                        };
                    }
                }
            });

            modalInstance.result.then(function (okValue) {
                //user clicked OK
                Key.delete({
                    keyType: type
                }, function (data) {
                    $scope.refreshKeys();
                }, function (error) {
                    $scope.error = "Error comunicating with the server, please try again.";
                });
            }, function () { /*cancel*/ });

        };

        $scope.resetAlerts = function () {
            $scope.error = false;
        };

        $scope.refreshKeys();


  }]);

controllers.controller("FacebookCtrl", ['$scope', '$http', '$modal', '$sce',
  function ($scope, $http, $modal, $sce) {

        var refresh = function () {
            $scope.inProgress = true;
            $http.get('api/1.0/cloud/facebook/')
                .success(function (data) {
                    $scope.facebookConnector = data;
                    $scope.facebookConnector.secureOAuthUrl = $sce.trustAsResourceUrl($scope.facebookConnector.oAuthUrl + '&display=popup');
                    $scope.inProgress = false;
                })
                .error(function (data) {
                    $scope.facebookConnector = {};
                    $scope.inProgress = false;
                });
        };

        $scope.revoke = function () {
            $scope.inProgress = true;
            $http.delete('api/1.0/cloud/facebook/')
                .success(function (data) {
                    $scope.success = "Facebook connection deleted";
                    refresh();
                })
                .error(function (data) {
                    $scope.error = "Error deleting facebook connection";
                    $scope.inProgress = false;
                });

        };


        $scope.connect = function () {

            $scope.success = null;
            $scope.error = null;

            var winWidth = 520;
            var winHeight = 350;
            var winTop = (screen.height / 2) - (winHeight / 2);
            var winLeft = (screen.width / 2) - (winWidth / 2);
            window.open($scope.facebookConnector.secureOAuthUrl, 'fb', 'top=' + winTop + ',left=' + winLeft + ',toolbar=0,status=0,width=' + winWidth + ',height=' + winHeight);

            var wait = function () {

                if (_.isNull($scope.facebookConnector.userId) === false) {
                    clearInterval(waitInterval);
                }
                refresh();

            };

            var waitInterval = setInterval(wait, 3000);



        };


        refresh();




  }]);


controllers.controller("GenericModalCtrl", ['$scope', '$modalInstance', 'labels',
  function ($scope, $modalInstance, labels) {

        $scope.labels = labels;

        $scope.ok = function () {
            $modalInstance.close('OK');
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };


  }]);