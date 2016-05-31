wkhomeControllers.controller('exploreresearchArea', ['$routeParams','$scope', '$rootScope', 'searchData', '$window', '$route',
    function ($routeParams, $scope, $rootScope, searchData, $window) {
        $scope.ifrightClick = function (value)
        {
            searchData.genericData = value;
            $window.location.hash = "/" + $routeParams.lang + "/w/cloud?" + "datacloud";
        };
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
                        $scope.$destroy();
                        $scope.$apply();
                        searchData.researchArea = label;
                        $window.location.hash = "cloud/group-by";
                    };
                    waitingDialog.hide();
                    $('#searchResults').modal('show');
//                        } else {
//                        searchData.authorSearch["@graph"] = authorSearch;
//                        $scope.data = searchData.authorSearch;         
//                        waitingDialog.hide();
//                    }
                }//End if(authorSearch)
                else
                {
                    alert("Information not found");
                    $window.location.hash = "/";
                    waitingDialog.hide();
                }
            }

        }, true);
    }]); // end exploreresearchArea