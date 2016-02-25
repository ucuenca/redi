
wkhomeControllers.controller('countPublications', ['$scope', 'globalData', 'sparqlQuery',
    function ($scope, globalData, sparqlQuery) {
        var queryTotalAuthors = globalData.PREFIX
                + 'CONSTRUCT { '
                + ' ?source a uc:Provider. '
                + ' ?source uc:name ?name;  '
                + '        uc:total ?total } '
                + ' WHERE {  '
                + '     SELECT ?source (substr(?source,8) as ?name) ?total '
                + '         WHERE { '
                + '             SELECT DISTINCT ?source (COUNT(?s) AS ?total) '
                + '             WHERE { '
                + '                 GRAPH <'+globalData.centralGraph+'> { '
                + '                     ?s foaf:Organization  ?source. '
                + '                 } '
                + '             } '
                + '             GROUP BY ?source '
                + '         } '
                + ' } ';
        sparqlQuery.querySrv({query: queryTotalAuthors}, function (rdf) {
            jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                //$scope.data = compacted;
                var endpoints = compacted['@graph'];
                var data = []
                endpoints.forEach(function (endpoint) {
                    var name = endpoint['uc:name'];
                    if (name.indexOf('scopus')>-1)
                    {
                            data.push({label: 'SCOPUS', value: endpoint['uc:total']['@value']});
                    }
                    else
                    if (name.indexOf('microsoft')>-1)
                    {
                            data.push({label: 'MICROSOFT ACADEMICS', value: endpoint['uc:total']['@value']});
                    }
                    else
                    if (name.indexOf('dblp')>-1)
                    {
                            data.push({label: 'DBLP', value: endpoint['uc:total']['@value']});
                    }
                    else
                    {
                        data.push({label: endpoint['uc:name'], value: endpoint['uc:total']['@value']});
                    }
                });
                $scope.$apply(function () {
                    $scope.data = {'entityName': 'Articles', 'data': data};
                });
            });
        });
    }]);