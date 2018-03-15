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
    'hm.readmore',
    'ui.bootstrap',
    'ui.bootstrap.locale-dialog'
]);



wkhomeApp.service('searchData', function () {
    this.authorSearch = null;
    this.areaSearch = null;
    this.genericData = null;
    this.defaultArea = "Semantic Web";
    this.selectedTagItem = "Semantic Web";
    this.globalauthor = null;
    this.clustersAuthors = null;
    this.dataRequested = false;
});

wkhomeApp.service('globalData', ['$window', function ($window) {
    //  var baseURL = "http://redi.cedia.edu.ec/";
     var baseURL = $window.location.origin + $window.location.pathname;
     baseURL = baseURL.replace("clon","");
     baseURL = baseURL.replace("localhost:8080","redi.cedia.edu.ec");
    this.language = "es";
    this.centralGraph = baseURL + "context/redi";
    this.externalAuthorsGraph = "http://ucuenca.edu.ec/wkhuska/externalauthors";
    this.clustersGraph = baseURL + "context/clusters";
                this.authorsGraph = baseURL + "context/authors";
    this.endpointsGraph = baseURL + "context/endpoints";
    this.organizationsGraph = baseURL + "context/organization";
    this.latindexGraph = baseURL + "context/latindex";
    this.translateData = null;
    this.PREFIX = 'PREFIX bibo: <http://purl.org/ontology/bibo/>'
            + ' PREFIX foaf: <http://xmlns.com/foaf/0.1/>  '
            + ' PREFIX dct: <http://purl.org/dc/terms/> '
            + ' PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> '
            + ' PREFIX uc: <http://ucuenca.edu.ec/ontology#>  '
            + ' PREFIX mm: <http://marmotta.apache.org/vocabulary/sparql-functions#> '
            + ' PREFIX dcterms: <http://purl.org/dc/terms/>'
            + 'PREFIX schema: <http://schema.org/>'
            ;
    this.CONTEXT = {
        "uc": "http://ucuenca.edu.ec/ontology#",
        "foaf": "http://xmlns.com/foaf/0.1/",
        "rdfs": "http://www.w3.org/2000/01/rdf-schema#",
        "bibo": "http://purl.org/ontology/bibo/",
        "dc": "http://purl.org/dc/elements/1.1/",
        "dct": "http://purl.org/dc/terms/",
        "dcterms": "http://purl.org/dc/terms/"
    };

    this.urltofindinGOOGLE = 'https://scholar.google.com/scholar?q={0}';
    this.urltofindinDBLP = 'http://dblp.uni-trier.de/search?q={0}';
    this.urltofindinSCOPUS = 'http://www.scopus.com/results/results.uri?numberOfFields=0&src=s&clickedLink=&edit=&editSaveSearch=&origin=searchbasic&authorTab=&affiliationTab=&advancedTab=&scint=1&menu=search&tablin=&searchterm1={0}&field1=TITLE&dateType=Publication_Date_Type&yearFrom=Before+1960&yearTo=Present&loadDate=7&documenttype=All&subjects=LFSC&_subjects=on&subjects=HLSC&_subjects=on&subjects=PHSC&_subjects=on&subjects=SOSC&_subjects=on&st1={1}&st2=&sot=b&sdt=b&sl=91&s=TITLE%28{2}%29';
    this.urltofindinACADEMICS = 'https://academic.microsoft.com/#/search?iq=@{0}@&q={1}&filters=&from=0&sort=0';
}]);

wkhomeApp.config(["$routeProvider", "$locationProvider",
    function ($routeProvider, $locationProvider) {
      $locationProvider.hashPrefix('');
      $routeProvider.
                when('/', {
                    templateUrl: '/wkhome/partials/home.html',
                }).
                when('/about', {
                  templateUrl: '/wkhome/partials/about.html'
                }).
                when('/sources', {
                  templateUrl: '/wkhome/partials/sources.html'
                }).
                when('/help', {
                  templateUrl: '/wkhome/partials/help.html'
                }).
                when('/tags/cloud', {
                  templateUrl: '/wkhome/partials/keywordsCloud.html',
                }).
                when('/cloud/group-by?area=:area', {
                  templateUrl: '/wkhome/partials/groupbyCloud.html',
                }).
                when('/cloud/group-by', {
                  templateUrl: '/wkhome/partials/groupbyCloud.html',
                }).
                when('/geo-views/sources', {
                  templateUrl: '/wkhome/partials/map.html',
                }).
                when('/cloud/clusters', {
                  templateUrl: '/wkhome/partials/clusterGroupByCloud.html',
                }).
                when('/statistics', {
                  templateUrl: '/wkhome/partials/statistics.html',
                }).
                when('/datacube', {
                  templateUrl: '/wkhome/partials/dataCube.html',
                }).


                when('/:lang/:section', {
                    templateUrl: '/wkhome/partials/home.html',
                }).
                when('/:lang/w/search?:text', {//when user search an author in textbox
                    templateUrl: '/wkhome/partials/search.html',
                    //      controller: 'ExploreController'
                }).
                when('/w/author/:text*', {//when user search an author in textbox
                    templateUrl: '/wkhome/partials/search.html',
                }).
                when('/:lang/w/listAllText', {
                    templateUrl: '/wkhome/partials/listPublications.html', //'/wkhome/partials/searchListPublications.html',
                }).
                when('/:lang/w/cloud/:authorId*\/', {
                    templateUrl: '/wkhome/partials/genericRelatedAuthor.html',
                }).
                when('/:lang/w/publications/:authorId*\/', {
                    templateUrl: '/wkhome/partials/publications.html',
                }).
                when('/:lang/w/clusters?:text', {
                    templateUrl: '/wkhome/partials/clustersCloud.html',
                }).
                when('/:lang/b/', {
                    templateUrl: '/wkhome/partials/geoplain.html',
                    controller: 'worldPath'
                }).
                when('/:lang/d3/:geoId.json', {
                    templateUrl: '/wkhome/partials/phone-detail.html',
                }).
                when('/:lang/cloud/keywords', {
                    templateUrl: '/wkhome/partials/clusterKeywordsCloud.html',
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
        $compileProvider.debugInfoEnabled(true);
    }]);

    wkhomeApp.config(["$translateProvider", function ($translateProvider) {
      $translateProvider.useStaticFilesLoader({
          prefix: '/wkhome/translations/locale-',
          suffix: '.json'
      });
      // $translateProvider.translations("en", wkhomeApp.labels_en);
      // $translateProvider.translations("es", wkhomeApp.labels_es);
      $translateProvider.preferredLanguage("en");
      // To get warnings in the developer console, regarding forgotten IDs in translations
      // $translateProvider.useMissingTranslationHandlerLog ();
      // Enable escaping of HTML
      $translateProvider.useSanitizeValueStrategy('escape');
    }]);
