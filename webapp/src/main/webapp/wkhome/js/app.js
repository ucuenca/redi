'use strict';

/* App Module */

var wkhomeApp = angular.module('wkhomeApp', [
  'ngRoute',
  'swipe',
  'snapscroll',
  /*'phonecatAnimations',*/
  /*'bootstrap',*/
 // 'myChart',   //aqui
 // 'geoPlain',   //aqui
  'wkhomeControllers',
  'commonDirectives',
  /*'phonecatFilters',*/
  'wkhomeServices'
]);

wkhomeApp.service('searchData', function () {
  this.authorSearch = null;
  this.areaSearch = null;
  this.genericData = null;
  this.researchArea = "Semantic Web";
});



wkhomeApp.config(['$routeProvider',
  function($routeProvider) {
    $routeProvider.
      when('/', {
        templateUrl: 'partials/home.html',
//        controller: 'indexInformation'
      }).
      when('/:section', {
        templateUrl: 'partials/home.html',
//        controller: 'showSection'
      }).
      when('/w/search?:text', {   //when user search an author in textbox
        templateUrl: 'partials/search.html',
  //      controller: 'ExploreController'
      }).
               
      when('/w/cloud?:text', {
        templateUrl: 'partials/genericcloud.html',
//        controller: 'ExploreController'
      }).
      
      when('/a/a', {
        templateUrl: 'partials/d3.html',
        controller: 'MainCtrl'
      }).
      when('/b/', {
        templateUrl: 'partials/geoplain.html',
        controller: 'worldPath'
      }).
      when('/tags/cloud', {
        templateUrl: 'partials/tags.html',
 //       controller: 'getKeywordsTag'
      }).
      when('/d3/:geoId.json', {
        templateUrl: 'partials/phone-detail.html',
 //       controller: 'ExploreController'
      }).
      when('/cloud/group-by', {
        templateUrl: 'partials/cloudgroup.html',
  //      controller: 'ExploreController'
      }).
      when('/geo-views/sources', {
        templateUrl: 'partials/map-sources.html',
  //      controller: 'ExploreController'
      }).
      /*when('/phones/:phoneId', {
        templateUrl: 'partials/phone-detail.html',
        controller: 'PhoneDetailCtrl'
      }).*/
      otherwise({
        redirectTo: '/0'
      });
  }]);
