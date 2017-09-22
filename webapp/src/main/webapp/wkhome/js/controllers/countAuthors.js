
wkhomeControllers.controller('countAuthors', ['$translate', '$routeParams', '$scope', '$window', 'globalData', 'sparqlQuery', 'searchData', 'Statistics',
    function ($translate, $routeParams, $scope, $window, globalData, sparqlQuery, searchData, Statistics) {

        //$window.location.hash = "/es/";

        $translate.use($routeParams.lang);

        //if click in pie-chart (Authors)
        $scope.ifClick = function (value)
        {
            searchData.genericData = value;
            $window.location.hash = "/" + $routeParams.lang + "/w/cloud?" + "datacloud";
        };

        Statistics.query({id: 'count_authors'}, function (data) {
          var endpoints = data['@graph'];
          var dataToSend = []
          if (endpoints) {
              endpoints.forEach(function (endpoint) {
                  dataToSend.push({label: endpoint['uc:name'], value: endpoint['uc:total']['@value']});
              });
                  $scope.data = {'entityName': 'Authors', 'data': dataToSend};
          }
        });
        //sparql construct to get total authors of publications
        // var queryTotalAuthors = globalData.PREFIX
        //         + 'CONSTRUCT { '
        //         + '         ?provenance a uc:Endpoint . '
        //         + '         ?provenance uc:name ?name . '
        //         + '         ?provenance uc:total ?total  } '
        //         + 'WHERE { '
        //         + ' graph <' + globalData.centralGraph + '> '
        //         + '       { '
        //         + '         SELECT ?provenance ?name (COUNT(DISTINCT(?s)) AS ?total) '
        //         + '             WHERE '
        //         + '             { '
        //         + '                 ?s a foaf:Person. '
        //         + '                 ?s foaf:publications ?pub . '
        //         + '                 ?s dct:provenance ?provenance . '
        //         + '                 ?pub dct:title ?title . '
        //         + '                 { '
        //         + '                     SELECT ?name '
        //         + '                     WHERE { '
        //         + '                         GRAPH <' + globalData.endpointsGraph + '> { '
        //         + '                             ?provenance uc:name ?name . '
        //         + '                         }'
        //         + '                     }'
        //         + '                 }'
        //         + '             }'
        //         + '         GROUP BY ?provenance ?name '
        //         + '     } '
        //         + ' } ';

        //for parliament triplestore test
//        var queryTotalAuthors = 'PREFIX dct: <http://purl.org/dc/terms/> PREFIX foaf: <http://xmlns.com/foaf/0.1/> '
//                + 'PREFIX uc: <http://ucuenca.edu.ec/wkhuska/resource/> '
//                + 'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> '
//                +       " construct { ?s ?p ?o } "
//                +    "  WHERE {  ?s ?p ?o } limit 10 " ;

        // sparqlQuery.querySrv({query: queryTotalAuthors}, function (rdf) {
        //     jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
        //         //$scope.data = compacted;
        //         var endpoints = compacted['@graph'];
        //         var data = []
        //         if (endpoints) {
        //             endpoints.forEach(function (endpoint) {
        //                 data.push({label: endpoint['uc:name'], value: endpoint['uc:total']['@value']});
        //             });
        //             $scope.$apply(function () {
        //                 $scope.data = {'entityName': 'Authors', 'data': data};
        //             });
        //         }
        //     });
        // });// End sparqlQuery.querySrv ...
        /*************************************************************/



//*****************************************//
//*********FOR TRANSLATE*******************//
        //  $scope.translate = globalData.translateData; //query and load resource related with selected theme
        $scope.$watch('globalData.language', function (newValue, oldValue, scope) {
            //alert($scope.selectedItem);
            $scope.translate = globalData.translateData; //query and load resource related with selected theme
        });



    }]);
