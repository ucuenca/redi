'use strict';

/* App Module */

var wkhomeApp = angular.module('wkhomeApp', [
  'ngRoute',
  /*'phonecatAnimations',*/

  'wkhomeControllers',
  /*'phonecatFilters',*/
  'wkhomeServices'
]);

wkhomeApp.config(['$routeProvider',
  function($routeProvider) {
    $routeProvider.
      when('/', {
        templateUrl: 'partials/home.html',
        controller: 'indexInformation'
      }).
      /*when('/phones/:phoneId', {
        templateUrl: 'partials/phone-detail.html',
        controller: 'PhoneDetailCtrl'
      }).*/
      otherwise({
        redirectTo: '/'
      });
  }]);
