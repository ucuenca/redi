wkhomeControllers.controller('barChart', ['$scope', 'globalData', 'sparqlQuery', 'clustersQuery', 'searchData', '$route', '$window',
    function ($scope, globalData, sparqlQuery, clustersQuery, searchData, $window) {

        var dataToSend = [];
        var totalQuery = globalData.PREFIX
                        + "CONSTRUCT {"
                        + "?provenance uc:totalPublications ?totalPub;"
                        + "uc:totalAuthors ?totalAuthors;"
                        + "uc:name ?name."
                        + "}   WHERE { "
                        +   "{"
                        +     "SELECT DISTINCT ?provenance (SAMPLE(?ies) as ?name) (count(DISTINCT ?author) as ?totalAuthors) (COUNT(DISTINCT ?publications) as ?totalPub) "
                        +     "WHERE {"
                        +       "GRAPH <" + globalData.centralGraph + "> {"
                        +         "?author a foaf:Person."
                        +         "?author dct:provenance ?provenance."
                        +         "?author foaf:publications ?publications."
                        +         "GRAPH <" + globalData.endpointsGraph + "> {"
                        +         	"?provenance uc:name ?ies."
                        +         "}"
                        +       "}"
                        + "    } GROUP BY ?provenance ORDER BY DESC(?totalAuthors)"
                        + "}"
                        + "}";

        sparqlQuery.query({query: totalQuery}, function (result) {
            jsonld.compact(result, globalData.CONTEXT, function (err, compacted) {
                var totalPubAut = compacted["@graph"];
                if (totalPubAut) {
                    _.map(totalPubAut, function (total) {
                        var sourceid = total["@id"];
                        var sourcename = total["uc:name"];
                        var totalAuthors = total["uc:totalAuthors"]["@value"];
                        var totalPublications = total["uc:totalPublications"]["@value"];

                        dataToSend.push({Source: sourcename, freq: {Autores: totalAuthors, Publicaciones: totalPublications, Salud: 0}});
                    });
                    $scope.$apply(function () {
                        $scope.data = dataToSend;
                    });
                }

            });
        });
    }]);
