//'use strict';

/* Controllers */

var wkhomeControllers = angular.module('wkhomeControllers', ['barChart','mapView', 'cloudTag', 'pieChart', 'explorableTree', 'cloudGroup', 'cloudCluster', 'genericCloud' , 'relatedAuthor', 'snapscroll', 'ui.bootstrap.pagination', 'keywClusters', 'clusterKeywCloud']);

wkhomeControllers.controller('ExploreController', ['$scope', '$window',
    function ($scope, $window) {
        console.log($scope.text);
    }]);





