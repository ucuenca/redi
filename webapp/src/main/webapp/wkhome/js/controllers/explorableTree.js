wkhomeControllers.controller('exploreAuthor', ['$routeParams', '$scope', '$rootScope', 'globalData', 'searchData', '$window', 'sparqlQuery',
    function ($routeParams, $scope, $rootScope, globalData, searchData, $window, sparqlQuery) {
        $('html,body').animate({
            scrollTop: 0
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

        $scope.buildnetworks = function (){


                  var author = _.findWhere($scope.data["@graph"], {"@type": "foaf:Person"});
                  if (author["foaf:name"]) {
                    var getRelatedAuthors = globalData.PREFIX
                            + 'CONSTRUCT {  <http://ucuenca.edu.ec/wkhuska/resultTitle> a uc:pagetitle. <http://ucuenca.edu.ec/wkhuska/resultTitle> uc:viewtitle "Authors Related With '+ author["foaf:name"] +'"  .         ?subject rdfs:label ?name.         ?subject uc:total ?totalPub   } '
                            + 'WHERE {'
                            + '  {'
                            + '    SELECT ?subject ?name (COUNT(DISTINCT ?relpublication) as ?totalPub)'
                            + '    WHERE {'
                            + '        GRAPH <'+ globalData.clustersGraph +'> {'
                            + '          ?cluster foaf:publications ?publication .'
                            + '          ?publication uc:hasPerson <'+ author["@id"] +'> .'
                            + '          ?cluster foaf:publications ?relpublication .'
                            + '          ?relpublication uc:hasPerson ?subject .'
                            + '          {'
                            + '            SELECT ?name {'
                            + '              GRAPH <' + globalData.centralGraph +'> { '
                            + '                ?subject foaf:name ?name .'
                            + '              }'
                            + '            }'
                            + '          }'
                            + '          FILTER (?subject != <'+ author["@id"] +'>)'
                            + '        }'
                            + '    }'
                            + '    GROUP BY ?subject ?name'
                            + '  }'
                            + '}';
                    waitingDialog.show("Loading Authors Related with " + author["foaf:name"]);
                    sparqlQuery.querySrv({query: getRelatedAuthors}, function (rdf) {

                        jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                            if (compacted && compacted.hasOwnProperty("@graph")) {
                                $scope.ifrightClick(compacted);
                                waitingDialog.hide();
                            } else {
                                waitingDialog.hide();
                            }
                        });
                    });
                  }
        }

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
                            model["keyword"] = "";

                            author["dct:subject"] instanceof Array ?
                                    _.map(author["dct:subject"], function (eachsubject, idx) {
                                        if (idx < 5) {
                                            model["keyword"] = model["keyword"] + eachsubject + ", ";
                                        }

                                    }) : model["keyword"] = author["dct:subject"];
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
