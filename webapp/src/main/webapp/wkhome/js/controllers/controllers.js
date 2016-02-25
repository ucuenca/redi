//'use strict';

/* Controllers */

var wkhomeControllers = angular.module('wkhomeControllers', ['mapView', 'cloudTag', 'pieChart', 'explorableTree', 'cloudGroup', 'cloudCluster', 'genericCloud', 'snapscroll', 'ui.bootstrap.pagination', 'keywClusters', 'clusterKeywCloud']);

wkhomeControllers.controller('ExploreController', ['$scope', '$window',
    function ($scope, $window) {
        console.log($scope.text);
    }]);





