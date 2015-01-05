'use strict';

controllers.controller("CardsCtrl", ['$scope', 'Card', '$modal', '$anchorScroll', '$http',
  function ($scope, Card, $modal, $anchorScroll, $http) {

        $scope.success = $scope.$parent.getAlert('success');

        $scope.refreshCards = function () {
            $scope.resetAlerts();

            $scope.inProgress = true;
            Card.query(function (data) {
                $scope.cards = data;
                $scope.inProgress = false;
            }, function (error) {
                $scope.error = "Something went wrong while getting your cards, please try again.";
                $scope.inProgress = false;
            });

        };

        $scope.delete = function (id) {

            var modalInstance = $modal.open({
                templateUrl: 'modal.html',
                controller: 'GenericModalCtrl',
                resolve: {
                    labels: function () {
                        return {
                            modalTitle: "Do you really want to delete this card?",
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
                Card.delete({
                    id: id
                }, function (data) {
                    $scope.success = "Card removed successfully";
                    $scope.refreshCards();
                }, function (error) {
                    $scope.error = "Something went wrong while deleting the card, please try again.";
                });
            }, function () { /*cancel*/ });


        };

        $scope.viewCard = function (card) {

            var cardViewer = $modal.open({
                templateUrl: 'viewCard.html',
                controller: 'ViewCardCtrl',
                size: 'lg',
                resolve: {
                    params: function () {
                        return {
                            card: card,
                            cardFieldLabels: $scope.$parent.cardFieldLabels
                        };
                    }
                }
            });

        };

        $scope.defaultCard = function (id) {

            $scope.inProgress = true;
            $http.post('api/1.0/cloud/cards/' + id + '/default/')
                .success(function (data) {
                    $scope.refreshCards();
                })
                .error(function (data) {
                    $scope.error = "Something went wrong while setting the default card, please try again.";
                    $scope.inProgress = false;
                });

        };

        $scope.resetAlerts = function () {
            $scope.error = false;
        };


        $scope.refreshCards();

  }]);

controllers.controller("CardDetailsCtrl", ['$scope', 'Card', 'Profile', '$routeParams', '$location', '$anchorScroll', '$upload',
  function ($scope, Card, Profile, $routeParams, $location, $anchorScroll, $upload) {

        $scope.cardFieldLabels = $scope.$parent.cardFieldLabels;

        Profile.get(function (data) {
            $scope.profile = data;
        }, function (error) {
            $scope.error = "Error comunicating with the server, please try again.";
        });

        if ($routeParams.cardId) {
            // Card edit
            Card.get({
                id: $routeParams.cardId,
                edit: true
            }, function (data) {

                $scope.availableFields = _.keys($scope.cardFieldLabels);
                $scope.usedFields = [];

                _.forOwn(data.fields, function (field, key) {
                    field.privacy = _.capitalize(field.privacy.toLowerCase());

                    if (field.privacy === "Public" || field.privacy === "Private") {
                        $scope.addField(key);
                    }

                });

                $scope.card = data;
                $scope.currentTag = $scope.card.tag;
            }, function (error) {
                $scope.error = "Error comunicating with the server, please try again.";
            });
        } else {
            // New card
            var fields = {};
            _.keys($scope.cardFieldLabels).forEach(function (e) {
                fields[e] = {
                    "value": null,
                    "privacy": "Only_me"
                };
            });

            $scope.availableFields = _.keys($scope.cardFieldLabels);
            $scope.usedFields = [];

            $scope.card = {
                "tag": null,
                "description": null,
                "fields": fields
            };
        }

        $scope.addField = function (field) {
            var element = _.remove($scope.availableFields, function (e) {
                return e === field;
            });

            if (_.isUndefined($scope.card) === false && _.isUndefined($scope.card.fields[field]) === false) {
                $scope.card.fields[field].privacy = "Private";
            }
            $scope.usedFields = _.union($scope.usedFields, element);
        };

        $scope.removeField = function (field) {
            var element = _.remove($scope.usedFields, function (e) {
                return e === field;
            });
            if (_.isUndefined($scope.card) === false && _.isUndefined($scope.card.fields[field]) === false) {
                $scope.card.fields[field].privacy = "Only_me";
            }
            $scope.availableFields = _.union($scope.availableFields, element);

        };

        $scope.showSection = function (section) {
            if (_.isUndefined($scope.card)) return false;

            var fields = $scope.card.fields;
            if (section === 'personalDetails') {
                return fields.firstName.privacy !== 'Only_me' ||
                    fields.lastName.privacy !== 'Only_me' ||
                    fields.nickname.privacy !== 'Only_me' ||
                    fields.gender.privacy !== 'Only_me' ||
                    fields.birthDate.privacy !== 'Only_me' ||
                    fields.nationality.privacy !== 'Only_me';
            } else if (section === 'contactInfo') {
                return fields.phone.privacy !== 'Only_me' ||
                    fields.mobilePhone.privacy !== 'Only_me' ||
                    fields.workPhone.privacy !== 'Only_me' ||
                    fields.email.privacy !== 'Only_me' ||
                    fields.website.privacy !== 'Only_me';
            } else if (section === 'address') {
                return fields.address_street.privacy !== 'Only_me' ||
                    fields.address_postalCode.privacy !== 'Only_me' ||
                    fields.address_locality.privacy !== 'Only_me' ||
                    fields.address_region.privacy !== 'Only_me' ||
                    fields.address_country.privacy !== 'Only_me';
            }

        };

        $scope.onFileSelect = function ($files) {
            $scope.file = $files[0];
        };

        var prepareFieldsForView = function () {
            _.forOwn($scope.card.fields, function (field, key) {
                field.privacy = _.capitalize(field.privacy.toLowerCase());
            });
        };
        var prepareFieldsForSubmit = function () {
            _.forOwn($scope.card.fields, function (field, key) {
                field.privacy = field.privacy.toUpperCase();
            });
        };


        $scope.submit = function () {
            if (!angular.isDefined($scope.card)) return;

            prepareFieldsForSubmit();

            $scope.inProgress = true;

            $anchorScroll();
            if ($scope.card.id) {
                // Card edit


                $scope.upload = $upload.upload({
                    url: 'api/1.0/cloud/cards/' + $scope.card.id,
                    method: 'POST',
                    headers: {
                        'header-key': 'header-value'
                    },
                    data: {
                        card: $scope.card
                    },
                    file: $scope.file
                }).progress(function (evt) {
                    //console.log('percent: ' + parseInt(100.0 * evt.loaded / evt.total));
                }).success(function (data, status, headers, config) {
                    $scope.$parent.success = "Card successfully saved.";
                    $location.path("/cards");
                }).error(function (data, status, headers, config) {
                    prepareFieldsForView();
                    if (status === 500) $scope.error = "It was not possible to save this card. Please make sure the Background image does not exceed 512KB.";
                    else $scope.error = data;
                    $scope.inProgress = false;
                });

            } else {
                // New Card


                $scope.upload = $upload.upload({
                    url: 'api/1.0/cloud/cards/',
                    method: 'POST',
                    headers: {
                        'header-key': 'header-value'
                    },
                    data: {
                        card: $scope.card
                    },
                    file: $scope.file
                }).progress(function (evt) {
                    //console.log('percent: ' + parseInt(100.0 * evt.loaded / evt.total));
                }).success(function (data, status, headers, config) {
                    $scope.$parent.success = "Card successfully saved.";
                    $location.path("/cards");
                }).error(function (data, status, headers, config) {
                    prepareFieldsForView();
                    if (status === 500) $scope.error = "It was not possible to save this card. Please make sure the Background image does not exceed 512KB.";
                    else $scope.error = data;
                    $scope.inProgress = false;
                });

            }
        };

        $scope.getProfileField = function (f) {
            var fields = f.split('_');

            if ($scope.profile) {

                if (fields.length == 1) {
                    return $scope.profile[fields[0]];
                }
                if (fields.length == 2) {
                    return $scope.profile[fields[0]][fields[1]];
                }
            }
        };

  }]);

controllers.controller("ViewCardCtrl", ['$scope', '$modalInstance', 'params', '$rootScope',
  function ($scope, $modalInstance, params, $rootScope) {

        $scope.card = params.card;
        $scope.cardFieldLabels = params.cardFieldLabels;


        $scope.url = $rootScope.sessionProperties.cloudCardAppUrl + $rootScope.sessionProperties.environment + '/' + $rootScope.sessionProperties.cloudName + '%23' + $scope.card.tag;

        $scope.close = function () {
            $modalInstance.dismiss('cancel');
        };

  }]);