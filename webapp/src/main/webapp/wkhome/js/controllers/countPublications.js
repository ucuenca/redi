wkhomeControllers.controller('countPublications', ['$window', '$routeParams', '$scope', 'searchData', 'globalData', 'sparqlQuery', 'Statistics',
  function($window, $routeParams, $scope, searchData, globalData, sparqlQuery, Statistics) {

    //if click in pie-chart (Authors)
    $scope.ifClick = function(value) {
      searchData.genericData = value;
      $window.location.hash = "/" + $routeParams.lang + "/w/cloud?" + "datacloud";
    };

    Statistics.query({
      id: 'count_publications'
    }, function(data) {
      var endpoints = data['@graph'];
      var data = []
      endpoints.forEach(function(endpoint) {
        var name = endpoint['uc:name'];

        data.push({
          label: name,
          value: endpoint['uc:total']['@value']
        });

      });
      $scope.data = {
        'entityName': 'Articles',
        'data': data
      };
    });
  }
]);
