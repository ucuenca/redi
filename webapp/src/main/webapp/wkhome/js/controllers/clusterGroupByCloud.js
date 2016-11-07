wkhomeControllers.controller('clusterGroupByCloud', ['$timeout', '$scope', 'globalData', 'sparqlQuery', 'clustersQuery', 'searchData', '$route', '$window', "$routeParams",
    function ($timeout, $scope, globalData, sparqlQuery, clustersQuery, searchData, $route, $window, $routeParams) {

        $('html,body').animate({
            scrollTop: $("#scrollToTop").offset().top
        }, "slow");

        $scope.gbselectedItem = 'cluster';

        $scope.$watch('searchData.areaSearch', function (newValue, oldValue, scope) {

            if (searchData.areaSearch) {
                var areaSearch = searchData.areaSearch["@graph"];
                if (areaSearch) {
                    //    if (authorSearch.length > 1) {
                    var candidates = _.map(areaSearch, function (area) {
                        var model = {};
                        //var keys = Object.keys(author);
                        model["id"] = area["@id"];
                        model["label"] = area["rdfs:label"];
                        return model;
                    });
                    $scope.candidates = candidates;
                    $scope.selectedAuthor = function ($event, label) {
                        $('#searchResults').modal('hide');
                        searchData.researchArea = label;
                        $scope.selectedItem = label;
                    };
                    waitingDialog.hide();
                    $('#searchResults').modal('show');
                }//End if(authorSearch)
                else
                {
                    alert("Information not found");
                    $window.location.hash = "/";
                    waitingDialog.hide();
                }

            }

        }, true);
        if (!searchData.allkeywords)
        {
            $scope.themes = [];
            waitingDialog.show();
            executeGroupTags();
            function executeGroupTags() {

                //only keywords that appear in more than 2 articles
                var queryKeywords = globalData.PREFIX
                        + ' CONSTRUCT { ?keyword rdfs:label ?key } '
                        + '	FROM <' + globalData.centralGraph + '> '
                        + ' WHERE { '
                        + '     SELECT  (count(?key) as ?k) ?key '
                        + '     WHERE { '
                        + '         ?subject bibo:Quote ?key. '
                        + '         BIND(REPLACE(?key, " ", "_", "i") AS ?unickey). '
                        + '         BIND(IRI(?unickey) as ?keyword) '
                        + '     } '
                        + '     GROUP BY ?keyword  ?key '
                        + '     HAVING(?k > 10) '
                        + '}';
                sparqlQuery.querySrv({query: queryKeywords}, function (rdf) {
                    waitingDialog.show();
                    jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {

                        _.map(compacted["@graph"], function (pub) {
                            var model = {};
                            model["id"] = pub["@id"];
                            model["tag"] = pub["rdfs:label"];
                            $scope.themes.push({tag: model["tag"]});
                        });
                        applyvalues();
                        waitingDialog.hide();
                    });
                });
            }
            function applyvalues() {
                $scope.$apply(function () {
                    $scope.relatedthemes = $scope.themes;
                    $scope.selectedItem = searchData.researchArea; // Selected Research Area Filter Default
                    searchData.allkeywords = $scope.themes;
                });
            }
            ;
        }
        else
        {
            $scope.relatedthemes = searchData.allkeywords;
            $scope.selectedItem = searchData.researchArea;
        } //  end  if (!searchData.allkeywords)



        $scope.$watch('gbselectedItem', function () {
            groupByResources($scope.dataaux, $scope.gbselectedItem);
        });
        $scope.$watch('selectedItem', function () {
            //alert($scope.selectedItem);
            loadResources($scope.selectedItem, $scope.gbselectedItem); //query and load resource related with selected theme
        });
        function groupByResources(values, groupby)//grouByResources resources by ...
        {
            // executeDraw(values,groupby);
            //this activity is cheking directly in cloudGroup.js
        }//end grouByResources

        function loadResources(value, groupby)//load resources related with selected keyword
        {
            var clusters = [];
            var authors = [];
            var myArray = new Array();
            if (searchData.clustersAuthors == null || searchData.clustersAuthors.length == 0) {
                searchData.clustersAuthors = [];

                var queryClusters = globalData.PREFIX
                  + 'CONSTRUCT {?author foaf:name ?name. ?author uc:hasCluster ?clusterId. ?author rdfs:label ?label. ?author bibo:Quote ?keywords }'
                  + 'WHERE{ '
                  + '  { '
                  + '    SELECT DISTINCT ?author ?name ?clusterId ?label (group_concat(DISTINCT ?keyword; separator = ", ") as ?keywords) '
                  + '    WHERE {'
                  + '      GRAPH <' + globalData.clustersGraph + '> { '
                  + '        {'
                  + '          SELECT ?clusterId '
                  + '          WHERE{'
                  + '            ?clusterId foaf:publications  ?pub'
                  + '          } GROUP BY ?clusterId'
                  + '            HAVING (COUNT(?pub) > 20)'
                  + '        }'
                  + '        ?clusterId rdfs:label ?label . '
                  + '        ?clusterId foaf:publications  ?publication . '
                  + '        ?publication uc:hasPerson ?author .'
                  + '        {'
                  + '          SELECT * {'
                  + '            GRAPH <' + globalData.centralGraph + '> {'
                  + '                ?author foaf:name ?name .'
                  + '                ?publication bibo:Quote ?keyword.'
                  + '            }'
                  + '          } '
                  + '        }'
                  + '      } '
                  + '    } '
                  + '    GROUP BY ?author ?name ?clusterId ?label'
                  + '  } '
                  + '}';

                sparqlQuery.querySrv({query: queryClusters}, function (rdf) {

                    jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                        _.map(compacted["@graph"], function (res) {
                            var model = {};
                            var clusterIds = res["uc:hasCluster"];
                            var keywords = "";
                            /** Keywords come aggregated, so it not necessary to aggregate again.
                            if (res["bibo:Quote"] != null && (res["bibo:Quote"].constructor === Array || res["bibo:Quote"] instanceof Array)) {
                                for (i = 0; i < res["bibo:Quote"].length && i < 12; i++) {
                                    keywords += (i == 0 ? res["bibo:Quote"][i] : ", " + res["bibo:Quote"][i]);
                                }
                            }**/
                            keywords = res["bibo:Quote"];
                            if (clusterIds != null && (clusterIds.constructor === Array || clusterIds instanceof Array)) {
                                for (i = 0; i < clusterIds.length; i++) {
                                    model["IdAuthor"] = res["@id"];
                                    model["IdCluster"] = res["uc:hasCluster"][i]["@id"];
                                    model["ClusterName"] = res["rdfs:label"][i];
                                    model["Author"] = res["foaf:name"];
                                    model["Keyword"] = keywords[i];
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
                                clusters.push(authors[i]);
                                //cont+=1;
                                myArray[authors[i]["cluster"].toString()][1] += 1;
                            }
                        }
                        searchData.clustersAuthors = clusters;
                        $timeout(executeDraw(searchData.clustersAuthors, groupby));
                        searchData.areaSearch = null;

                    });
                });
            } else {
                $timeout(executeDraw(searchData.clustersAuthors, groupby));
                searchData.areaSearch = null;
            }
           /*
            clustersQuery.success(function (data) {
                $scope.clusters = data;
                var myArray = new Array();
                for (i = 0, len = data.length; i < len; i++) {
                    if (myArray[data[i].cluster.toString()] == null) {
                        myArray[data[i].cluster.toString()] = new Array();
                        myArray[data[i].cluster.toString()][0] = 1;
                        myArray[data[i].cluster.toString()][1] = 0;
                    }
                    myArray[data[i].cluster.toString()][0] = myArray[data[i].cluster.toString()][0] + 1;
                }
                var cont = 1;
                for (i = 0, len = data.length; i < len ; i++) { //&& cont < 600
                    if (myArray[data[i].cluster.toString()][0] > 4 && myArray[data[i].cluster.toString()][1] < 95) {
                        var model = {};
                        model["Cluster"] = data[i].cluster;
                        model["Author"] = data[i].author;
                        model["Keyword"] = data[i].kw;
                        model["Title"] = data[i].title.toString();
                        model["URI"] = data[i].uriPublication;
                        //$timeout(function () {
                            $scope.publicationsByKeyword.push({cluster: model["Cluster"], author: model["Author"], keyword: model["Keyword"], title: model["Title"], uri: model["URI"]});
                            cont+=1;
                        //});
                        myArray[data[i].cluster.toString()][1] += 1;
                    }
                }

                $timeout(executeDraw($scope.publicationsByKeyword, groupby));
                searchData.areaSearch = null;

            });
             */
        }//end Load Resources

        $scope.clickonAuthor = function (id_author)
        {
            clickonRelatedauthor(id_author);
        }; //end clickonAuthor

        clickonRelatedauthor = function (id_author)
        {
            var getAuthorDataQuery = globalData.PREFIX
                    + ' CONSTRUCT {   <' + id_author + '> foaf:name ?name; a foaf:Person  '
                    + ' }   '
                    + ' WHERE '
                    + ' {'
                    + 'Graph <' + globalData.centralGraph + '>'
                    + '{'
                    + '     <' + id_author + '> a foaf:Person.'
                    + '     <' + id_author + '> foaf:name ?name'
                    + ' } '
                    + '}';

            sparqlQuery.querySrv({query: getAuthorDataQuery}, function (rdf) {
                jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                    $scope.$apply(function () {
                        searchData.authorSearch = compacted;
                        //alert(author);
                        $window.location.hash = "/" + $routeParams.lang + "/w/search?" + id_author;
                    });
                });
            });
        }; //end clickonRelatedauthor

        function executeDraw(dataToDraw, groupby)
        {
            $timeout(function () {
                $scope.data = [{value: dataToDraw, group: groupby}];
                $scope.dataaux = dataToDraw;
            });
        }


    }]); //end clusterTagsController
