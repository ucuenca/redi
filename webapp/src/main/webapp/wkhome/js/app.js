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
    'ui.bootstrap.locale-dialog',
    'ngSolr',
    'ngCookie',
    'ui.select',
    'td.easySocialShare'
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

wkhomeApp.service('globalData', ['$window','cookies', function ($window, cookies) {
     var baseURL = $window.location.origin + $window.location.pathname;
    //  baseURL = baseURL.replace("http","https").replace(":" + $window.location.port, "");
     this.serverInstance=baseURL;
     baseURL = baseURL.replace("clon","");
     baseURL = baseURL.replace("localhost:8080","redi.cedia.edu.ec");
    this.language = "es";
    this.centralGraph = baseURL + "context/redi";
    this.externalAuthorsGraph = "http://ucuenca.edu.ec/wkhuska/externalauthors";
    this.clustersGraph = baseURL + "context/clustersPub";
                this.authorsGraph = baseURL + "context/authors";
    this.endpointsGraph = baseURL + "context/endpoints";
    this.organizationsGraph = baseURL + "context/organization";
    this.latindexGraph = baseURL + "context/latindex";
    this.translateData = null;
    //this.publicationsCore = this.serverInstance + 'solr/publications';
    this.publicationsCore = this.serverInstance +'solr/publications';
    this.authorsCore = this.serverInstance +'solr/authors';
    this.projectsCore = this.serverInstance+'solr/projects';
    this.organizationsCore = this.serverInstance +'solr/organizations';
    this.patentsCore = this.serverInstance+'solr/patents';
    this.datasetsCore = this.serverInstance+'solr/datasets';
    this.eventsCore = this.serverInstance+'solr/events';
    this.servicesCore = this.serverInstance+'solr/services';

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
        "dcterms": "http://purl.org/dc/terms/",
        "schema": "http://schema.org/"
    };

    this.urltofindinGOOGLE = 'https://scholar.google.com/scholar?q={0}';
    this.urltofindinDBLP = 'http://dblp.uni-trier.de/search?q={0}';
    this.urltofindinSCOPUS = 'http://www.scopus.com/results/results.uri?numberOfFields=0&src=s&clickedLink=&edit=&editSaveSearch=&origin=searchbasic&authorTab=&affiliationTab=&advancedTab=&scint=1&menu=search&tablin=&searchterm1={0}&field1=TITLE&dateType=Publication_Date_Type&yearFrom=Before+1960&yearTo=Present&loadDate=7&documenttype=All&subjects=LFSC&_subjects=on&subjects=HLSC&_subjects=on&subjects=PHSC&_subjects=on&subjects=SOSC&_subjects=on&st1={1}&st2=&sot=b&sdt=b&sl=91&s=TITLE%28{2}%29';
    this.urltofindinACADEMICS = 'https://academic.microsoft.com/#/search?iq=@{0}@&q={1}&filters=&from=0&sort=0';
    
    this.cookie_prefix = "redi_session_obj";
    
    this.getSession = function (){
        if (cookies.get(this.cookie_prefix)){
          return JSON.parse(cookies.get(this.cookie_prefix));
        }else {
          return null;
        }
    };
    
}]);

wkhomeApp.config(["$routeProvider", "$locationProvider",
    function ($routeProvider, $locationProvider) {
      $locationProvider.hashPrefix('');
      $routeProvider.
                when('/', {
                    templateUrl: 'wkhome/partials/home.html',
                }).
                when('/info/about', {
                  templateUrl: 'wkhome/partials/about.html'
                }).
                 when('/info/work', {
                  templateUrl: 'wkhome/partials/work.html'
                }).
                when('/info/sources', {
                  templateUrl: 'wkhome/partials/sources.html'
                }).
                when('/info/help', {
                  templateUrl: 'wkhome/partials/helpact.html'
                }).
                when('/tags/cloud', {
                  templateUrl: 'wkhome/partials/keywordsCloud.html',
                }).
                when('/cloud/group-by?area=:area', {
                  templateUrl: 'wkhome/partials/groupbyCloud.html',
                }).
                when('/cloud/group-by', {
                  templateUrl: 'wkhome/partials/groupbyCloud.html',
                }).
                when('/view/map', {
                  templateUrl: 'wkhome/partials/map.html',
                }).
                when('/cloud/clusters', {
                  templateUrl: 'wkhome/partials/clusterGroupByCloud.html',
                }).
                when('/info/statistics', {
                  templateUrl: 'wkhome/partials/statistics.html',
                }).
                when('/info/statisticsbyInst/:inst*', {
                  templateUrl: 'wkhome/partials/statisticsbyInst.html',
                }).
                when('/info/statisticsbyAuthor/:author*', {
                  templateUrl: 'wkhome/partials/statisticsbyAuthor.html',
                }).
                when('/info/statisticsbyArea/:areauri*', {
                  templateUrl: 'wkhome/partials/statisticsbyArea.html',
                }).
                 when('/info/publication/:pub*', {
                  templateUrl: 'wkhome/partials/publicationProfile.html',
                }).
                when('/view/datacube', {
                  templateUrl: 'wkhome/partials/dataCube.html',
                }).
                when('/author/publications/:query*/authorProfile/:authorId*', {
                  templateUrl: 'wkhome/partials/publications.html',
                }).
                when('/search/publications/:query*', {
                  templateUrl: 'wkhome/partials/publications.html',
                }).
                when('/total/publications/:query*', {
                  templateUrl: 'wkhome/partials/publicationsAll.html',
                }).
                when('/total/authors/:query*', {
                  templateUrl: 'wkhome/partials/authorsAll.html',
                }).
                when('/total/publications', {
                  templateUrl: 'wkhome/partials/publicationsAll.html',
                }).
                when('/total/authors', {
                  templateUrl: 'wkhome/partials/authorsAll.html',
                }).
                when('/total/projects', {
                  templateUrl: 'wkhome/partials/projectsAll.html',
                }).
                when('/total/projects/:query*', {
                  templateUrl: 'wkhome/partials/projectsAll.html',
                }).
                when('/total/patents', {
                  templateUrl: 'wkhome/partials/patentsAll.html',
                }).
                when('/total/patents/:query*', {
                  templateUrl: 'wkhome/partials/patentsAll.html',
                }).
                when('/total/datasets', {
                  templateUrl: 'wkhome/partials/datasetsAll.html',
                }).
                when('/total/datasets/:query*', {
                  templateUrl: 'wkhome/partials/datasetsAll.html',
                }).
                when('/total/events', {
                  templateUrl: 'wkhome/partials/eventsAll.html',
                }).
                when('/total/events/:query*', {
                  templateUrl: 'wkhome/partials/eventsAll.html',
                }).
                when('/total/services', {
                  templateUrl: 'wkhome/partials/servicesAll.html',
                }).
                when('/total/services/:query*', {
                  templateUrl: 'wkhome/partials/servicesAll.html',
                }).
                when('/total/organizations', {
                  templateUrl: 'wkhome/partials/organizationsAll.html',
                }).
                when('/total/organizations/:query*', {
                  templateUrl: 'wkhome/partials/organizationsAll.html',
                }).
                when('/author/tree/:text*', {
                  templateUrl: 'wkhome/partials/search.html',
                }).
                when('/author/network/:authorId*', {
                  templateUrl: 'wkhome/partials/genericRelatedAuthor.html',
                }).
                when('/author/profileval/:authorId*', {
                  templateUrl: 'wkhome/partials/authorVal.html',
                }).
                when('/author/profile/:author*', {
                  templateUrl: 'wkhome/partials/authorProfile.html',
                 // controller: 'AuthorProfile'
                }).
                when('/project/profile/:project*', {
                  templateUrl: 'wkhome/partials/projectProfile.html',
                 // controller: 'AuthorProfile'
                }).
                when('/organization/profile/:org*', {
                  templateUrl: 'wkhome/partials/orgProfile.html',
                 // controller: 'AuthorProfile'
                }).
                when('/patent/profile/:patent*', {
                  templateUrl: 'wkhome/partials/patentProfile.html',
                 // controller: 'AuthorProfile'
                }).when('/dataset/profile/:dataset*', {
                  templateUrl: 'wkhome/partials/datasetProfile.html',
                 // controller: 'AuthorProfile'
                }).when('/event/profile/:event*', {
                  templateUrl: 'wkhome/partials/eventProfile.html',
                 // controller: 'AuthorProfile'
                }).when('/service/profile/:service*', {
                  templateUrl: 'wkhome/partials/serviceProfile.html',
                 // controller: 'AuthorProfile'
                }).
                 when('/project/collaborationProject', {
                  templateUrl: 'wkhome/partials/ColaborationProject.html',
                 // controller: 'AuthorProfile'
                }).
                when('/group/area', {
                  templateUrl: 'wkhome/partials/genericSubClusterGraph.html',
                }).
                when('/oauth/callback/code/:code*/state/:state*', {
                  templateUrl: 'wkhome/partials/callback.html',
                }).
                        
                when('/:lang/w/search?:text', {//when user search an author in textbox
                    templateUrl: 'wkhome/partials/search.html',
                    //      controller: 'ExploreController'
                }).
                when('/:lang/w/listAllText', {
                    templateUrl: 'wkhome/partials/listPublications.html', //'/wkhome/partials/searchListPublications.html',
                }).
                when('/:lang/w/clusters?:text', {
                    templateUrl: 'wkhome/partials/clustersCloud.html',
                }).
                when('/:lang/b/', {
                    templateUrl: 'wkhome/partials/geoplain.html',
                    controller: 'worldPath'
                }).
                when('/:lang/d3/:geoId.json', {
                    templateUrl: 'wkhome/partials/phone-detail.html',
                }).
                when('/:lang/cloud/keywords', {
                    templateUrl: 'wkhome/partials/clusterKeywordsCloud.html',
                }).
                  when('/patent/register', {
                    templateUrl: 'wkhome/partials/regpatent.html',
                }).
                  when('/group/trends', {
                    templateUrl: 'wkhome/partials/genericTrendsAreas.html',
                }).
                   when('/report/Inst', {
                    templateUrl: 'wkhome/partials/reportAllInst.html',
                }).
                   when('/author/report/Authors', {
                    templateUrl: 'wkhome/partials/reportAllAuthor.html',
                }).
                   when('/author/report/:au*', {
                    templateUrl: 'wkhome/partials/reportAuthor.html',
                }).
                  when('/report/:org*', {
                    templateUrl: 'wkhome/partials/reportInst.html',
                }).
                otherwise({
                    redirectTo: '/'
                })
                ;
    }]);

    wkhomeApp.config(['$compileProvider', function ($compileProvider) {
        $compileProvider.debugInfoEnabled(true);
    }]);

    wkhomeApp.config(["$translateProvider", function ($translateProvider) {
      $translateProvider.useStaticFilesLoader({
          prefix: 'wkhome/translations/locale-',
          suffix: '.json'
      });
      // $translateProvider.translations("en", wkhomeApp.labels_en);
      // $translateProvider.translations("es", wkhomeApp.labels_es);
      $translateProvider.preferredLanguage("es");
      // To get warnings in the developer console, regarding forgotten IDs in translations
      // $translateProvider.useMissingTranslationHandlerLog ();
      // Enable escaping of HTML
      $translateProvider.useSanitizeValueStrategy('escape');
    }]);
