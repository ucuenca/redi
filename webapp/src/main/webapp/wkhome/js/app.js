'use strict';

/* App Module */

var wkhomeApp = angular.module('wkhomeApp', [
  'ngRoute',
  'swipe',
  'snapscroll',
  /*'phonecatAnimations',*/
  /*'bootstrap',*/
  'myChart',
  'geoPlain',
  'wkhomeControllers',
  'commonDirectives',
  /*'phonecatFilters',*/
  'wkhomeServices'
]);

wkhomeApp.service('searchData', function () {
  this.authorSearch = null;
});

wkhomeApp.config(['$routeProvider',
  function($routeProvider) {
    $routeProvider.
      when('/', {
        templateUrl: 'partials/home.html',
        controller: 'indexInformation'
      }).
      when('/:section', {
        templateUrl: 'partials/home.html',
        controller: 'showSection'
      }).
      when('/w/search?:text', {
        templateUrl: 'partials/search.html',
        controller: 'ExploreController'
      }).
      when('/a/a', {
        templateUrl: 'partials/d3.html',
        controller: 'MainCtrl'
      }).
      when('/b/', {
        templateUrl: 'partials/geoplain.html',
        controller: 'worldPath'
      }).
      when('/d3/:geoId.json', {
        templateUrl: 'partials/phone-detail.html',
        controller: 'PhoneDetailCtrl'
      }).
      /*when('/phones/:phoneId', {
        templateUrl: 'partials/phone-detail.html',
        controller: 'PhoneDetailCtrl'
      }).*/
      otherwise({
        redirectTo: '/0'
      });
  }]);
