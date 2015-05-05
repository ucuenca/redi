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
