wkhomeControllers.controller('barChart', ['$scope', 'globalData', 'sparqlQuery', 'clustersQuery', 'searchData', '$route', '$window',
    function ($scope, globalData, sparqlQuery, clustersQuery, searchData, $window) {

        var dataToSend = [];
        /**
         * Getting source and count publications
         */
        var sparqlquery = globalData.PREFIX
                + ' CONSTRUCT '
                + ' {  '
                + '     ?prov uc:total ?totalp.'
                + '     ?prov uc:name ?sname.'
                + ' } '
                + ' WHERE {'
                + ' {'
                + '     SELECT DISTINCT (SAMPLE(?provenance)  as ?prov) (SAMPLE(?sourcename)  as ?sname)  (count(DISTINCT ?pub) as ?totalp)'
                + '         WHERE {'
                + '             GRAPH <' + globalData.centralGraph + '> {'
                + '                 ?s a foaf:Person.'
                // + '                 ?s foaf:name ?name.'
                + '                 ?s foaf:publications ?pub.'
                // + '                 ?pub dct:title ?title.'
                + '                 ?s dct:provenance ?provenance.     '
                + '                 { '
                + '                     SELECT * '
                + '                     WHERE '
                + '                     { '
                + '                         graph <' + globalData.endpointsGraph + '> '
                + '                         { '
                + '           			?provenance uc:name ?sourcename '
                + '                         } '
                + '                     } '
                + '                 } '
                + '             }'
                + '         }group by ?provenance '
                + ' } '
                + ' }';

        sparqlQuery.query({query: sparqlquery}, function (result) {
            jsonld.compact(result, globalData.CONTEXT, function (err, compacted) {
                var sources = compacted["@graph"];
                if (sources)
                {
                    _.map(sources, function (source) {
                        var sourceid = source["@id"];
                        var sourcename = source["uc:name"];
                        var totalPubBySource = source["uc:total"]["@value"];
                        /**
                         * count authors of each source
                         */
                        var sparqlCountKeywords = globalData.PREFIX
                                + ' CONSTRUCT '
                                + ' {  <' + sourceid + '> uc:total ?totalAuthors.'
                                + ' } '
                                + ' WHERE {'
                                + ' {'
                                + '     SELECT DISTINCT (count(DISTINCT ?s) as ?totalAuthors)'
                                + '     WHERE {'
                                + '         GRAPH <' + globalData.centralGraph + '> {'
                                + '             ?s foaf:publications ?pub.'
                                + '             ?s dct:provenance <' + sourceid + '>. '
                                + '       }'
                                + '     }  '
                                + ' } '
                                + ' } ';
                        sparqlQuery.query({query: sparqlCountKeywords}, function (result) {
                            jsonld.compact(result, globalData.CONTEXT, function (err, compacted) {
                                var source = compacted["@graph"][0];
                                var totalAutBySource = source["uc:total"]["@value"];
                                var id = source["@id"];
                                dataToSend.push({Source: sourcename, freq: {Autores: totalAutBySource, Publicaciones: totalPubBySource, Salud: 0}});
                                $scope.$apply(function () {
                                    $scope.data = dataToSend;
                                });
                            });
                        }); // end sparqlQuery of  Getting the first 10 keywords that most publications have of each source

                    });
                    // drawing(dataToSend);
                }

            });
        }); // end sparqlQuery of Getting source and count publications

//        drawing = function (value) {
//            $scope.data = value;
//
//        };



        //$scope.data = dataToSend;

    }]); //end barController
