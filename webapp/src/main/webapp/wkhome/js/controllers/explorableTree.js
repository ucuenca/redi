wkhomeControllers.controller('exploreAuthor', ['$routeParams', '$scope', '$rootScope', 'globalData', 'searchData', '$window', 'sparqlQuery',
    function ($routeParams, $scope, $rootScope, globalData, searchData, $window, sparqlQuery) {
        $('html,body').animate({
            scrollTop: $("#scrollToTop").offset().top
        }, "slow");



        $scope.author = '';
        $scope.authorId = '';
        $rootScope.$on("CallParentMethod", function (author) {
            $scope.clickonRelatedauthor(author);
        });

        $scope.data = '';



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

        searchAuthorInfo = function (author)
        {
            var getAuthorDataQuery = globalData.PREFIX
                    + ' CONSTRUCT {   <' + author + '> foaf:name ?name; '
                    + ' a foaf:Person;  '
                    + ' foaf:publications  ?publications. '
                    + ' ?publications ?predicate ?object. '
                    + ' ?publications uc:contributor ?authorsName '
                    + ' }   '
                    + ' WHERE '
                    + ' {'
                    + '     <' + author + '> foaf:name ?name.'
                    + '     <' + author + '> foaf:publications  ?publications.'
                    + '     ?publications ?predicate ?object. '
                    + '     ?authors foaf:publications ?publications. '
                    + '     ?authors foaf:name ?authorsName.         '
                    //+ '     FILTER (?authorsName != ?name). '
                    + ' } ';

            sparqlQuery.querySrv({query: getAuthorDataQuery}, function (rdf) {
                jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                    $scope.$apply(function () {
                        $scope.author = compacted["@graph"];
                        $scope.authorId = compacted["@graph"][0]['@id'];
                    });
                });
            });
        };

        $scope.numeroPub = function (publications)
        {
            if (publications != null && (publications.constructor === Array || publications instanceof Array))
                return publications.length;
            else
                return 1;
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
                            //$scope.author = $scope.data["@graph"]["@id"];
                            searchAuthorInfo($scope.data["@graph"][0]["@id"]);
                            $('#searchResults').modal('hide');
                        };
                        waitingDialog.hide();
                        $('#searchResults').modal('show');
                    } else {
                        searchData.authorSearch["@graph"] = authorSearch;
                        //$scope.data = searchData.authorSearch;         
                        waitingDialog.hide();
                        //$scope.author = searchData.authorSearch["@graph"]["@id"];
                        searchAuthorInfo(searchData.authorSearch["@graph"][0]["@id"]);
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