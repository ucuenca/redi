wkhomeControllers.controller('groupbyCloud', ['$translate', '$routeParams', '$scope', 'globalData', 'sparqlQuery', '$route', '$window', '$location', 'Statistics',
  function($translate, $routeParams, $scope, globalData, sparqlQuery, $route, $window, $location, Statistics) {
    $translate.use($routeParams.lang);
    $scope.selectedArea = $routeParams.area;

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

    $scope.ifClick = function(value) {
      $scope.todos = [];
      var model = {};
      _.map(value, function(pub) {

        model["id"] = pub["@id"];
        model["title"] = typeof pub["dct:title"] === 'string' ? pub["dct:title"] : _(pub["dct:title"]).first();
        model["abstract"] = typeof pub["bibo:abstract"] === 'string' ? pub["bibo:abstract"] : _(pub["bibo:abstract"]).first();
        model["uri"] = pub["bibo:uri"] ? (_.isArray(pub["bibo:uri"]) ? _(pub["bibo:uri"]).first() : pub["bibo:uri"]) : undefined;
        model["keywords"] = pub["dct:subject"] ? pub["dct:subject"] : undefined;
        $scope.todos.push({
          id: model["id"],
          title: model["title"],
          abstract: model["abstract"],
          uri: model["uri"],
          keywords: model["keywords"]
        });
      });

      $scope.loadData();
      $('html,body').animate({
        scrollTop: $("#scrollToHere").offset().top
      }, "slow");
    };

    $scope.querySearch   = function (query) {
      var some = $scope.areas;
      var lowercaseQuery = angular.lowercase(query);

      if (query) {
        some = $scope.areas.filter(function(area){
            return (angular.lowercase(area.tag).indexOf(lowercaseQuery) !== -1);
        });
      }
      return _(some).pluck('tag');
    };

    $scope.$watch('selectedArea', function() { //Funcion para cuando se selecciona la Research Area
      if ($scope.selectedArea) {
        $location.search({area:$scope.selectedArea}); // set url params dynamically
        $scope.todos = [];
        $scope.filteredTodos = [];
        loadAuthors($scope.selectedArea, 'organization'); //query and load resource related with selected theme
        // loadAuthors($scope.selectedArea, $scope.gbselectedItem); //query and load resource related with selected theme
        var authorInfo = $('div.tree-node-author-info .authorsByClusters');
        authorInfo.html('');
        authorInfo = $('div.tree-node-author-info .authorsByPublications');
        authorInfo.html('');
        var title = $('div#scrollToHere.col-md-12 div.col-md-12.head-info');
        title.html('');
      }
    });

    function loadAuthors(area, groupby) {
      waitingDialog.show("Consultando Autores Relacionados con:  \"" + area + "\"");

      var queryRelatedPublications = globalData.PREFIX
          + 'CONSTRUCT {  ?author foaf:name  ?authorName;          dct:provenance ?orgName .}'
          + 'WHERE { '
          + '  GRAPH <' + globalData.clustersGraph + '> {    '
          + '    ?area rdfs:label ?label;'
          + '                foaf:publications [uc:hasPerson ?author].'
          + '    FILTER REGEX(?label, "' + area + '")'
          + '  }'
          + '  GRAPH <' + globalData.centralGraph + '> {'
          + '    ?author foaf:name ?authorName;'
          + '            schema:memberOf ?org.'
          + '  }'
          + '  GRAPH <' + globalData.organizationsGraph + '> {'
          + '    ?org uc:name ?orgName.'
          + '  }'
          + '} ';

      sparqlQuery.querySrv({
        query: queryRelatedPublications
      }, function(rdf) {
        jsonld.compact(rdf, globalData.CONTEXT, function(err, compacted) {
          if (compacted["@graph"]) {
            // Map authors, and when there's an author with many institutions, duplicate author.
            var authors = _.chain(compacted["@graph"])
            .map(function(pub) {
              var name = _.isArray(pub["foaf:name"]) ? _(pub["foaf:name"]).first() : pub["foaf:name"];
              if (pub["dct:provenance"] instanceof Array) {
                  var aux = [];
                  _(pub["dct:provenance"]).each(function (org){
                    aux.push({
                      id: pub["@id"],
                      name: name,
                      organization: org
                    })
                  });
                  return aux;
              } else {
                return {
                  id: pub["@id"],
                  name: name,
                  organization: pub["dct:provenance"]
                };
              }
            })
            .flatten()
            .uniq(function (item) {return item.id + item.organization;})
            .value();
            executeDraw(authors, groupby);
            waitingDialog.hide();
            $('html,body').animate({
              scrollTop: $("#scrollToTop").offset().top
            }, "slow");
          } else {
            alert("No se han recuperado datos");
            waitingDialog.hide();
          }
        });
      });
    }

    function executeDraw(dataToDraw, groupby) {
      $scope.$apply(function() {
        $scope.data = [{
          value: dataToDraw,
          group: groupby
        }];
      });
    }

    //PUBLICATION INFORMATION
    $scope.todos = [];
    $scope.loadData = function() {
      $scope.filteredTodos = [], $scope.currentPage = 1, $scope.numPerPage = 10, $scope.maxSize = 5;
      $scope.$watch('currentPage + numPerPage', function() {
        var begin = (($scope.currentPage - 1) * $scope.numPerPage),
          end = begin + $scope.numPerPage;
        $scope.filteredTodos = $scope.todos.slice(begin, end);
      });
    };

    //Function that displays the buttons to export the report
    $scope.exportReport = function(id) {
      $scope.author = id;
      $scope.showRepButtons = true;
    };
  }
]); //end groupTagsController
