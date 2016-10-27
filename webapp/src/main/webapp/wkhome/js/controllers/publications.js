wkhomeControllers.controller('publicationsController', ['$scope', '$window', 'globalData', 'sparqlQuery', 'searchData', '$routeParams', '$translate',
    function ($scope, $window, globalData, sparqlQuery, searchData, $routeParams, $translate) {
      $translate.use($routeParams.lang);
      $scope.publications = [];
      $scope.author = {} ;

      var authorURI = $routeParams.authorId;
      var sparqlPublications = globalData.PREFIX
              + " CONSTRUCT { <" + authorURI + "> uc:publication ?publicationUri. "
              + " ?publicationUri a bibo:Document. "
              + " ?publicationUri dct:title ?title. "
              + " ?publicationUri bibo:abstract ?abstract. "
              + " ?publicationUri bibo:uri ?uri. "
              + " ?publicationUri bibo:Quote ?keyword. "
              + " } "
              + " WHERE {"
              + " GRAPH <"+globalData.centralGraph+">"
              + " {"
              + " <" + authorURI + "> foaf:publications ?publicationUri ."
              + " ?publicationUri dct:title ?title ."
              + " OPTIONAL { ?publicationUri bibo:abstract  ?abstract.  } "
              + " OPTIONAL { ?publicationUri bibo:uri  ?uri.  } "
              + " OPTIONAL { ?publicationUri bibo:Quote ?keyword. }"
              + " }"
              + "}  ORDER BY DESC(?title)";

      waitingDialog.show("Searching: ");

      sparqlQuery.querySrv({query: sparqlPublications}, function (rdf) {
        jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
          if (compacted){
              var entity = compacted["@graph"];
              var final_entity = _.where(entity, {"@type": "bibo:Document"});
              var values = final_entity.length ? final_entity : [final_entity];


                  var model = {};
                  _.map(values, function (pub) {
                      model["id"] = pub["@id"];
                      model["title"] = pub["dct:title"];
                      model["abstract"] = pub["bibo:abstract"] ? pub["bibo:abstract"] : "";
                      model["uri"] = $scope.findPDF(pub["bibo:uri"]);
                      model["keywords"] = pub["bibo:Quote"];
                      if (model["title"])
                      {
                          $scope.publications.push({id: model["id"], title: model["title"], abstract: model["abstract"], uri: model["uri"], keywords: model['keywords']});
                      }
                  });

                  $scope.loadData();

              waitingDialog.hide();

          } else {
              waitingDialog.hide();
          }
        });
      });

      var sparqlAuthor = globalData.PREFIX
        + "CONSTRUCT {"
        + "  <" + authorURI + "> a foaf:Person ;"
        + "                    foaf:name ?name ;"
        + "                    dct:subject ?area ;"
        + "                    dct:provenance ?provenance ."
        + "  ?provenance uc:city ?city ."
        + "  ?provenance uc:province ?province ."
        + "  ?provenance uc:fullName ?fullname ."
        + "} "
        + "WHERE{"
        + "  {"
        + "  	SELECT *"
        + "    WHERE { "
        + "      GRAPH <http://ucuenca.edu.ec/wkhuska> {"
        + "        <" + authorURI + "> a foaf:Person ;"
        + "                           foaf:name ?name ;"
        + "                           dct:provenance ?provenance ;"
        + "        OPTIONAL { <" + authorURI + "> dct:subject ?area . }"
        + "        {"
        + "          SELECT ?city ?province ?fullname {"
        + "            GRAPH <http://ucuenca.edu.ec/wkhuska/endpoints> {"
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

      sparqlQuery.querySrv({query: sparqlAuthor}, function(rdf) {
        jsonld.compact(rdf, globalData.CONTEXT, function(error, compacted){
          if (!error) {
            var entity = compacted["@graph"];
            $scope.author.name = entity[0]["foaf:name"];
            $scope.author.areas = entity[0]["dct:subject"];
            $scope.author.city = entity[1]["uc:city"];
            $scope.author.province = entity[1]["uc:province"];
            $scope.author.institution = entity[1]["uc:fullName"];
          }
        });
      });

      $scope.findPDF = function (uris){
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
