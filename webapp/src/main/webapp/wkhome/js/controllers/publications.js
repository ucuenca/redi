wkhomeControllers.controller('publicationsController', ['$scope', '$window', 'globalData', 'sparqlQuery', 'searchData', '$routeParams', '$translate',
    function ($scope, $window, globalData, sparqlQuery, searchData, $routeParams, $translate) {
      $translate.use($routeParams.lang);
      $scope.publications = [];
      $scope.author = {} ;

      var authorURI = $routeParams.authorId;
      var sparqlPublications = globalData.PREFIX
              + " CONSTRUCT { <" + authorURI + "> uc:publication ?publicationUri. "
              + "  <" + authorURI + "> uc:publication ?publicationUri."
              + "  ?publicationUri a bibo:AcademicArticle;"
              + "                    dct:title ?title;"
              + "                    bibo:abstract ?abstract;"
              + "                    bibo:uri ?uri;"
              + "} WHERE {"
              + "  GRAPH  <" + globalData.centralGraph + "> {"
              + "    <" + authorURI + "> foaf:publications ?publicationUri ."
              + "    ?publicationUri dct:title ?title ."
              + "    OPTIONAL { ?publicationUri bibo:abstract  ?abstract.  }  "
              + "    OPTIONAL { ?publicationUri bibo:uri  ?uri.  } "
              + "  }"
              + "}";
              //#ORDER BY DESC(?title)";
      var sparqlAuthor = globalData.PREFIX
              + "CONSTRUCT {"
              + "  <" + authorURI + "> a foaf:Person ;"
              + "                    foaf:name ?name ;"
              + "                    foaf:topic ?area ;"
              + "                    dct:provenance ?provenance;"
              + "                    foaf:img ?img."
              + "  ?provenance uc:city ?city ."
              + "  ?provenance uc:province ?province ."
              + "  ?provenance uc:fullName ?fullname ."
              + "} "
              + "WHERE{"
              + "  {"
              + "  	SELECT *"
              + "    WHERE { "
              + "      GRAPH <" + globalData.centralGraph + "> {"
              + "        <" + authorURI + "> a foaf:Person ;"
              + "                           foaf:name ?name ;"
              + "                           dct:provenance ?provenance ;"
              + "        OPTIONAL { <" + authorURI + "> foaf:topic_interest ?topicURI. }"
              + "        OPTIONAL { <" + authorURI + "> foaf:img ?img. }"
              + "        OPTIONAL { ?topicURI rdfs:label ?area. }"
              + "        {"
              + "          SELECT ?city ?province ?fullname {"
              + "            GRAPH <" + globalData.endpointsGraph + "> {"
              + "              ?provenance uc:city ?city ."
              + "              ?provenance uc:province ?province ."
              + "              ?provenance uc:fullName ?fullname ."
              + "            }"
              + "          }"
              + "        }"
              + "      }"
              + "    }"
              + "    LIMIT 10"
              + "  }"
              + "}";

      waitingDialog.show("Searching: ");

      sparqlQuery.querySrv({query: sparqlPublications}, function (rdf) {
        jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
          if (compacted){
              var entities = _.where(compacted["@graph"], {"@type": "bibo:AcademicArticle"});
              var values = entities.length ? entities : [entities];

                  var model = {};
                  _.map(values, function (pub) {
                      model["id"] = pub["@id"];
                      model["title"] = typeof(pub["dct:title"]) !== 'string' ? _.first(pub["dct:title"]) : pub["dct:title"];
                      model["abstract"] = typeof(pub["bibo:abstract"]) !== 'string' ? _.first(pub["bibo:abstract"]) : pub["bibo:abstract"];
                      model["uri"] = classifyURLS(pub["bibo:uri"]);
                      //model["keywords"] = pub["bibo:Quote"];
                      if (model["title"]) {
                          $scope.publications.push({id: model["id"], title: model["title"], abstract: model["abstract"], uri: model["uri"]});
                      }
                  });

                  $scope.loadData();

              waitingDialog.hide();

          } else {
              waitingDialog.hide();
          }
        });
      });

      sparqlQuery.querySrv({query: sparqlAuthor}, function(rdf) {
        jsonld.compact(rdf, globalData.CONTEXT, function(error, compacted){
          if (!error) {
            var entity = _.findWhere(compacted["@graph"], {"@type": "foaf:Person"});
            $scope.author.name = _.first(entity["foaf:name"]);
            $scope.author.areas = entity["foaf:topic"];
            $scope.author.photo = entity["foaf:img"] ? entity["foaf:img"]["@id"] : undefined;
            $scope.author.institutions = [];
            _.each(entity["dct:provenance"], function(v){
              var provenance = _.findWhere(compacted["@graph"], v)
              var name = location.href.indexOf("/es/")
                        ? _.findWhere(provenance["uc:fullName"], {"@language": "es"})["@value"]
                        : _.findWhere(provenance["uc:fullName"], {"@language": "en"})["@value"];
              $scope.author.institutions.push({name: name, province: provenance["uc:city"], city: provenance["uc:province"]})
            });
          }
        });
      });

      classifyURLS = function (uris){
        var uri = {uri:"", type:""};

        if (Object.prototype.toString.call(uris) === '[object Array]') {
          _.each(uris, function (element){
            uri.uri = element["@id"];

            if (element["@id"].indexOf(".pdf") !== -1) {
              uri.type = "pdf";
              return uri;
            } else {
              uri.type = "web";
            }
          });
        } else if (typeof uris === 'object') {
          uri.uri = uris["@id"];
          uri.type = (uris["@id"].indexOf(".pdf") !== -1) ? "pdf" : "web";
        }

        return uri;
      }

      $scope.loadData = function () {
          $scope.$apply(function () {
              $scope.filterPublications = []
                      , $scope.currentPage = 1
                      , $scope.numPerPage = 10
                      , $scope.maxSize = 5;
              $scope.$watch('currentPage + numPerPage', function () {
                  var begin = (($scope.currentPage - 1) * $scope.numPerPage)
                          , end = begin + $scope.numPerPage;
                  $scope.filterPublications = $scope.publications.slice(begin, end);
              });
          });
      };

      $scope.loadAuthorProfile = function(){
        $window.location.hash = "/" + $routeParams.lang + "/w/search?" + authorURI;
      }

    }]);
