wkhomeControllers.controller('clusterKeywordsCloud', ['$routeParams','$scope', '$window', 'sparqlQuery', 'searchData', 'globalData',
    function ($routeParams, $scope, $window, sparqlQuery, searchData, globalData) {

        searchData.genericData = null;//Para que no aparezcan los datos anteriores y se grafique 2 veces
        $('html,body').animate({
            scrollTop: $("#scrollToTop").offset().top
        }, "1");
        
        $scope.ifClick = function (value)
        {
            searchData.genericData = value;
            $window.location.hash = '/' + $routeParams.lang + "/w/clusters?" + "datacloud";
        };

        $scope.todos = [];

        $scope.loadData = function () {
            $scope.$apply(function () {
                $scope.filteredTodos = []
                        , $scope.currentPage = 1
                        , $scope.numPerPage = 10
                        , $scope.maxSize = 5;
                $scope.$watch('currentPage + numPerPage', function () {
                    var begin = (($scope.currentPage - 1) * $scope.numPerPage)
                            , end = begin + $scope.numPerPage;
                    $scope.filteredTodos = $scope.todos.slice(begin, end);
                });
            });
        };



        if (!searchData.allkeywordsCloud) // if no load data by default
        {
            waitingDialog.show();
            var queryKeywords = globalData.PREFIX
                    + ' CONSTRUCT { ?keyword rdfs:label ?k; uc:total ?totalPub } FROM <' + globalData.centralGraph + '> WHERE { '
                    + ' SELECT ?keyword ?k (COUNT(DISTINCT(?subject)) AS ?totalPub) '
                    + ' WHERE { '
                    + ' ?person foaf:publications ?subject. '
                    + ' ?subject bibo:Quote ?k . '
                    + ' BIND(IRI(?k) AS ?keyword) . } '
                    + ' GROUP BY ?keyword ?k '
                    + ' HAVING(?totalPub > 2 && ?totalPub < 180) '
                    + ' ORDER BY DESC(?totalPub) '
                    + ' LIMIT 145 '
                    //+'ORDER BY DESC(?totalPub) '
                    + '}';
            sparqlQuery.querySrv({query: queryKeywords}, function (rdf) {
                jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                    $scope.$apply(function () {
                        $scope.data = {schema: {"context": globalData.CONTEXT, fields: ["rdfs:label", "uc:total"]}, data: compacted};
                        searchData.allkeywordsCloud = {schema: {"context": globalData.CONTEXT, fields: ["rdfs:label", "uc:total"]}, data: compacted};
                        waitingDialog.hide();
                    });
                });
            });
        }
        else
        {
            $scope.data = searchData.allkeywordsCloud;
        } // end if if (!searchData.allkeywordsCloud)     
    }]);