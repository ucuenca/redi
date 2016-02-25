
wkhomeControllers.controller('searchText', ['$routeParams','$scope', '$window', 'globalData', 'sparqlQuery', 'searchData',
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
        $scope.submit = function () {
            if ($scope.searchText) {
                console.log($scope.searchText);
                waitingDialog.show();
                var queryAuthors = globalData.PREFIX
                        + " CONSTRUCT { "
                        + " ?subject a foaf:Person. "
                        + " ?subject foaf:name ?name } "
                        + " WHERE { "
                        + " { "
                        + "     SELECT DISTINCT (sample(?s) AS ?subject) ?name "
                        + "     WHERE { "
                        + '         GRAPH <' + globalData.centralGraph + '> {'
                        + "             ?s a foaf:Person. "
                        + "             ?s foaf:name ?name."
                        + "             ?s foaf:publications ?pub. "
                        + "             ?pub dct:title ?title. "
                        + '             FILTER(mm:fulltext-search(str(?name), "' + $scope.searchText + '")).'
                        + "     } } "
                        + "     GROUP BY ?name "
                        + "  } "
                        + " }";
//                var filterPath = 'FILTER(CONTAINS(UCASE(?name), "{0}" )) . ';
//                var searchTextt = $scope.searchText.trim();
//                var keywords = searchTextt.split(" ");
//                var filterContainer = "";
//                keywords.forEach(function (val) {
//                    if (val.length > 0) {
//                        filterContainer += String.format(filterPath, val.toUpperCase());
//                    }
//                });
//                queryAuthors = String.format(queryAuthors, filterContainer);
                sparqlQuery.querySrv({query: queryAuthors},
                function (rdf) {
                    jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                        if (compacted["@graph"])
                        {
                            searchData.authorSearch = compacted;
                            $window.location.hash = "/" + $routeParams.lang + "/w/search?" + $scope.searchText;
                        }
                        else
                        {
                            waitingDialog.show();
                            var queryAuthors = globalData.PREFIX
                                    + " CONSTRUCT { ?keywordduri rdfs:label ?k } "
                                    + " WHERE { "
                                    + " { "
                                    + "     SELECT DISTINCT (sample(?keyword) AS ?keywordduri) ?k "
                                    + "     WHERE { "
                                    + '         GRAPH <' + globalData.centralGraph + '> {'
                                    + "         ?s foaf:publications ?pub. "
                                    + "         ?pub bibo:Quote ?k."
                                    + "         BIND(IRI(?k) AS ?keyword) . "
                                    // + "         {0}"
                                    + '         FILTER(mm:fulltext-search(str(?k), "' + $scope.searchText + '")).'
                                    + "     } } "
                                    + "     GROUP BY ?k "
                                    + "  } "
                                    + " }";
//                            var filterPath = 'FILTER(CONTAINS(UCASE(?k), "{0}" )) . ';
//                            var searchTextt = $scope.searchText.trim();
//                            var keywords = searchTextt.split(" ");
//                            var filterContainer = "";
//                            keywords.forEach(function (val) {
//                                if (val.length > 0) {
//                                    filterContainer += String.format(filterPath, val.toUpperCase());
//                                }
//                            });
                            //queryAuthors = String.format(queryAuthors, filterContainer);
                            sparqlQuery.querySrv({query: queryAuthors},
                            function (rdf) {
                                jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                                    if (compacted["@graph"])
                                    {
                                        searchData.areaSearch = compacted;
                                        waitingDialog.hide();
                                        //   $window.location.hash = "w/research-area?" + $scope.searchText;
                                        $window.location.hash = "/" + $routeParams.lang + "/cloud/group-by";
                                    }
                                    else
                                    {
                                        alert("Information not found");
                                        waitingDialog.hide();
                                    }
                                });
                            }); // end of  sparqlQuery.querySrv({...
                        }
                    });
                }); // end of  sparqlQuery.querySrv({...
            }
        };
    }]);