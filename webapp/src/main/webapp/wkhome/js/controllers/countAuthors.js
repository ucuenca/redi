
wkhomeControllers.controller('countAuthors', ['$translate', '$routeParams', '$scope', '$window', 'globalData', 'sparqlQuery', 'searchData',
    function ($translate, $routeParams, $scope, $window, globalData, sparqlQuery, searchData) {

        //$window.location.hash = "/es/";

        $translate.use($routeParams.lang);

        //if click in pie-chart (Authors)
        $scope.ifClick = function (value)
        {
            searchData.genericData = value;
            $window.location.hash = "/" + $routeParams.lang + "/w/cloud?" + "datacloud";
        };
        //sparql construct to get total authors of publications
        var queryTotalAuthors = globalData.PREFIX
                + 'CONSTRUCT { '
                + '         ?provenance a uc:Endpoint . '
                + '         ?provenance uc:name ?name . '
                + '         ?provenance uc:total ?total  } '
                + 'WHERE { '
                + ' graph <' + globalData.centralGraph + '> '
                + '       { '
                + '         SELECT ?provenance ?name (COUNT(DISTINCT(?s)) AS ?total) '
                + '             WHERE '
                + '             { '
                + '                 ?s a foaf:Person. '
                + '                 ?s foaf:publications ?pub . '
                + '                 ?s dct:provenance ?provenance . '
                + '                 ?pub dct:title ?title . '
                + '                 { '
                + '                     SELECT ?name '
                + '                     WHERE { '
                + '                         GRAPH <' + globalData.endpointsGraph + '> { '
                + '                             ?provenance uc:name ?name . '
                + '                         }'
                + '                     }'
                + '                 }'
                + '             }'
                + '         GROUP BY ?provenance ?name '
                + '     } '
                + ' } ';

        //for parliament triplestore test
//        var queryTotalAuthors = 'PREFIX dct: <http://purl.org/dc/terms/> PREFIX foaf: <http://xmlns.com/foaf/0.1/> '
//                + 'PREFIX uc: <http://ucuenca.edu.ec/wkhuska/resource/> '
//                + 'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> '
//                +       " construct { ?s ?p ?o } "  
//                +    "  WHERE {  ?s ?p ?o } limit 10 " ;

        sparqlQuery.querySrv({query: queryTotalAuthors}, function (rdf) {
            jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                //$scope.data = compacted;
                var endpoints = compacted['@graph'];
                var data = []
                if (endpoints) {
                    endpoints.forEach(function (endpoint) {
                        data.push({label: endpoint['uc:name'], value: endpoint['uc:total']['@value']});
                    });
                    $scope.$apply(function () {
                        $scope.data = {'entityName': 'Authors', 'data': data};
                    });
                }
            });
        });// End sparqlQuery.querySrv ...
        /*************************************************************/
        /*************************************************************/
        /*query to get the keywords in memory */
        /*************************************************************/
        loadAllKeyword();
        $scope.themes = [];
        function loadAllKeyword() {
            var queryKeywords = globalData.PREFIX
                    + ' CONSTRUCT { ?keywordp rdfs:label ?keyp } '
                    + '	FROM <' + globalData.centralGraph + '> '
                    + ' WHERE { '
                    + '     SELECT  (count(?key) as ?k) (SAMPLE(?keyword) as ?keywordp) (SAMPLE(?key) as ?keyp) '
                    + '         WHERE { '
                    + '              ?subject foaf:publications ?pubs. '
                    + '              ?subject dct:subject ?key. '
                    + '             BIND(REPLACE(?key, " ", "_", "i") AS ?unickey). '
                    + '             BIND(IRI(?unickey) as ?keyword) '
                    + '         } '
                    + '     GROUP BY ?subject '
                    //            + '     HAVING(?k > 1) '
                    + '}';
            sparqlQuery.querySrv({query: queryKeywords}, function (rdf) {

                jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                    _.map(compacted["@graph"], function (pub) {
                        var model = {};
                        model["id"] = pub["@id"];
                        model["tag"] = pub["rdfs:label"];
                        $scope.themes.push({tag: model["tag"]});
                    });
                    $scope.$apply(function () {
                        searchData.allkeywords = $scope.themes;
                    });
                    waitingDialog.hide();
                });
            });
        }
        /***********************************/
        /***********************************/




        /*********************************************/
        /* LOAD DATA TO KEYWORDS CLOUD */
        /*********************************************/

        var queryKeywords = globalData.PREFIX
                + ' CONSTRUCT { '
                + '         ?keyword rdfs:label ?k; '
                + '               uc:total ?totalPub } '
                + ' FROM <' + globalData.centralGraph + '>  '
                + ' WHERE { '
                + '     SELECT ?keyword ?k (COUNT(DISTINCT(?subject)) AS ?totalPub) '
                + '     WHERE { '
                + '         ?person foaf:publications ?subject. '
                + '         ?subject bibo:Quote ?k . '
                + '         BIND(IRI(?k) AS ?keyword) . '
                + '     } '
                + '     GROUP BY ?keyword ?k '
                + '     HAVING(?totalPub > 2 && ?totalPub < 180) '
                + '     ORDER BY DESC(?totalPub) '
                + '     LIMIT 145'
                + ' } ';
        sparqlQuery.querySrv({query: queryKeywords}, function (rdf) {

            jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                $scope.$apply(function () {
                    //$scope.data = {schema: {"context": context, fields: ["rdfs:label", "uc:total"]}, data: compacted};
                    searchData.allkeywordsCloud = {schema: {"context": globalData.CONTEXT, fields: ["rdfs:label", "uc:total"]}, data: compacted};
                    waitingDialog.hide();
                });
            });
        });
        //***************************************************//


//*****************************************//
//*********FOR TRANSLATE*******************//
        //  $scope.translate = globalData.translateData; //query and load resource related with selected theme
        $scope.$watch('globalData.language', function (newValue, oldValue, scope) {
            //alert($scope.selectedItem);
            $scope.translate = globalData.translateData; //query and load resource related with selected theme
        });



    }]);
