wkhomeControllers.controller('exploreAuthor', ['$routeParams', '$scope', '$rootScope', 'globalData', 'searchData', '$window', 'sparqlQuery',
    function ($routeParams, $scope, $rootScope, globalData, searchData, $window, sparqlQuery) {
        $('html,body').animate({
            scrollTop: 0
        }, "slow");

        $scope.authorId = $routeParams.text;
        $scope.publication = undefined;

        var getAuthorInfo = globalData.PREFIX
                            + 'CONSTRUCT { '
                            + '<' + $scope.authorId + '> foaf:name ?name;'
                            + '      a foaf:Person;'
                            + '      foaf:img ?img.'
                            + ' } WHERE { '
                            + '   GRAPH <' + globalData.centralGraph + '> {'
                            + '      <' + $scope.authorId + '> foaf:name ?name.'
                            + '      OPTIONAL{<' + $scope.authorId + '> foaf:img ?img}'
                            + '     }'
                            + ' } LIMIT 1 ';
        sparqlQuery.querySrv({query: getAuthorInfo}, function (rdf) {
            jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                $scope.$apply(function () {
                    $scope.data = compacted;
                });
            });
        });

        clickonRelatedauthor = function (authorId) {
          $window.location.hash = "/" + $routeParams.lang + "/w/author/" + authorId;
        };

        $scope.ifrightClick = function (value) {
           // searchData.genericData = value;
          //  $window.location.hash = "/" + $routeParams.lang + "/w/cloud?" + "datacloud";
            $window.location.hash = "/view/network/" + authorId;
        };

        $scope.clickPublications = function () {
          var uri = encodeURIComponent($scope.authorId);
          $window.location.hash = '/publications/q=author:"' + uri + '"&fl=*&rows=10&wt=json/author/' + $scope.authorId;
        };

        $scope.buildnetworks = function (authorId) {

             $window.location.hash = "/view/network/" + authorId;

            /*var author = _.findWhere($scope.data["@graph"], {"@type": "foaf:Person"});
            if (author["foaf:name"]) {
                var getRelatedAuthors = globalData.PREFIX
                        + 'CONSTRUCT {  <http://ucuenca.edu.ec/wkhuska/resultTitle> a uc:pagetitle. <http://ucuenca.edu.ec/wkhuska/resultTitle> uc:viewtitle "Authors Related With ' + author["foaf:name"] + '"  .         ?subject rdfs:label ?name.         ?subject uc:total ?totalPub   } '
                        + 'WHERE {'
                        + '  {'
                        + '    SELECT ?subject ?name (COUNT(DISTINCT ?relpublication) as ?totalPub)'
                        + '    WHERE {'
                        + '        GRAPH <' + globalData.clustersGraph + '> {'
                        + '          ?cluster foaf:publications ?publication .'
                        + '          ?publication uc:hasPerson <' + author["@id"] + '> .'
                        + '          ?cluster foaf:publications ?relpublication .'
                        + '          ?relpublication uc:hasPerson ?subject .'
                        + '          {'
                        + '            SELECT ?name {'
                        + '              GRAPH <' + globalData.centralGraph + '> { '
                        + '                ?subject foaf:name ?name .'
                        + '              }'
                        + '            }'
                        + '          }'
                        + '          FILTER (?subject != <' + author["@id"] + '>)'
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
            }*/
        }

        $scope.numeroPub = function (publications)
        {
            if (publications != null && (publications.constructor === Array || publications instanceof Array))
                return publications.length;
            else
                return 1;
        }
    }]);
