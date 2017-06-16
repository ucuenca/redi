
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

        //var queryAuthors = globalData.PREFIX
        //        + " CONSTRUCT { "
        //        + " ?s a foaf:Person. "
        //        + " ?s foaf:name ?name. "
        //        + " ?s dct:subject ?key. } "
        //        + " WHERE { "
        //        + " { "
        //        + "     SELECT DISTINCT ?s ?name  ?key "
        //        + "     WHERE { "
        //        + '         GRAPH <' + globalData.centralGraph + '> {'
        //        + "             ?s a foaf:Person. "
      //          + "             ?s foaf:name ?name."
        //        + "             ?s foaf:publications ?pub. "
        //        + "             ?pub dct:title ?title. "
        //        + "             optional { ?s dct:subject ?key }"
        //        + "             {0}"
        //        + "     } } "
      //          + "  } "
      //          + " }";
        var queryAuthors = globalData.PREFIX
          + "CONSTRUCT { ?s a foaf:Person; foaf:name ?name; dct:subject ?key; foaf:img ?img. }"
          + "FROM <" + globalData.centralGraph + ">"
          + "WHERE {"
          + "  ?s a foaf:Person;"
          + "       foaf:name ?name;"
          + "       foaf:publications []."
          + "  OPTIONAL { ?s foaf:topic_interest ?topic }"
          + "  OPTIONAL { ?topic rdfs:label ?key.} "
          + "  OPTIONAL { ?s foaf:img   ?img }"
          + "  {0}"
          + "}";

        $scope.submit = function () {
            if ($scope.searchText) {
                console.log($scope.searchText);
                waitingDialog.show();

                /**
                 * Firts Attempt, search text using fulltext function of marmotta
                 */
                var fulltextFilter = ' FILTER(mm:fulltext-search(str(?name), "' + $scope.searchText + '", "es")).'
                var fulltextqueryAuthors = String.format(queryAuthors, fulltextFilter);
                sparqlQuery.querySrv({query: fulltextqueryAuthors},
                function (rdf) {
                    jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                        if (compacted["@graph"]){
                            waitingDialog.hide();
                            searchData.authorSearch = compacted;
                            $window.location.hash = "/" + $routeParams.lang + "/w/search?" + $scope.searchText;
                        } else {
                            /**
                             * Second Attempt: search text using CONTAINS function of SPARQL
                             */
                            var filterPath = 'FILTER(CONTAINS(UCASE(?name), "{0}" )) . ';
                            var searchTextt = $scope.searchText.trim();
                            var keywords = searchTextt.split(" ");
                            /*
                            var middle = Math.ceil((keywords.length)/2);
                            var words = '"' + keywords[0];
                            for (i = 1; i < keywords.length; i++) {
                                if (i < middle) {
                                    words = words + " " + keywords[i] + ((i+1) == middle? '" "': '');
                                } else {
                                    words = keywords[i];
                                }
                            }
                            */
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
                                                //+ "         ?s dct:subject ?k. "
                                                + "         ?pub bibo:Quote ?k."
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

    }]);
