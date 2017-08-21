wkhomeControllers.controller('searchText', ['$routeParams', '$scope', '$window', 'globalData', 'sparqlQuery', 'searchData', 'searchQueryService', 'SolrSearch',
  function($routeParams, $scope, $window, globalData, sparqlQuery, searchData, searchQueryService, SolrSearch) {
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

    $scope.submit = function() {
      if ($scope.searchText) {
        waitingDialog.show();
        /**
         * Find authors usign solr.
         */
        SolrSearch.get({
          search: $scope.searchText
        }, function(result) {
          if (result.response.docs) {
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
            } else {
              /**
               * As a second attempt, the text will look for dct:SUBJECT
               *  using fulltext
               */
              var querySearchKeyword = globalData.PREFIX
                          + " CONSTRUCT { ?keywordduri rdfs:label ?k } "
                          + " WHERE { "
                          + " { "
                          + "     SELECT DISTINCT (sample(?keyword) AS ?keywordduri) ?k "
                          + "     WHERE { "
                          + '         GRAPH <' + globalData.centralGraph + '> {'
                          + "         ?s foaf:publications [dct:subject ?keyword]."
                          + "         ?keyword rdfs:label ?k."
                          + '         FILTER(mm:fulltext-search(str(?k), "' + $scope.searchText + '")).'
                          + "     } } "
                          + "     GROUP BY ?k "
                          + "  } "
                          + " }";

              sparqlQuery.querySrv({
                  query: querySearchKeyword
                },
                function(rdf) {
                  jsonld.compact(rdf, globalData.CONTEXT, function(err, compacted) {
                    if (compacted["@graph"]) {
                      waitingDialog.hide();
                      searchData.areaSearch = compacted;
                      $window.location.hash = "/" + $routeParams.lang + "/cloud/group-by";
                    } else {
                      var params = {
                        textSearch: $scope.searchText
                      };
                      searchQueryService.querySrv(params, function(response) {
                        var res = '';
                        for (var i = 0; i < Object.keys(response).length - 2; i++) {
                          res += response[i];
                        }
                        if (res && res !== '' && res !== 'undefinedundefinedundefinedundefined') {
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
                            }); // end of  sparqlQuery.querySrv({...}) fourth Attempt
                        }
                      });
                    }
                  });
                }); // end of  sparqlQuery.querySrv({...}) third Attempt
            }
          }
        });


                      }
              }
  }
]);
