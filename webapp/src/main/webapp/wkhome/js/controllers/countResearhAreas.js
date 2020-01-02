wkhomeControllers.controller('countResearchAreas', ['$routeParams', '$scope', 'globalData', 'sparqlQuery', 'searchData', '$window', 'Statistics',
  function($routeParams, $scope, globalData, sparqlQuery, searchData, $window, Statistics) {
    //if click in pie-chart (Research Areas)
    $scope.ifClick = function(value) {
      $window.location.hash = "/" + $routeParams.lang + "/cloud/group-by?area="+value;
    };

    Statistics.query({
      id: 'count_research_areas'
    }, function(data) {
      var endpoints = data['@graph'];
      var dataToSend = []
      endpoints.forEach(function(endpoint) {
        var label = endpoint['uc:name'].hasOwnProperty('@value') ? endpoint['uc:name']['@value'] : endpoint['uc:name'] ;
        var value = endpoint['uc:total'].length > 1 ? endpoint['uc:total'][0]['@value'] : endpoint['uc:total']['@value'];
        dataToSend.push({
          label: label,
          value: value
        });
      });
      $scope.data = {
        'entityName': 'Researchers',
        'data': _.last(_.sortBy(dataToSend, function(d) { return parseInt(d.value); }), 22)
      };
    });
  }
]);
