'use strict';

/* Services */

var wkhomeServices = angular.module('wkhomeServices', ['ngResource']);

/* Sample of a RESTful client Service */
wkhomeServices.factory('Phone', ['$resource',
  function($resource){
    return $resource('phones/:phoneId.json', {}, {
      query: {method:'GET', params:{phoneId:'phones'}, isArray:true}
    });
  }]);

wkhomeServices.factory('sparqlQuery', ['$resource', '$http', '$window',
  function($resource, $http, $window){
    $http.defaults.headers.common['content-type'] = 'application/x-www-form-urlencoded';
    $http.defaults.headers.common['Accept'] = 'application/ld+json';
    var transform = function(data){
        return $.param(data);
    }
    return $resource('http://' + $window.location.hostname + ($window.location.port ? ':8080':'') + '/marmotta/sparql/select', {}, {
      querySrv: {method:'POST', isArray:true, transformRequest: transform}
    });
  }]);

/*
wkhomeServices.factory('searchData', function(){
    this.authorSearch = [];
  });
*/
wkhomeServices.factory('d3JSON', ['$resource',
  function($resource){
    return $resource('d3/:geoId.json', {}, {
      query: {method:'GET', params:{geoId:'world-50m'}, isArray:true}
    });
  }]);
