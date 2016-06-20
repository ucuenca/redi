wkhomeControllers.controller('clusterGroupByCloud', ['$timeout', '$scope', 'globalData', 'sparqlQuery', 'clustersQuery', 'searchData', '$route', '$window',
    function ($timeout, $scope, globalData, sparqlQuery, clustersQuery, searchData, $route, $window) {

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
            $scope.publicationsByKeyword = [];
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

        }//end Load Resources

        function executeDraw(dataToDraw, groupby)
        {
            $timeout(function () {
                $scope.data = [{value: dataToDraw, group: groupby}];
                $scope.dataaux = dataToDraw;
            });
        }


    }]); //end clusterTagsController 
