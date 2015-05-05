'use strict';

/* Controllers */

var wkhomeControllers = angular.module('wkhomeControllers', []);

wkhomeControllers.controller('indexInformation', ['$scope', 'Phone',
  function($scope, Phone) {
    $scope.welcome = "Hello World!";
    $scope.user = {};
    /*$scope.phones = Phone.query();
    $scope.orderProp = 'age';*/
  }]);

/* Sample of a oontroller that manages requests with URL params */
wkhomeControllers.controller('PhoneDetailCtrl', ['$scope', '$routeParams', 'Phone',
  function($scope, $routeParams, Phone) {
    
    $scope.phone = Phone.get({phoneId: $routeParams.phoneId}, function(phone) {
      $scope.mainImageUrl = phone.images[0];
    });

    $scope.setImage = function(imageUrl) {
      $scope.mainImageUrl = imageUrl;
    }
  }]);
