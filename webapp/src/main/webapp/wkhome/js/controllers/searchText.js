
wkhomeControllers.controller('searchText', ['$routeParams', '$scope', '$window', 'globalData', 'sparqlQuery', 'searchData',
    function ($routeParams, $scope, $window, globalData, sparqlQuery, searchData) {
        //$scope.sparqlQuery = sparqlQuery;
        String.format = function () {
            // The string containing the format items (e.g. "{0}")
            // will and always has to be the first argument.
            var theString = arguments[0];
            // start with the second argument (i = 1)
            for (var i = 1; i < arguments.length; i++) {
                // "gm" = RegEx options for Global search (more than one instance)
                // and for Multiline search
                var regEx = new RegExp("\\{" + (i - 1) + "\\}", "gm");
                theString = theString.replace(regEx, arguments[i]);
            }
            return theString;
        };

        var queryAuthors = globalData.PREFIX
                + " CONSTRUCT { "
                + " ?s a foaf:Person. "
                + " ?s foaf:name ?name. "
                + " ?s dct:subject ?key. } "
                + " WHERE { "
                + " { "
                + "     SELECT DISTINCT ?s ?name  ?key "
                + "     WHERE { "
                + '         GRAPH <' + globalData.centralGraph + '> {'
                + "             ?s a foaf:Person. "
                + "             ?s foaf:name ?name."
                + "             ?s foaf:publications ?pub. "
                + "             ?pub dct:title ?title. "
                + "             ?s dct:subject ?key"
                + "             {0}"
                + "     } } "
                + "  } "
                + " }";

        $scope.submit = function () {
            if ($scope.searchText) {
                console.log($scope.searchText);
                waitingDialog.show();

                /**
                 * Firts Attempt, search text using fulltext function of marmotta
                 */
                var fulltextFilter = ' FILTER(mm:fulltext-search(str(?name), "' + $scope.searchText + '")).'
                var fulltextqueryAuthors = String.format(queryAuthors, fulltextFilter);
                sparqlQuery.querySrv({query: fulltextqueryAuthors},
                function (rdf) {
                    jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                        if (compacted["@graph"])
                        {
                            waitingDialog.hide();
                            searchData.authorSearch = compacted;
                            $window.location.hash = "/" + $routeParams.lang + "/w/search?" + $scope.searchText;
                        }
                        else
                        {
                            /**
                             * Second Attempt: search text using CONTAINS function of SPARQL 
                             */
                            var filterPath = 'FILTER(CONTAINS(UCASE(?name), "{0}" )) . ';
                            var searchTextt = $scope.searchText.trim();
                            var keywords = searchTextt.split(" ");
                            var filterContainer = "";
                            keywords.forEach(function (val) {
                                if (val.length > 0) {
                                    filterContainer += String.format(filterPath, val.toUpperCase());
                                }
                            });

                            var containerqueryAuthors = String.format(queryAuthors, filterContainer);
                            sparqlQuery.querySrv({query: containerqueryAuthors},
                            function (rdf) {
                                jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                                    if (compacted["@graph"])
                                    {
                                        waitingDialog.hide();
                                        searchData.authorSearch = compacted;
                                        $window.location.hash = "/" + $routeParams.lang + "/w/search?" + $scope.searchText;
                                    }
                                    else
                                    {
                                        /**
                                         * As a last attempt, the text will look for dct:SUBJECT
                                         *  using fulltext
                                         */
                                        var querySearchKeyword = globalData.PREFIX
                                                + " CONSTRUCT { ?keywordduri rdfs:label ?k } "
                                                + " WHERE { "
                                                + " { "
                                                + "     SELECT DISTINCT (sample(?keyword) AS ?keywordduri) ?k "
                                                + "     WHERE { "
                                                + '         GRAPH <' + globalData.centralGraph + '> {'
                                                + "         ?s foaf:publications ?pub. "
                                                + "         ?s dct:subject ?k. "
                                                //+ "         ?pub bibo:Quote ?k."
                                                + "         BIND(IRI(?k) AS ?keyword) . "
                                                + '         FILTER(mm:fulltext-search(str(?k), "' + $scope.searchText + '")).'
                                                + "     } } "
                                                + "     GROUP BY ?k "
                                                + "  } "
                                                + " }";

                                        sparqlQuery.querySrv({query: querySearchKeyword},
                                        function (rdf) {
                                            jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                                                if (compacted["@graph"])
                                                {
                                                    waitingDialog.hide();
                                                    searchData.areaSearch = compacted;
                                                    $window.location.hash = "/" + $routeParams.lang + "/cloud/group-by";
                                                }
                                                else
                                                {
                                                    alert("Information not found");
                                                    waitingDialog.hide();
                                                }
                                            });
                                        }); // end of  sparqlQuery.querySrv({...}) last Attempt                                       
                                    } // end else of last attempt
                                });
                            }); // end of  sparqlQuery.querySrv({...}) of second Attempt   
                        }//end else of second attempt
                    });
                }); // end of  sparqlQuery.querySrv({...}) of firts attempt
            }// end   if ($scope.searchText) {
        };//end $scope.submit = function () {
        
        
        
        
        
        
        /**
         * Loading DATA in Memory
         */
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

    }]);