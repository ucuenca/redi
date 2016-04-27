wkhomeControllers.controller('barChart', ['$scope', 'globalData', 'sparqlQuery', 'clustersQuery', 'searchData', '$route', '$window',
    function ($scope, globalData, sparqlQuery, clustersQuery, searchData, $window) {

        var dataToSend = [];
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
                + '                 ?s foaf:name ?name.'
                + '                 ?s foaf:publications ?pub.'
                + '                 ?s dct:provenance ?provenance.     '
                + '                 { '
                + '                     SELECT * '
                + '                     WHERE '
                + '                     { '
                + '                         graph <http://ucuenca.edu.ec/wkhuska/endpoints> '
                + '                         { '
                + '           			?provenance uc:name ?sourcename '
                + '                         } '
                + '                     } '
                + '                 } '
                + '             }'
                + '         }group by ?provenance '
                + ' } '
                + ' }';
        /**
         * Getting source and count publications
         */
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
                         * Getting the first 10 keywords that most publications have of each source
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

                            });
                        }); // end sparqlQuery of  Getting the first 10 keywords that most publications have of each source

                    });
                    drawing(dataToSend);
                }

            });
        }); // end sparqlQuery of Getting source and count publications
        drawing = function (value) {
            $scope.data = value;

        };
        var freqData = [
            {State: 'ESPE', freq: {Autores: 4786, Publicaciones: 1319, Salud: 249}}
            , {State: 'UCUENCA', freq: {Autores: 1101, Agua: 412, Salud: 674}}
            , {State: 'ESPOL', freq: {Autores: 932, Agua: 2149, Salud: 418}}
            , {State: 'DE', freq: {Autores: 832, Agua: 1152, Salud: 1862}}
            , {State: 'FL', freq: {Autores: 4481, Agua: 3304, Salud: 948}}
            , {State: 'GA', freq: {Autores: 1619, Agua: 167, Salud: 1063}}
            , {State: 'IA', freq: {Autores: 1819, Agua: 247, Salud: 1203}}
            , {State: 'IL', freq: {Autores: 4498, Agua: 3852, Salud: 942}}
            , {State: 'IN', freq: {Autores: 797, Agua: 1849, Salud: 1534}}
            , {State: 'KS', freq: {Autores: 162, Agua: 379, Salud: 471}}
        ];


        //$scope.data = dataToSend;

    }]); //end barController 
