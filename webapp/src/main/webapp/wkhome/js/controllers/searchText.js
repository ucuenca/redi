wkhomeControllers.controller('searchText', ['$routeParams', '$scope', '$window', 'globalData', 'sparqlQuery', 'searchData', 'searchQueryService', '$location', 'AuthorsService', 'KeywordsService', 'PublicationsService','searchTextResultsService',
  function($routeParams, $scope, $window, globalData, sparqlQuery, searchData, searchQueryService, $location, AuthorsService, KeywordsService, PublicationsService, searchTextResultsService) {
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

    

    function Candidate(id, val, desc, path) {
      this.id = id;
      this.val = val;
      this.desc = desc;
      this.path = path;
    }

    var queryPublications = globalData.PREFIX +
      "CONSTRUCT {" +
      "  ?keyword uc:publication ?publicationUri." +
      "  ?publicationUri dct:contributors ?subject . " +
      "  ?subject foaf:name ?name ." +
      "  ?subject a foaf:Person . " +
      "  ?publicationUri a bibo:Document. " +
      "  ?publicationUri dct:title ?title." +
      "  ?publicationUri bibo:abstract ?abstract. " +
      "  ?publicationUri bibo:uri ?uri. " +
      "} WHERE { " +
      "  GRAPH <" + globalData.centralGraph + "> { " +
      " {0} " +
      "    ?subject foaf:publications ?publicationUri . " +
      "    ?subject foaf:name ?name . " +
      "    ?publicationUri dct:title ?title . " +
      "    OPTIONAL{ ?publicationUri bibo:abstract ?abstract. } " +
      "    OPTIONAL{ ?publicationUri bibo:uri ?uri. }" +
      "  }" +
      "}";

    $scope.submit = function() {
      if ($scope.searchText) {
        waitingDialog.show();
        AuthorsService.get({
          search: $scope.searchText
        }, function(result) {
          if (result.response.docs.length > 0) {
            var authors = result.response.docs;
            if (authors.length > 1) {
              var path = "/w/author/";
              var candidates = _.map(authors, function(author) {
                var id = author["lmf.uri"];
                var name = _.max(author.name, function(name) {
                  return name.length
                });
                var topics = _.chain(author.topics)
                  .uniq()
                  .first(10)
                  .value()
                  .join(", ");
                var candidate = new Candidate(id, name, topics, path);

                return candidate;
              });
              $scope.candidates = candidates;
              searchTextResultsService.saveData(candidates);
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
              var keywords = _.uniq(result.response.docs, function(item) {
                return item['lmf.uri'];
              });
              if (keywords.length > 1) {
                var candidates = _.map(keywords, function(keyword) {
                  var label = _.first(keyword.keyword);
                  var candidate = new Candidate(label, label, undefined, "/cloud/group-by?area=");
                  return candidate;
                });
                $scope.candidates = candidates;
                searchTextResultsService.saveData(candidates);
                waitingDialog.hide();
                $('#searchResults').modal('show');
              } else if (keywords.length === 1) {
                var keyword = _(keywords[0].keyword).first();
                waitingDialog.hide();
                $location.path($routeParams.lang + "/cloud/group-by/").search({
                  area: keyword
                });
                $window.location.hash = "/" + $routeParams.lang + "/cloud/group-by?area=" + keyword;
              } else {
                PublicationsService.get({
                  search: $scope.searchText
                }, function(result) {
                  if (result.response.docs.length > 0) {
                    var publications = result.response.docs;
                    var template = " <{0}> ";
                    var search = "values ?publicationUri {";
                    for (i = 0; i < publications.length; i++) {
                      search += String.format(template, publications[i]["lmf.uri"]);
                    }
                    search += '}';
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
