wkhomeControllers.controller('exploreAuthor', ['$routeParams','$scope', '$rootScope', 'globalData', 'searchData', '$window', 'sparqlQuery',
    function ($routeParams, $scope, $rootScope, globalData, searchData, $window, sparqlQuery) {

         $rootScope.$on("CallParentMethod", function (author) {
            $scope.clickonRelatedauthor(author);
        });

        $scope.data = '';

        $('html,body').animate({
            scrollTop: $("#scrollToHere").offset().top
        }, "slow");

        clickonRelatedauthor = function (author)
        {
            var getAuthorDataQuery = globalData.PREFIX
                    + ' CONSTRUCT {   <' + author + '> foaf:name ?name; a foaf:Person  '
                    + ' }   '
                    + ' WHERE '
                    + ' {'
                    + '     <' + author + '> foaf:name ?name'
                    + ' } ';

            sparqlQuery.querySrv({query: getAuthorDataQuery}, function (rdf) {
                jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                    $scope.$apply(function () {

                        $scope.data = compacted;
                    });
                });
            });


        };
        $scope.ifrightClick = function (value)
        {
            searchData.genericData = value;
            $window.location.hash = "/" + $routeParams.lang + "/w/cloud?" + "datacloud";
        };

        if (searchData.authorSearch != null && searchData.authorSearch["@graph"].length == 1) {
            //$scope.data = searchData.authorSearch;
            clickonRelatedauthor(searchData.authorSearch["@graph"][0]["@id"]);
        }

        $scope.$watch('searchData.authorSearch', function (newValue, oldValue, scope) {

            if (searchData.authorSearch) {
                var authorSearch = searchData.authorSearch["@graph"];
                if (authorSearch) {
                    if (authorSearch.length > 1) {
                        var candidates = _.map(authorSearch, function (author) {
                            var model = {};
                            //var keys = Object.keys(author);
                            model["id"] = author["@id"];
                            model["name"] = author["foaf:name"];
                            return model;
                        });
                        $scope.candidates = candidates;

                        /*if (searchData.authorSearch["@graph"].length === 1)
                         {
                         $scope.data = searchData.authorSearch;
                         }*/

                        $scope.selectedAuthor = function ($event, uri) {
                            searchData.authorSearch["@graph"] = _.where(authorSearch, {"@id": uri});
                            //$scope.data = _.where(authorSearch, {"@id": uri});
                            $scope.data = searchData.authorSearch;
                            $('#searchResults').modal('hide');
                        };
                        waitingDialog.hide();
                        $('#searchResults').modal('show');
                    } else {
                        searchData.authorSearch["@graph"] = authorSearch;
                        //$scope.data = searchData.authorSearch;         
                        waitingDialog.hide();
                    }
                }//End if(authorSearch)
                else
                {
                    alert("Information not found");
                    $window.location.hash = "/";
                    waitingDialog.hide();
                }
            }
        }, true);
    }]); // end exploreAuthor