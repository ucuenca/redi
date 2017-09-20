wkhomeControllers.controller('searchText', ['$routeParams', '$scope', '$window', 'globalData', 'sparqlQuery', 'searchData', 'searchQueryService', 'AuthorsService', 'KeywordsService','PublicationsService',
  function($routeParams, $scope, $window, globalData, sparqlQuery, searchData, searchQueryService, AuthorsService, KeywordsService,PublicationsService) {
    String.format = function() {
      // The string containing the format items (e.g. "{0}")
      // will and always has to be the first argument.
      var theString = arguments[0];
      // start with the second argument (i = 1)
      for (var i = 1; i < arguments.length; i++) {
        // "gm" = RegEx options for Global search (more than one instance)
        // and for Multiline search
        var regEx = new RegExp("\\{" + (i - 1) + "\\}", "gm");
        theString = theString.replace(regEx, arguments[i]);
      }
      return theString;
    };

    var queryPublications = globalData.PREFIX
                          + "CONSTRUCT {"
                          + "  ?keyword uc:publication ?publicationUri."
                          + "  ?publicationUri dct:contributors ?subject . "
                          + "  ?subject foaf:name ?name ."
                          + "  ?subject a foaf:Person . "
                          + "  ?publicationUri a bibo:Document. "
                          + "  ?publicationUri dct:title ?title."
                          + "  ?publicationUri bibo:abstract ?abstract. "
                          + "  ?publicationUri bibo:uri ?uri. "
                          + "} WHERE { "
                          + "  GRAPH <" + globalData.centralGraph + "> { "
                          + " {0} "
                          + "    ?subject foaf:publications ?publicationUri . "
                          + "    ?subject foaf:name ?name . "
                          + "    ?publicationUri dct:title ?title . "
                          + "    OPTIONAL{ ?publicationUri bibo:abstract ?abstract. } "
                          + "    OPTIONAL{ ?publicationUri bibo:uri ?uri. }"
                          + "  }"
                          + "}";

    $scope.submit = function() {
      if ($scope.searchText) {
        waitingDialog.show();
        AuthorsService.get({
          search: $scope.searchText
        }, function(result) {
          if (result.response.docs.length > 0) {
            var authors = result.response.docs;
            if (authors.length > 1) {
              var candidates = _.map(authors, function(author) {
                var model = {};
                model["id"] = author["lmf.uri"];
                model["name"] = _.max(author.name, function(name) {
                  return name.length
                });
                model["keyword"] = _.chain(author.topics)
                  .uniq()
                  .first(10)
                  .value()
                  .join(", ");
                return model;
              });
              $scope.candidates = candidates;
              $scope.selectedAuthor = function($event, uri) {
                $('#searchResults').modal('hide');
                $('#searchResults').on('hidden.bs.modal', function() {
                  $window.location.hash = "/" + $routeParams.lang + "/w/author/" + uri;
                });
              };
              waitingDialog.hide();
              $('#searchResults').modal('show');
            } else if (authors.length === 1) {
              var authorId = authors[0]["lmf.uri"];
              waitingDialog.hide();
              $window.location.hash = "/" + $routeParams.lang + "/w/author/" + authorId;
            }
          } else {
            KeywordsService.get({
              search: $scope.searchText
            }, function(result) {
              if (result.response.docs.length > 0) {
                var keywords = result.response.docs;
                var candidates = _.map(keywords, function(keyword) {
                  var model = {};
                  model["id"] = keyword["lmf.uri"];
                  model["label"] = _.first(keyword.keyword);
                  return model;
                });
                waitingDialog.hide();
                searchData.areaSearch = candidates;
                // $location.url("/" + $routeParams.lang + "/cloud/group-by");
                $window.location.hash = "/" + $routeParams.lang + "/cloud/group-by";
              } else {
                PublicationsService.get({
                  search: $scope.searchText
                }, function(result) {
                  if (result.response.docs.length > 0) {
                    var publications = result.response.docs;
                    var template = " BIND(<{0}> as ?publicationUri) ";
                    var search = String.format(template, publications[0]["lmf.uri"]);
                    for(i=1; i<publications.length; i++){
                      search = search + "UNION" + String.format(template, publications[i]["lmf.uri"]);
                    }
                    var res = String.format(queryPublications, search);
                    sparqlQuery.querySrv({
                        query: res
                      },
                      function(rdf) {
                        jsonld.compact(rdf, globalData.CONTEXT, function(err, compacted) {
                          if (compacted["@graph"]) {
                            waitingDialog.hide();
                            searchData.publicationsSearch = compacted;
                            $window.location.hash = "/" + $routeParams.lang + "/w/listAllText";
                          } else {
                            if ($routeParams.lang === "es") {
                              alert("La información no se encuentra disponible en REDI en este momento.");
                            } else {
                              alert("The information is not available in REDI at the moment. Please try again later.");
                            }
                            waitingDialog.hide();
                          }
                        });
                      });
                  } else {
                    if ($routeParams.lang === "es") {
                      alert("La información no se encuentra disponible en REDI en este momento.");
                    } else {
                      alert("The information is not available in REDI at the moment. Please try again later.");
                    }
                    waitingDialog.hide();
                  }
                });
              }
            });
          }
        });
      }
    }
  }
]);
