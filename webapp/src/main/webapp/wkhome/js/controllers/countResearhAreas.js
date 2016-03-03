

wkhomeControllers.controller('countResearchAreas', ['$routeParams','$scope', 'globalData', 'sparqlQuery', 'searchData', '$window',
    function ($routeParams, $scope, globalData, sparqlQuery, searchData, $window) {
        //if click in pie-chart (Research Areas)
        $scope.ifClick = function (value)
        {
            searchData.researchArea = value;
            $window.location.hash = "/" + $routeParams.lang + "/cloud/group-by";
        };
        var queryTotalAreas = globalData.PREFIX
                + ' CONSTRUCT { '
                + '     ?uriArea a uc:ResearchArea. '
                + '     ?uriArea uc:name ?keyword; '
                + '                 uc:total ?total }'
                + ' WHERE {  '
                + '     SELECT  ?keyword (IRI(REPLACE(?keyword, " ", "_", "i")) as ?uriArea) ?total '
                + '     WHERE { '
                + '         { '
                + '             SELECT DISTINCT ?keyword (COUNT(?publications) AS ?total) '
                + '             WHERE { '
                + '                 GRAPH <' + globalData.centralGraph + '> { '
                + '                     ?s foaf:publications ?publications. '
                + '                     ?s dct:subject ?keyword. '
                + '                 } '
                + '              } '
                + '              GROUP BY ?keyword '
                + '              HAVING (?total < 200 )'
                + '              ORDER BY DESC(?total) '
                + '              LIMIT 15 '
                + '         } '
                + '     }'
                + ' }';
        sparqlQuery.querySrv({query: queryTotalAreas}, function (rdf) {
            jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                //$scope.data = compacted;
                var endpoints = compacted['@graph'];
                var data = []
                endpoints.forEach(function (endpoint) {
                    data.push({label: endpoint['uc:name'], value: endpoint['uc:total']['@value']});
                });
                $scope.$apply(function () {
                    $scope.data = {'entityName': 'Articles', 'data': data};
                });
            });
        });
    }]);