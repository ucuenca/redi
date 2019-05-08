'use strict';

/* Services */

var wkhomeServices = angular.module('wkhomeServices', ['ngResource']);

//For testing purposes
//wkhomeServices.serverInstance = 'http://190.15.141.85:8080/marmottatest';
//wkhomeServices.serverInstance = 'http://190.15.141.85:80';
//wkhomeServices.serverInstance = 'http://localhost:8080/marmotta';
//for parliament triplestore test
//wkhomeServices.serverInstance = 'http://localhost:8080/parliament';


/* Sample of a RESTful client Service */
wkhomeServices.factory('Phone', ['$resource',
    function ($resource) {
        return $resource('phones/:phoneId.json', {}, {
            query: {method: 'GET', params: {phoneId: 'phones'}, isArray: true}
        });
    }]);



/* RESTful client Service */
wkhomeServices.factory('authorRestQuery', ['$resource', '$http', 'globalData',
    function ($resource, $http, globalData) {
        $http.defaults.headers.common['content-type'] = 'application/x-www-form-urlencoded';
        $http.defaults.headers.common['Accept'] = 'application/ld+json';
        var transform = function (data) {
            return $.param(data);
        };
        var serverInstance = globalData.serverInstance;
        return $resource(serverInstance + 'pubman/pubsearch', {}, {
            query: {method: 'POST', isArray: true, transformRequest: transform, headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'}}
        });
    }]);


wkhomeServices.factory('sparqlQuery', ['$resource', '$http', 'globalData',
    function ($resource, $http, globalData) {
        $http.defaults.headers.common['content-type'] = 'application/x-www-form-urlencoded';
        $http.defaults.headers.common['Accept'] = 'application/ld+json';
        var transform = function (data) {
            return $.param(data);
        }
        var serverInstance = globalData.serverInstance;
        return $resource(serverInstance + 'mongo/sparql', {}, {
            querySrv: {method: 'POST', isArray: true, transformRequest: transform, headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'}}
        });
    }]);

wkhomeServices.factory('d3JSON', ['$resource',
    function ($resource) {
        return $resource('d3/:geoId.json', {}, {
            query: {method: 'GET', params: {geoId: 'world-50m'}, isArray: true}
        });
    }]);

wkhomeServices.factory('clustersQuery', ['$resource', '$http', '$window',
    function ($resource, $http, $window) {

        function successCallback(response) {
            return response.data;
        }
        ;
        function errorCallback(error) {
            return error.data;
        }
        ;

        return $http.get('resources/datos_clustering.json')
                .then(successCallback, errorCallback);

    }]);

wkhomeServices.factory('translateService', ['$resource', '$http', '$window', 'globalData',
    function ($resource, $http, $window, globalData) {
        return $resource('resources/:data.json', {}, {
            query: {method: 'GET', params: {data: globalData.language}, isArray: false}
        });

    }]);

wkhomeServices.factory('reportService', ['$resource', '$http', 'globalData',
    function ($resource, $http, globalData) {
        $http.defaults.headers.common['content-type'] = 'application/x-www-form-urlencoded';
        $http.defaults.headers.common['Accept'] = 'application/ld+json';
        var transform = function (data) {
            data.hostname = globalData.serverInstance;
            return $.param(data);
        }
        var serverInstance = globalData.serverInstance;
        return $resource(
                serverInstance + 'pubman/report', {}, {
            querySrv: {method: 'POST', isArray: false, transformRequest: transform, headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'}}
        });
    }]);

wkhomeServices.factory('AuthorsService', ['$resource', '$http', 'globalData',
    function ($resource, $http, globalData) {
        var serverInstance = globalData.serverInstance;
        return $resource(serverInstance + 'solr/authors/select?q=name%3A(:search)&fq=org%3A*&wt=json&fl=lmf.uri,name,topics', {search: '@id'});
    }]);
wkhomeServices.factory('PublicationsService', ['$resource', '$http', 'globalData',
    function ($resource, $http, globalData) {
        var serverInstance = globalData.serverInstance;
        return $resource(serverInstance + 'solr/publications/select?q=":search"&wt=json&fl=lmf.uri', {search: '@id'});
    }]);
wkhomeServices.factory('KeywordsService', ['$resource', '$http', 'globalData',
    function ($resource, $http, globalData) {
        var serverInstance = globalData.serverInstance;
        return $resource(serverInstance + 'solr/keywords/select?q=":search"&fl=lmf.uri,keyword&wt=json&', {search: '@id'});
    }]);

wkhomeServices.factory('RecomendService', ['$resource', '$http', 'globalData',
    function ($resource, $http, globalData) {
       // q=Geospatial+and+statistical+information+INSPIRE+Linked+data+RDF+&fq=NOT+uri%3A"https%3A%2F%2Fredi.cedia.edu.ec%2Fresource%2Fpublication%2Fa97efe3177208d8408c2e8ef882dbf56"&rows=3&fl=lmf.uri%2Ctitle%2Ccontributor-name%2Cauthor-name&wt=xml&indent=true
        var serverInstance = globalData.serverInstance;
       //  var serverInstance =  "https://rediclon.cedia.edu.ec/";
        return $resource(serverInstance + 'solr/publications/select?q=:search&rows=3&fl=lmf.uri,title,contributor-name,author-name&wt=json&indent=true', {search: '@id'});

    }]);

wkhomeServices.factory('searchQueryService', ['$resource', '$http', 'globalData',
    function ($resource, $http, globalData) {
        $http.defaults.headers.common['content-type'] = 'application/x-www-form-urlencoded';
        $http.defaults.headers.common['Accept'] = 'application/ld+json';
        var transform = function (data) {
            data.hostname = globalData.serverInstance;
            return $.param(data);
        }
        var serverInstance = globalData.serverInstance;
        return $resource(
                serverInstance + 'pubman/searchQuery', {}, {
            querySrv: {method: 'POST', isArray: false, transformRequest: transform, headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'}}
        });
    }]);

wkhomeServices.factory('Statistics', ['$resource', '$http', 'globalData',
    function ($resource, $http, globalData) {
        var serverInstance = globalData.serverInstance;
        return $resource(serverInstance + 'mongo/statistics?id=:id', {}, {
            query: {
                method: 'GET',
                params: {id: 'id'},
                isArray: false,
                cache: true,
                headers: {'Accept': 'application/json'}
            }
        });
    }
]);

wkhomeServices.factory('StatisticsbyInst', ['$resource', '$http', 'globalData',
    function ($resource, $http, globalData) {
        var serverInstance = globalData.serverInstance;
        return $resource(serverInstance + 'mongo/statisticsbyInst?id=:id', {}, {
            query: {
                method: 'GET',
                params: {id: 'id'},
                isArray: false,
                cache: true,
                headers: {'Accept': 'application/json'}
            }
        });
    }
]);

wkhomeServices.factory('StatisticsbyAuthor', ['$resource', '$http', 'globalData',
    function ($resource, $http, globalData) {
        var serverInstance = globalData.serverInstance;
        return $resource(serverInstance + 'mongo/author-stats?uri=:id', {}, {
            query: {
                method: 'GET',
                params: {id: 'id'},
                isArray: false,
                cache: true,
                headers: {'Accept': 'application/json'}
            }
        });
    }
]);

wkhomeServices.factory('Authors', ['$resource', '$http', 'globalData',
    function ($resource, $http, globalData) {
        var serverInstance = globalData.serverInstance;
        return $resource(serverInstance + 'mongo/author?uri=:id', {}, {
            query: {
                method: 'GET',
                params: {id: 'id'},
                isArray: false,
                cache: true,
                headers: {'Accept': 'application/json'}
            }
        });
    }
]);

wkhomeServices.factory('Countries', ['$resource', '$http', 'globalData',
    function ($resource, $http, globalData) {
        var serverInstance = globalData.serverInstance;
        return $resource(serverInstance + 'mongo/countries', {}, {
            query: {
                method: 'GET',
                isArray: true,
                cache: true,
                headers: {'Accept': 'application/json'}
            }
        });
    }
]);

wkhomeServices.factory('clustersTotals', ['$resource', '$http', 'globalData',
    function ($resource, $http, globalData) {
        var serverInstance = globalData.serverInstance;
        return $resource(serverInstance + 'mongo/clustersTotals', {}, {
            query: {
                method: 'GET',
                isArray: true,
                cache: true,
                headers: {'Accept': 'application/json'}
            }
        });
    }
]);

wkhomeServices.factory('subclustersTotals', ['$resource', '$http', 'globalData',
    function ($resource, $http, globalData) {
        var serverInstance = globalData.serverInstance;
        return $resource(serverInstance + 'mongo/subclustersTotals?uri=:id', {}, {
            query: {
                method: 'GET',
                params: {id: 'id'},
                isArray: true,
                cache: true,
                headers: {'Accept': 'application/json'}
            }
        });
    }
]);

wkhomeServices.factory('Journal', ['$resource', '$http', 'globalData',
    function ($resource, $http, globalData) {
        var factory = {};
        factory.getJournal = function () {
            //var serverInstance = "https://rediclon.cedia.edu.ec/"
            var serverInstance = globalData.serverInstance;
            return $resource(serverInstance + "solr/collections/select?q=collec-uri%3A%22:uri%22&fl=*&wt=json", {
                uri: '@id'
            });
        }
        return factory;
    }
]);

wkhomeServices.factory('querySubcluster', ['$resource', '$http', 'globalData',
    function ($resource, $http, globalData) {
        var serverInstance = globalData.serverInstance;
        return $resource(serverInstance + 'mongo/cluster?uri=:id', {}, {
            query: {
                method: 'GET',
                params: {id: 'id'},
                isArray: false,
                cache: true,
                headers: {'Accept': 'application/json'}
            }
        });
    }
]);

wkhomeServices.service('searchTextResultsService', ['$rootScope', function ($rootScope) {
        this.bucket = {};
        this.saveData = function (data) {
            this.bucket['data'] = data;
            $rootScope.$broadcast('saveData');
        }
        this.getData = function () {
            return this.bucket['data'];
        }
        return this;
    }]);


wkhomeServices.factory('getORCIDToken', ['$resource', '$http', 'globalData',
    function ($resource, $http, globalData) {
        var serverInstance = globalData.serverInstance;
        return $resource(serverInstance + 'mongo/getORCIDToken?uri=:uri&code=:code', {}, {
            query: {
                method: 'GET',
                params: {uri: 'uri', code: 'code'},
                isArray: false,
                cache: false,
                headers: {'Accept': 'application/json'}
            }
        });
    }
]);
