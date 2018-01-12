wkhomeControllers.controller('genericCloud', ['$scope', '$window', 'globalData', 'sparqlQuery', 'searchData',
    function ($scope, $window, globalData, sparqlQuery, searchData) {
        $scope.todos = [];

        $scope.ctrlFn = function (value)
        {
            $scope.todos = [];
            var model = {};
            _.map(value, function (pub) {
                //var keys = Object.keys(author);
                model["id"] = pub["@id"];
                model["title"] = _.isArray(pub["dct:title"]) ? _.first(pub["dct:title"]) : pub["dct:title"];
                model["abstract"] = pub["bibo:abstract"] ? pub["bibo:abstract"] : "";
                model["uri"] = pub["bibo:uri"] ? pub["bibo:uri"]["@id"] : "";
                
                if (model["title"])
                {
                    $scope.todos.push({id: model["id"], title: model["title"], abstract: model["abstract"], uri: model["uri"]});
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

        //Function that displays the buttons to export the report
        $scope.exportReport = function (id) {
            $scope.author = id;
            $scope.showRepButtons = true;
        };

    }]); //end genericcloudController
