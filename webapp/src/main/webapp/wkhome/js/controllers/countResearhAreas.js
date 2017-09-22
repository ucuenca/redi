wkhomeControllers.controller('countResearchAreas', ['$routeParams', '$scope', 'globalData', 'sparqlQuery', 'searchData', '$window', 'Statistics',
  function($routeParams, $scope, globalData, sparqlQuery, searchData, $window, Statistics) {
    //if click in pie-chart (Research Areas)
    $scope.ifClick = function(value) {
      searchData.researchArea = value;
      $window.location.hash = "/" + $routeParams.lang + "/cloud/group-by";
    };

    Statistics.query({
      id: 'count_research_areas'
    }, function(data) {
      var endpoints = data['@graph'];
      var dataToSend = []
      endpoints.forEach(function(endpoint) {
        var label = endpoint['uc:name'];
        var value = endpoint['uc:total'].length > 1 ? endpoint['uc:total'][0]['@value'] : endpoint['uc:total']['@value'];
        dataToSend.push({
          label: label,
          value: value
        });
      });
      $scope.data = {
        'entityName': 'Researchers',
        'data': dataToSend
      };
    });
    // var queryTotalAreas = globalData.PREFIX
    //         + ' CONSTRUCT { '
    //         + '     ?uriArea a uc:ResearchArea. '
    //         + '     ?uriArea uc:name ?keyword; '
    //         + '                 uc:total ?total }'
    //         + ' WHERE {  '
    //         + '     SELECT  ?keyword (IRI(REPLACE(?keyword, " ", "_", "i")) as ?uriArea) ?total '
    //         + '     WHERE { '
    //         + '         { '
    //         + '             SELECT DISTINCT ?keyword (COUNT(DISTINCT ?s) AS ?total) '
    //         + '             WHERE { '
    //         + '                 GRAPH <' + globalData.centralGraph + '> { '
    //         + '                     ?s foaf:publications ?publications. '
    //         + '                     ?publications dcterms:subject ?keywordSubject. '
    //         + '                     ?keywordSubject rdfs:label ?keyword. '
    //         //+ '                     ?s dct:subject ?keyword. '
    //         + '                 } '
    //         + '              } '
    //         + '              GROUP BY ?keyword '
    //         + '              ORDER BY DESC(?total) '
    //         + '              LIMIT 10 '
    //         + '         } '
    //         + '         FILTER(!REGEX(?keyword,"TESIS")) '
    //         + '     }'
    //         + ' }';
    // sparqlQuery.querySrv({query: queryTotalAreas}, function (rdf) {
    //     jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
    //         //$scope.data = compacted;
    //         var endpoints = compacted['@graph'];
    //         var data = []
    //         endpoints.forEach(function (endpoint) {
    //             var label = endpoint['uc:name'];
    //             var value = endpoint['uc:total'].length > 1 ? endpoint['uc:total'][0]['@value'] : endpoint['uc:total']['@value'];
    //             data.push({label: label, value: value});
    //         });
    //         $scope.$apply(function () {
    //             $scope.data = {'entityName': 'Researchers', 'data': data};
    //         });
    //     });
    // });
  }
]);
