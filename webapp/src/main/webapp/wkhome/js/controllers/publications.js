wkhomeControllers.controller('PublicationsController', ['$scope', '$window', 'globalData', 'sparqlQuery', 'searchData', '$routeParams', '$translate', '$uibModal',
    function ($scope, $window, globalData, sparqlQuery, searchData, $routeParams, $translate, $uibModal) {

        if ($routeParams.authorId) {
            $scope.authorURI = $routeParams.authorId;
        }
        $scope.core = globalData.publicationsCore;
        $scope.puburi = "";
        $scope.showInfo = function (uri, type) {
            $uibModal.open({
                templateUrl: 'wkhome/partials/collectionTemplate.html',
                controller: JournalController,
                resolve: {
                    collections: function () {
                        return {
                            collectionType: type,
                            collectionUri: uri
                        };
                    }
                }
            });
        };



        $scope.showmodal = function () {
            $("#myModal").modal();
        }

        $scope.openmodal = function (puburi) {
            //  $scope.puburi = "https://redi.cedia.edu.ec/resource/publication/080790388cd3adbc37099a6e41b3e163" ;
            $scope.puburi = puburi;
            console.log("Documento");
            console.log(puburi);
            $scope.$broadcast('hijo');


        }

        $scope.openmodal2 = function () {
            $scope.puburi = "https://redi.cedia.edu.ec/resource/publication/a211ec9c3c5da269fbadb4a8bf48b55c";
            $("#myModal").modal();
            $scope.$broadcast('hijo');

        }

         function unique( a) {
            return Array.isArray(a) ? a[0] : a;
        }



        var JournalController = function ($scope, $uibModalInstance, collections, Journal) {
            $scope.journals = [];
            _.forEach(collections.collectionUri, function (uri) {
                Journal.getJournal().get({uri: uri}, function (data) {
                    $scope.journals.push(data.response.docs);
                    $scope.journals = _.flatten($scope.journals);
                });
            });
            $scope.type = collections.collectionType;
            $scope.dialogTitle = "";
        };

        if ($routeParams.authorId) {
            var sparqlAuthor = globalData.PREFIX +
                    "CONSTRUCT {" +
                    "  ?author a foaf:Person ;" +
                    "                    foaf:name ?name ;" +
                    "                    schema:memberOf ?org;" +
                    "                    foaf:img ?img." +
                    "  ?org uc:city ?city ;" +
                    "       uc:province ?province ;" +
                    "       uc:fullName ?fullname ." +
                    "} " +
                    "WHERE{" +
                    "  {" +
                    "   SELECT DISTINCT *" +
                    "    WHERE { " +
                    "      VALUES ?author { <" + $scope.authorURI + "> }" +
                    "      GRAPH <" + globalData.centralGraph + "> {" +
                    "        ?author a foaf:Person ;" +
                    "                           foaf:name ?name ;" +
                    "                           schema:memberOf ?org ." +
                    "        OPTIONAL { ?author foaf:img ?img. }" +
                    "      }" +
                    "      GRAPH <" + globalData.organizationsGraph + "> {" +
                    "        ?org uc:city ?city ;" +
                    "        uc:province ?province ;" +
                    "        uc:fullName ?fullname ." +
                    "        FILTER (lang(?fullname) = '" + $translate.use() + "')" +
                    "      }" +
                    "    }" +
                    "    LIMIT 1" +
                    "  }" +
                    "}";
            // waitingDialog.show("Searching: ");
            sparqlQuery.querySrv({
                query: sparqlAuthor
            }, function (rdf) {
                jsonld.compact(rdf, globalData.CONTEXT, function (error, compacted) {
                    if (!error) {
                        var entity = _.findWhere(compacted["@graph"], {
                            "@type": "foaf:Person"
                        });
                        author = {};
                        author.name = typeof (entity["foaf:name"]) === 'string' ? entity["foaf:name"] : _.first(entity["foaf:name"]);
                        author.photo = entity["foaf:img"] ? unique(entity["foaf:img"]["@id"]) : '/wkhome/images/no_photo.png';
                        author.institutions = [];
                        _.each(entity["schema:memberOf"], function (v) {
                            var provenance = typeof (v) === 'object' ?
                                    _.findWhere(compacted["@graph"], v) :
                                    _.findWhere(compacted["@graph"], {
                                        "@id": v
                                    })
                            author.institutions.push({
                                name: provenance['uc:fullName']['@value'],
                                province: provenance["uc:city"],
                                city: provenance["uc:province"]
                            })
                        });
                        $scope.$apply(function () {
                            $scope.author = author;
                            $scope.puburi = "https://redi.cedia.edu.ec/resource/publication/080790388cd3adbc37099a6e41b3e163";
                        });
                    }
                });
            });
        }

    }
]);
