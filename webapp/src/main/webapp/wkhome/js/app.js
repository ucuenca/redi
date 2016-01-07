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
  //  this.allKeywords = null;
    this.researchArea = "Semantic Web";
    
});



wkhomeApp.config(['$routeProvider',
    function ($routeProvider) {
        $routeProvider.
                when('/', {
                    templateUrl: '/wkhome/partials/home.html',
//        controller: 'indexInformation'
                }).
                when('/:section', {
                    templateUrl: '/wkhome/partials/home.html',
//        controller: 'showSection'
                }).
                when('/w/search?:text', {//when user search an author in textbox
                    templateUrl: '/wkhome/partials/search.html',
                    //      controller: 'ExploreController'
                }).
                when('/w/cloud?:text', {
                    templateUrl: '/wkhome/partials/genericcloud.html',
//        controller: 'ExploreController'
                }).
                when('/a/a', {
                    templateUrl: '/wkhome/partials/d3.html',
                    controller: 'MainCtrl'
                }).
                when('/b/', {
                    templateUrl: '/wkhome/partials/geoplain.html',
                    controller: 'worldPath'
                }).
                when('/tags/cloud', {
                    templateUrl: '/wkhome/partials/tags.html',
                    //       controller: 'getKeywordsTag'
                }).
                when('/d3/:geoId.json', {
                    templateUrl: '/wkhome/partials/phone-detail.html',
                    //       controller: 'ExploreController'
                }).
                when('/cloud/group-by', {
                    templateUrl: '/wkhome/partials/cloudgroup.html',
                    //      controller: 'ExploreController'
                }).
                when('/geo-views/sources', {
                    templateUrl: '/wkhome/partials/map-sources.html',
                    //      controller: 'ExploreController'
                }).
                when('/info/about', {
                    templateUrl: '/wkhome/partials/about.html'
                }).
                when('/info/help', {
                    templateUrl: '/wkhome/partials/help.html'
                }).
                when('/info/contact', {
                    templateUrl: '/wkhome/partials/contact.html'
                }).
                /*when('/phones/:phoneId', {
                 templateUrl: 'partials/phone-detail.html',
                 controller: 'PhoneDetailCtrl'
                 }).*/
                otherwise({
                    redirectTo: '/0'
                });
    }]);
