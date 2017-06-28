
wkhomeControllers.controller('clustersCloud', ['$routeParams', '$scope', '$window', 'sparqlQuery', 'searchData', 'globalData',
    function ($routeParams, $scope, $window, sparqlQuery, searchData, globalData) {
        $scope.todos = [];
        $scope.ctrlFn = function (value)
        {
            $scope.todos = [];
            var model = {};
            _.map(value, function (obj) {
                //var keys = Object.keys(author);
                model["id"] = obj["@id"];
                model["name"] = obj["foaf:name"];
                model["keywords"] = obj["dcterms:subject"];
                /*if (obj["bibo:uri"]) {
                 model["uri"] = obj["bibo:uri"]["@id"];
                 }*/
                if (model["name"] && model["keywords"])
                {
                    $scope.todos.push({id: model["id"], name: model["name"], keywords: model["keywords"], uri: model["id"]});
                }
            });
            $('html,body').animate({
                scrollTop: $("#scrollToHere").offset().top
            }, "slow");
            $scope.loadData();
        };
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
        $scope.$watch('searchData.genericData', function (newValue, oldValue, scope) {
            $scope.data = {schema: {"context": globalData.CONTEXT, fields: ["rdfs:label", "uc:total"]}, data: searchData.genericData};
        });

        $scope.searchAuthor = function (author)
        {
            var getAuthorDataQuery = globalData.PREFIX
                    + ' CONSTRUCT {   <' + author + '> foaf:name ?name; a foaf:Person  '
                    + ' }   '
                    + ' WHERE '
                    + ' {'
                    + 'Graph <' + globalData.centralGraph + '>'
                    + '{'
                    + '     <' + author + '> a foaf:Person.'
                    + '     <' + author + '> foaf:name ?name'

                    + ' } '
                    + '}';

            sparqlQuery.querySrv({query: getAuthorDataQuery}, function (rdf) {
                jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                    $scope.$apply(function () {
                        searchData.authorSearch = compacted;
                        //alert(author);
                        $window.location.hash = "/" + $routeParams.lang + "/w/search?" + author;

                    });
                });
            });
        };
        
        //Function that displays the buttons to export the report
        $scope.exportReport = function (id) {
            $scope.clusterId = id;
            $scope.showRepButtons = true;
        };
        
    }]); //end clusterscloudController