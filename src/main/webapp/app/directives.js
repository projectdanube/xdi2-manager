'use strict';

var directives = angular.module("CloudDashboardApp.directives", []);

directives.directive('cardField',
    function () {
        return {
            restrict: 'E',
            transclude: true,
            replace: true,
            scope: {
                field: '='
            },
            templateUrl: 'card-field-edit.html'
        };
    });

directives.directive('cardViewField',
    function () {
        return {
            restrict: 'E',
            transclude: true,
            replace: true,
            require: '^tooltip',
            scope: {
                field: '='
            },
            templateUrl: 'card-field-view.html'
        };
    });


directives.directive('dynamicResize', ['$window',
    function ($window) {
        return function (scope, element) {
            scope.getWinHeight = function () {
                return $window.innerHeight;
            };

            var setNavHeight = function (newHeight) {
                element.css('height', newHeight + 'px');
            };

            // Set on load
            scope.$watch(scope.getWinHeight, function (newValue, oldValue) {
                setNavHeight(scope.getWinHeight() - 100);
            }, true);

            // Set on resize
            angular.element($window).bind('resize', function () {
                scope.$apply();
            });
        };
}]);

directives.directive('stopEvent', function () {
    return {
        restrict: 'A',
        link: function (scope, element, attr) {
            element.bind('click', function (e) {
                e.stopPropagation();
            });
        }
    };
});

directives.directive('cloudCardShortcut', ['$http', '$q',
    function ($http, $q) {
        return {
            restrict: 'A',
            require: 'ngModel',
            link: function (scope, elm, attrs, ngModel) {
                ngModel.$asyncValidators.cloudCardShortcut = function (modelValue, viewValue) {
                    if (ngModel.$isEmpty(modelValue)) {
                        // consider empty models to be invalid
                        return $q.when(false);
                    }

                    // in card edit, tag doesn't change
                    if (scope.currentTag === viewValue.trim()) {
                        return $q.when(true);
                    }

                    var shortcutRegex = /^[0-9a-z.\-_]*$/i;
                    if (shortcutRegex.test(viewValue.trim())) {

                        var deferred = $q.defer();
                        $http.get('api/1.0/cloud/cards/tag/' + viewValue.trim())
                            .success(function (data) {
                                if (data.indexOf("uuid") >= 0) {
                                    deferred.reject("This shortcut is already defined.");
                                }
                                deferred.resolve();
                            })
                            .error(function (data) {
                                deferred.reject("server error");
                                deferred.resolve();
                            });

                        return deferred.promise;

                    } else {
                        return $q.when(false);
                    }
                };
            }
        };
}]);