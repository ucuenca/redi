'use strict';

/* App Module */
var wkhomeApp = angular.module('wkhomeApp', [
    'pascalprecht.translate',
    'ngSanitize',
    'ngRoute',
    'ui.router',
    'swipe',
    'snapscroll',
    'wkhomeControllers',
    //'commonDirectives',
    'wkhomeServices',
    'ngAnimate',
    'ngMaterial',
]);



wkhomeApp.service('searchData', function () {
    this.authorSearch = null;
    this.areaSearch = null;
    this.genericData = null;
    this.researchArea = "Semantic Web";
    this.selectedTagItem = "Semantic Web";
    this.globalauthor = null;
    this.clustersAuthors = null;
    this.dataRequested = false;
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
            + ' PREFIX uc: <http://ucuenca.edu.ec/ontology#>  '
            + ' PREFIX mm: <http://marmotta.apache.org/vocabulary/sparql-functions#> '
            ;
    this.CONTEXT = {
        "uc": "http://ucuenca.edu.ec/ontology#",
        "foaf": "http://xmlns.com/foaf/0.1/",
        "rdfs": "http://www.w3.org/2000/01/rdf-schema#",
        "bibo": "http://purl.org/ontology/bibo/",
        "dc": "http://purl.org/dc/elements/1.1/",
        "dct": "http://purl.org/dc/terms/",
    };

    this.urltofindinGOOGLE = 'https://scholar.google.com/scholar?q={0}';
    this.urltofindinDBLP = 'http://dblp.uni-trier.de/search?q={0}';
    this.urltofindinSCOPUS = 'http://www.scopus.com/results/results.uri?numberOfFields=0&src=s&clickedLink=&edit=&editSaveSearch=&origin=searchbasic&authorTab=&affiliationTab=&advancedTab=&scint=1&menu=search&tablin=&searchterm1={0}&field1=TITLE&dateType=Publication_Date_Type&yearFrom=Before+1960&yearTo=Present&loadDate=7&documenttype=All&subjects=LFSC&_subjects=on&subjects=HLSC&_subjects=on&subjects=PHSC&_subjects=on&subjects=SOSC&_subjects=on&st1={1}&st2=&sot=b&sdt=b&sl=91&s=TITLE%28{2}%29'
});

wkhomeApp.config(['$routeProvider',
    function ($routeProvider) {

        $routeProvider.
                when('/:lang/', {
                    templateUrl: '/wkhome/partials/home.html',
                }).
                when('/:lang/:section', {
                    templateUrl: '/wkhome/partials/home.html',
                }).
                when('/:lang/w/search?:text', {//when user search an author in textbox
                    templateUrl: '/wkhome/partials/search.html',
                    //      controller: 'ExploreController'
                }).
                when('/:lang/w/author/:text', {//when user search an author in textbox
                    templateUrl: '/wkhome/partials/search.html',
                }).
                when('/:lang/w/cloud?:text', {
                    templateUrl: '/wkhome/partials/genericPageCloud.html',
                }).
                when('/:lang/w/clusters?:text', {
                    templateUrl: '/wkhome/partials/clustersCloud.html',
                }).
                when('/:lang/data/datacube', {
                    templateUrl: '/wkhome/partials/dataCube.html',
                }).
                when('/:lang/data/statistics', {
                    templateUrl: '/wkhome/partials/statistics.html',
                }).
                when('/:lang/b/', {
                    templateUrl: '/wkhome/partials/geoplain.html',
                    controller: 'worldPath'
                }).
                when('/:lang/tags/cloud', {
                    templateUrl: '/wkhome/partials/keywordsCloud.html',
                }).
                when('/:lang/d3/:geoId.json', {
                    templateUrl: '/wkhome/partials/phone-detail.html',
                }).
                when('/:lang/cloud/group-by', {
                    templateUrl: '/wkhome/partials/groupbyCloud.html',
                }).
                when('/:lang/geo-views/sources', {
                    templateUrl: '/wkhome/partials/map.html',
                }).
                when('/:lang/cloud/clusters', {
                    templateUrl: '/wkhome/partials/clusterGroupByCloud.html',
                }).
                when('/:lang/cloud/keywords', {
                    templateUrl: '/wkhome/partials/clusterKeywordsCloud.html',
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
                when('/:lang/info/sources', {
                    templateUrl: '/wkhome/partials/sources.html'
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

    wkhomeApp.config(['$compileProvider', function ($compileProvider) {
        $compileProvider.debugInfoEnabled(false);
    }]);
