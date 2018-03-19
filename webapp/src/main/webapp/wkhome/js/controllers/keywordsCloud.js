wkhomeControllers.controller('keywordsCloud', ['$translate', '$routeParams', '$scope', 'globalData', 'sparqlQuery', 'searchData', '$window', 'Statistics',
  function($translate, $routeParams, $scope, globalData, sparqlQuery, searchData, $window, Statistics) {
    $("html, body").animate({
      scrollTop: 0
    }, 'slow', 'swing');

    Statistics.query({
      id: 'keywords_frequencypub_gt4'
    }, function(data) {
      $scope.areas = [];
      _.map(data["@graph"], function(keyword) {
        $scope.areas.push({
          tag: keyword["rdfs:label"]["@value"]
        });
      });
    });

    // if (!searchData.allkeywordsList) {
    //   $scope.themes = [];
    //   var queryKeywords = globalData.PREFIX
    //     + 'CONSTRUCT { ?kw_ rdfs:label ?key } 	'
    //     + ' WHERE {      '
    //     + '  SELECT ?kw_ (COUNT(DISTINCT ?p) as ?tot) (SAMPLE(?kw) as ?key)'
    //     + '  WHERE {              '
    //     + '    GRAPH <' + globalData.centralGraph + '> {'
    //     + '      ?author foaf:publications ?p.'
    //     + '      ?p dct:subject ?kw_.              '
    //     + '      ?kw_ rdfs:label ?kw.              '
    //     + '      '
    //     + '    }'
    //     + '  } GROUP BY ?kw_     '
    //     + '}';
    //   sparqlQuery.querySrv({
    //     query: queryKeywords
    //   }, function(rdf) {
    //     var context = {
    //       "rdfs": "http://www.w3.org/2000/01/rdf-schema#",
    //     };
    //     jsonld.compact(rdf, globalData.CONTEXT, function(err, compacted) {
    //       _.map(compacted["@graph"], function(pub) {
    //         var model = {};
    //         model["id"] = pub["@id"];
    //         model["tag"] = pub["rdfs:label"];
    //         $scope.themes.push({
    //           tag: model["tag"]
    //         });
    //       });
    //       $scope.$apply(function() {
    //         searchData.allkeywordsList = $scope.themes;
    //         $scope.relatedthemes = searchData.allkeywordsList;
    //         $scope.selectedItem = "";
    //       });
    //
    //     });
    //   });
    // } else {
    //   $scope.relatedthemes = searchData.allkeywordsList;
    //   $scope.selectedItem = "";
    // }

    /**
     * Search for areas...
     */
    $scope.querySearch = function(query) {
      return query ? searchData.allkeywordsList.filter(createFilterFor(query)) : searchData.allkeywordsList;
    };

    /**
     * Create filter function for a query string
     */
    function createFilterFor(query) {
      var lowercaseQuery = angular.lowercase(query);
      return function filterFn(area) {
        return (angular.lowercase(area.tag).indexOf(lowercaseQuery) !== -1);
      };
    }

    $scope.clickonAuthor = function(id_author) {
      clickonRelatedauthor(id_author);
    }; //end clickonAuthor

    clickonRelatedauthor = function(id_author) {
      $window.location.hash = "/" + $routeParams.lang + "/w/author/" + id_author;
    }; //end clickonRelatedauthor

    $scope.todos = [];
    $scope.ctrlFn = function(value) {

      var publicaciones = _.where(value, {
        "@type": "bibo:Document"
      });
      var autores = _.where(value, {
        "@type": "foaf:Person"
      });

      $scope.todos = [];
      $scope.autores = [];
      var model = {};
      _.map(publicaciones, function(pub) {
        //var keys = Object.keys(author);

        model["id"] = pub["@id"];
        model["title"] = typeof pub["dct:title"] === 'string' ? pub["dct:title"] : _(pub["dct:title"]).first();

        model["author"] = pub["dct:contributor"] ? pub["dct:contributor"] : [];
        model["abstract"] = pub["bibo:abstract"] ? (typeof pub["bibo:abstract"] === 'string' ? pub["bibo:abstract"] : _(pub["bibo:abstract"]).first()) : "";
        model["uri"] = typeof pub["bibo:uri"] === 'string' ? pub["bibo:uri"] : (_.some(pub["bibo:uri"], function(value) {
          return _(value).has('@id');
        }) ? _.first(pub["bibo:uri"]) : "");

        $scope.autores = [];
        var cont = 0;
        _.map(pub["dct:contributors"], function(authorid) {
          cont = cont + 1;
          var authorresource = authorid["@id"] ? (_.findWhere(autores, {
            "@id": authorid["@id"]
          })) : (_.findWhere(autores, {
            "@id": authorid
          }));
          var name = typeof authorresource["foaf:name"] === 'string' ? authorresource["foaf:name"] : _(authorresource["foaf:name"]).first();
          $scope.autores.push({
            id: authorresource["@id"],
            name: name
          });
        });

        if (model["title"]) {
          $scope.todos.push({
            id: model["id"],
            title: model["title"],
            abstract: model["abstract"],
            uri: model["uri"],
            author: $scope.autores
          });
        }
      });
      $('html,body').animate({
        scrollTop: $("#scrollToHere").offset().top
      }, "slow");
      $scope.loadData();
    };
    $scope.loadData = function() {
      $scope.$apply(function() {
        $scope.filteredTodos = [], $scope.currentPage = 1, $scope.numPerPage = 10, $scope.maxSize = 5;
        $scope.$watch('currentPage + numPerPage', function() {
          var begin = (($scope.currentPage - 1) * $scope.numPerPage),
            end = begin + $scope.numPerPage;
          $scope.filteredTodos = $scope.todos.slice(begin, end);
        });
      });
    };

    if (!searchData.allkeywordsCloud) // if no load data by default
    {
      waitingDialog.show();
      var queryKeywords = globalData.PREFIX
            + 'CONSTRUCT {  ?keyword rdfs:label ?k;  uc:total ?totalPub }  '
            + 'FROM <' + globalData.centralGraph + '>  '
            + 'WHERE {      '
            + '  SELECT ?keyword (SAMPLE(?label) as ?k) (COUNT(DISTINCT ?publication) AS ?totalPub)      '
            + '  WHERE {          '
            + '    ?person foaf:publications ?publication.          '
            + '    ?publication dcterms:subject  ?keyword.         '
            + '    ?keyword rdfs:label ?label .      '
            + '  }      GROUP BY ?keyword  LIMIT 50'
            + '}';

      sparqlQuery.querySrv({
        query: queryKeywords
      }, function(rdf) {
        jsonld.compact(rdf, globalData.CONTEXT, function(err, compacted) {
          $scope.$apply(function() {
            $scope.data = {
              schema: {
                "context": globalData.CONTEXT,
                fields: ["rdfs:label", "uc:total"]
              },
              data: compacted
            };
            searchData.allkeywordsCloud = {
              schema: {
                "context": globalData.CONTEXT,
                fields: ["rdfs:label", "uc:total"]
              },
              data: compacted
            };
            waitingDialog.hide();
          });
        });
      });
    } else {
      $scope.data = searchData.allkeywordsCloud;
    }


    $scope.$watch('selectedItem', function() { //Funcion para cuando se selecciona la Research Area
      // $scope.selectedItemChange = function (item) {
      if ($scope.selectedItem != undefined && $scope.selectedItem.length > 0) {
        waitingDialog.show();
        var queryKeywords = globalData.PREFIX +
          'CONSTRUCT {  ?keyword_ rdfs:label ?lbl;  uc:total ?totalPub  }  ' +
          'WHERE  {  ' +
          'SELECT DISTINCT ?keyword_ (SAMPLE(?keyword) as ?lbl) (COUNT(DISTINCT(?publications)) AS ?totalPub) ' +
          '  WHERE {     ' +
          '    graph <' + globalData.centralGraph + '>         {         ' +
          '    	?publications dcterms:subject ?keyword_.         ' +
          '    	?keyword_ rdfs:label ?keyword' +
          '  	} ' +
          '    GRAPH <' + globalData.clustersGraph + '> {' +
          '    	?area rdfs:label ?label;' +
          '              foaf:publications ?publications.' +
          '      	FILTER REGEX(?label, "' + $scope.selectedItem + '")' +
          '    }' +
          ' }  GROUP BY ?keyword_  LIMIT 50' +
          '}';

        sparqlQuery.querySrv({
          query: queryKeywords
        }, function(rdf) {
          jsonld.compact(rdf, globalData.CONTEXT, function(err, compacted) {
            $scope.$apply(function() {
              $scope.data = {
                schema: {
                  "context": globalData.CONTEXT,
                  fields: ["rdfs:label", "uc:total"]
                },
                data: compacted
              };
              waitingDialog.hide();
            });
          });
        });
      } else {
        $scope.data = searchData.allkeywordsCloud;
      }
    });

    //Function that displays the buttons to export the report
    $scope.exportReport = function(id) {
      $scope.keyw = id;
      $scope.showRepButtons = true;
    };

  }
]);
