'use strict';

/* App Module */
var wkhomeApp = angular.module('wkhomeApp', [
    'pascalprecht.translate',
    'ngSanitize',
    'ngRoute',
    'ui.router',
    'swipe',
    'snapscroll',
    /*'phonecatAnimations',*/
    /*'bootstrap',*/
    // 'myChart',   //aqui
    // 'geoPlain',   //aqui
    'wkhomeControllers',
    'commonDirectives',
    /*'phonecatFilters',*/
    'wkhomeServices',
]);



wkhomeApp.service('searchData', function () {
    this.authorSearch = null;
    this.areaSearch = null;
    this.genericData = null;
    this.researchArea = "Semantic Web";
    this.globalauthor = null;
});

wkhomeApp.service('globalData', function () {
    this.language = "es";
    this.centralGraph = "http://ucuenca.edu.ec/wkhuska";
    this.clustersGraph = "http://ucuenca.edu.ec/wkhuska/clusters";
    this.authorsGraph = "http://ucuenca.edu.ec/wkhuska/authors";
    this.endpointsGraph = "http://ucuenca.edu.ec/wkhuska/endpoints";
    this.externalAuthorsGraph = "http://ucuenca.edu.ec/wkhuska/externalauthors";
    this.translateData = null;
    this.PREFIX = ' PREFIX bibo: <http://purl.org/ontology/bibo/>'
            + ' PREFIX foaf: <http://xmlns.com/foaf/0.1/>  '
            + ' PREFIX dct: <http://purl.org/dc/terms/> '
            + ' PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> '
            + ' PREFIX uc: <http://ucuenca.edu.ec/resource/>  '
            + ' PREFIX mm: <http://marmotta.apache.org/vocabulary/sparql-functions#> '
            ;
    this.CONTEXT = {
        "uc": "http://ucuenca.edu.ec/resource/",
        "foaf": "http://xmlns.com/foaf/0.1/",
        "rdfs": "http://www.w3.org/2000/01/rdf-schema#",
        "bibo": "http://purl.org/ontology/bibo/",
        "dc": "http://purl.org/dc/elements/1.1/",
        "dct": "http://purl.org/dc/terms/",
    };
});

wkhomeApp.config(['$routeProvider',
    function ($routeProvider) {
//        $stateProvider
//                .state("mystate", {
//                    url: '/mystate',
//                    views: {
//                        '': {templateUrl: '/wkhome/partials/home.html'},
//                        'menu@mystate': {templateUrl: 'mainmenu.html'}
//                    }
//                })
//                .state("groupby", {
//                    url: '/cloud/group-by',
//                    views: {
//                        '': {templateUrl: '/wkhome/partials/cloudgroup.html'}
//                      
//                    }
//                });

        $routeProvider.
                when('/:lang/', {
                    templateUrl: '/wkhome/partials/home.html',
                  
//        controller: 'indexInformation'
                }).
                when('/:lang/:section', {
                    templateUrl: '/wkhome/partials/home.html',
//        controller: 'showSection'
                }).
                when('/:lang/w/search?:text', {//when user search an author in textbox
                    templateUrl: '/wkhome/partials/search.html',
                    //      controller: 'ExploreController'
                }).
                when('/:lang/w/cloud?:text', {
                    templateUrl: '/wkhome/partials/genericcloud.html',
//        controller: 'ExploreController'
                }).
                when('/:lang/w/clusters?:text', {
                    templateUrl: '/wkhome/partials/clusterskeywcloud.html',
//        controller: 'ExploreController'
                }).
                when('/:lang/a/a', {
                    templateUrl: '/wkhome/partials/d3.html',
                    controller: 'MainCtrl'
                }).
                when('/:lang/b/', {
                    templateUrl: '/wkhome/partials/geoplain.html',
                    controller: 'worldPath'
                }).
                when('/:lang/tags/cloud', {
                    templateUrl: '/wkhome/partials/tags.html',
                    //       controller: 'getKeywordsTag'
                }).
                when('/:lang/tags/cloud/clusters', {
                    templateUrl: '/wkhome/partials/colorcluster.html',
                    //       controller: 'getKeywordsTag'
                }).
                when('/:lang/d3/:geoId.json', {
                    templateUrl: '/wkhome/partials/phone-detail.html',
                    //       controller: 'ExploreController'
                }).
                when('/:lang/cloud/group-by', {
                    templateUrl: '/wkhome/partials/cloudgroup.html',
                    //      controller: 'ExploreController'
                }).
                when('/:lang/geo-views/sources', {
                    templateUrl: '/wkhome/partials/map-sources.html',
                    //      controller: 'ExploreController'
                }).
                when('/:lang/cloud/clusters', {
                    templateUrl: '/wkhome/partials/cloudcluster.html',
                }).
                when('/:lang/cloud/keywords', {
                    templateUrl: '/wkhome/partials/kwcloudcluster.html',
                }).
                when('/:lang/info/about', {
                    templateUrl: '/wkhome/partials/about.html'
                }).
                when('/:lang/info/help', {
                    templateUrl: '/wkhome/partials/help.html'
                }).
                when('/:lang/info/contact', {
                    templateUrl: '/wkhome/partials/contact.html'
                }).
//                .
                /*when('/phones/:phoneId', {
                 templateUrl: 'partials/phone-detail.html',
                 controller: 'PhoneDetailCtrl'
                 }).*/
                otherwise({
                    redirectTo: '/es/'
                })
                ;
    }]);
