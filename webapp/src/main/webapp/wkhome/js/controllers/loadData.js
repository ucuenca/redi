/*
 * Controller to load the data the first time
 */

wkhomeControllers.controller('loadData', ['sparqlQuery', 'searchData', '$translate', '$routeParams', '$scope', '$window', 'translateService', 'globalData',
    function (sparqlQuery, searchData, $translate, $routeParams, $scope, $window, translateService, globalData) {

        /**
         * Loading DATA in Memory
         */
        /*************************************************************/
        /*query to get the keywords in memory */
        /*************************************************************/
        loadAllKeyword();
        $scope.themes = [];
        function loadAllKeyword() {
            //only keywords that appear in more than 2 articles
            var queryKeywords = globalData.PREFIX
                    + ' CONSTRUCT { ?keyword rdfs:label ?key } '
                    + '	FROM <' + globalData.centralGraph + '> '
                    + ' WHERE { '
                    + '     SELECT  (count(?key) as ?k) ?key '
                    + '         WHERE { '
                    + '             ?subject foaf:publications ?pub. '
                    + '             ?pub dcterms:subject ?keySub. '
                    + '             ?keySub rdfs:label ?key. '
                    + '             BIND(REPLACE(?key, " ", "_", "i") AS ?unickey). '
                    + '             BIND(IRI(?unickey) as ?keyword) '
                    + '         } '
                    + '     GROUP BY ?keyword  ?key '
                    + '     HAVING(?k > 4) '
                    + '}';
            sparqlQuery.querySrv({query: queryKeywords}, function (rdf) {
                var context = {
                    "rdfs": "http://www.w3.org/2000/01/rdf-schema#",
                };
                jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                    _.map(compacted["@graph"], function (pub) {
                        var model = {};
                        model["id"] = pub["@id"];
                        model["tag"] = pub["rdfs:label"];
                        $scope.themes.push({tag: model["tag"]});
                    });
                    $scope.$apply(function () {
                        searchData.allkeywordsList = $scope.themes;
                        $scope.relatedthemes = searchData.allkeywordsList;
                        $scope.selectedItem = "";
                    });

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
                + '         ?subject dcterms:subject ?keywordSubject. '
                + '         ?keywordSubject rdfs:label ?k. '
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
        //Load clusters
        //***************************************************//

        if (!searchData.dataRequested) {
            loadClusters();
            searchData.dataRequested = true;
        }

        $scope.clusters = [];
        function loadClusters() {
            if (searchData.clustersAuthors == null || searchData.clustersAuthors.length == 0) {
                $scope.clusters = [];
                var authors = [];
                var myArray = new Array();
                var queryClusters = globalData.PREFIX
                        + 'CONSTRUCT {'
                        + '  ?author foaf:name ?name;'
                        + '          uc:hasCluster ?clusterId;'
                        + '          rdfs:label ?label;'
                        + '          dct:subject ?keywords.'
                        + '} WHERE {'
                        + '  GRAPH <' + globalData.clustersGraph + '> {'
                        + '    ?clusterId foaf:publications ?publication;'
                        + '               rdfs:label ?label.'
                        + '    ?publication uc:hasPerson ?author.'
                        + '    GRAPH <' + globalData.centralGraph + '>  {'
                        + '      ?author foaf:name ?name.'
                        + '      ?publication dct:subject [rdfs:label ?keywords].'
                        + '    }'
                        + ' '
                        + '  }'
                        + '}';

                sparqlQuery.querySrv({query: queryClusters}, function (rdf) {

                    jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                        _.map(compacted["@graph"], function (res) {
                            var model = {};
                            var clusterIds = res["uc:hasCluster"];
                            var keywords = "";
                            if (res["dcterms:subject"] != null && (res["dcterms:subject"].constructor === Array || res["dcterms:subject"] instanceof Array)) {
                                for (i = 0; i < res["dcterms:subject"].length && i < 12; i++) {
                                    keywords += (i == 0 ? res["dcterms:subject"][i] : ", " + res["dcterms:subject"][i]);
                                }
                            }
                            if (clusterIds != null && (clusterIds.constructor === Array || clusterIds instanceof Array)) {
                                for (i = 0; i < clusterIds.length; i++) {
                                    model["IdAuthor"] = res["@id"];
                                    model["IdCluster"] = res["uc:hasCluster"][i]["@id"];
                                    model["ClusterName"] = res["rdfs:label"][i];
                                    model["Author"] = res["foaf:name"];
                                    model["Keyword"] = keywords;
                                    model["Title"] = res["foaf:name"];
                                    model["URI"] = res["foaf:name"];
                                    authors.push({idAuthor: model["IdAuthor"], cluster: model["IdCluster"], clusterName: model["ClusterName"], author: model["Author"], keyword: model["Keyword"], title: model["Title"], uri: model["URI"]});
                                }
                            } else {
                                model["IdAuthor"] = res["@id"];
                                model["IdCluster"] = res["uc:hasCluster"]["@id"];
                                model["ClusterName"] = res["rdfs:label"];
                                model["Author"] = res["foaf:name"];
                                model["Keyword"] = keywords;
                                model["Title"] = res["foaf:name"];
                                model["URI"] = res["foaf:name"];
                                authors.push({idAuthor: model["IdAuthor"], cluster: model["IdCluster"], clusterName: model["ClusterName"], author: model["Author"], keyword: model["Keyword"], title: model["Title"], uri: model["URI"]});
                            }
                        });

                        var myArray = new Array();
                        for (i = 0, len = authors.length; i < len; i++) {
                            if (myArray[authors[i]["cluster"].toString()] == null) {
                                myArray[authors[i]["cluster"].toString()] = new Array();
                                myArray[authors[i]["cluster"].toString()][0] = 1;
                                myArray[authors[i]["cluster"].toString()][1] = 0;
                            }
                            myArray[authors[i]["cluster"].toString()][0] = myArray[authors[i]["cluster"].toString()][0] + 1;
                        }

                        //var cont = 1;
                        for (i = 0, len = authors.length; i < len; i++) { //&& cont < 600
                            if (myArray[authors[i]["cluster"].toString()][0] > 4 && myArray[authors[i]["cluster"].toString()][1] < 95) {
                                $scope.clusters.push(authors[i]);
                                //cont+=1;
                                myArray[authors[i]["cluster"].toString()][1] += 1;
                            }
                        }
                        searchData.clustersAuthors = $scope.clusters;
                        searchData.areaSearch = null;
                    });
                });
            }
        }



    }]); //end controller
