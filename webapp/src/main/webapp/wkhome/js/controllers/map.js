
wkhomeControllers.controller('map', ['$routeParams', '$scope', '$window', 'globalData', 'sparqlQuery', 'searchData', 'Statistics',
    function ($routeParams, $scope, $window, globalData, sparqlQuery, searchData, Statistics) {
        //if click in pie-chart
        $scope.ifClick = function (value) {
            searchData.genericData = value;
            $window.location.hash = "/" + $routeParams.lang + "/w/cloud?" + "datacloud";
        };
        // $scope.themes = [];

        Statistics.query({
          id: 'keywords_frequencypub_gt4'
        }, function(data) {
          $scope.relatedtags = [];
          _.map(data["@graph"], function(keyword) {
            $scope.relatedtags.push({
              tag: keyword["rdfs:label"]["@value"]
            });
          });
        });
        // if (!searchData.allkeywords)
        // {
        //     waitingDialog.show("Loading Research Areas");
        //     var queryKeywords = globalData.PREFIX
        //             + ' CONSTRUCT { ?keyword rdfs:label ?key } '
        //             + ' WHERE { '
        //             + '     SELECT  (count(?pubs) as ?total) ' //(SAMPLE(?keyword) as ?keywordp) (SAMPLE(?key) as ?keyp)  '
        //             + '     WHERE { '
        //             + '         graph <'+globalData.centralGraph+'> {'
        //             + '             ?subject foaf:publications ?pubs. '
        //             //+ '           ?subject dct:subject ?key. '
        //             + '             ?pubs dcterms:subject ?keywordSubject. '
        //             + '             ?keywordSubject rdfs:label ?key. '
        //             + '             BIND(REPLACE(?key, " ", "_", "i") AS ?unickey). '
        //             + '             BIND(IRI(?unickey) as ?keyword) '
        //             + '         }'
        //             + '     } '
        //             + '     GROUP BY ?keyword  ?key '
        //             //+ '     GROUP BY ?subject'
        //
        //             + '     HAVING(?total > 4) ' //si la keyword aparece en mas de 5 publicaciones
        //             + '}';
        //     sparqlQuery.querySrv({query: queryKeywords}, function (rdf) {
        //         jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
        //             _.map(compacted["@graph"], function (pub) {
        //                 var model = {};
        //                 model["id"] = pub["@id"];
        //                 model["tag"] = pub["rdfs:label"];
        //                 $scope.themes.push({tag: model["tag"]});
        //             });
        //             $scope.$apply(function () {
        //                 $scope.relatedtags = $scope.themes;
        //                 $scope.selectedTagItem = 'Semantic Web';
        //                 searchData.allkeywords = $scope.themes;
        //             });
        //             waitingDialog.hide();
        //         });
        //     });
        // }
        // else
        // {
        //     $scope.relatedtags = searchData.allkeywords;
        //     $scope.selectedTagItem = 'Semantic Web';
        // }



        //default selectedTagItem =  Semantic Web  - > see in app.js
        $scope.$watch('selectedTagItem', function () {
            //alert($scope.selectedItem);
            if ($scope.selectedTagItem) {

            waitingDialog.show("Consultando Ubicacion de Autores Relacionados con:  \"" + $scope.selectedTagItem + "\"");
            var queryBySource = globalData.PREFIX
                    + 'CONSTRUCT {            '
                    + '         ?org dcterms:subject ?label_;'
                    + '              uc:totalpublications ?totPub;'
                    + '              ?b ?c.'
                    + '} WHERE { '
                    + '  {	'
                    + '    SELECT ?org (count(DISTINCT ?publications) as ?totPub) (SAMPLE(?label) as ?label_)'
                    + '     WHERE {             '
                    + '       GRAPH <' + globalData.centralGraph + '>  {                   	'
                    + '         [] foaf:publications ?publications;'
                    + '                   schema:memberOf ?org.	'
                    + '       }    '
                    + '       GRAPH <' + globalData.clustersGraph + '> {   		'
                    + '         [] foaf:publications ?publications;'
                    + '                rdfs:label ?label.      '
                    + '         FILTER REGEX(?label, "' + $scope.selectedTagItem + '")    '
                    + '       }          '
                    + '     } GROUP BY ?org'
                    + '  }'
                    + '  {'
                    + '     GRAPH <' + globalData.organizationsGraph + '> {   		'
                    + '       ?org ?b ?c.                        '
                    + '     }    '
                    + '  }'
                    + '}';
                    // + 'CONSTRUCT {          '
                    // + '  ?org dcterms:subject ?label_;'
                    // + '              uc:totalpublications ?totPub;'
                    // + '              ?b_ ?c_.'
                    // + '} WHERE {     '
                    // + '  SELECT ?org ?area (count(DISTINCT ?publications) as ?totPub) (SAMPLE(?label) as ?label_) (SAMPLE(?b) as ?b_) (SAMPLE(?c) as ?c_)'
                    // + '  WHERE {         '
                    // + '    GRAPH <' + globalData.centralGraph + '>  {             '
                    // + '      	[] foaf:publications ?publications;'
                    // + '                 schema:memberOf ?org.'
                    // + '	}'
                    // + '    GRAPH <' + globalData.clustersGraph + '> {'
                    // + '   		?area foaf:publications ?publications;'
                    // + '              rdfs:label ?label.'
                    // + '      FILTER REGEX(?label, "' + $scope.selectedTagItem + '")'
                    // + '    } '
                    // + '    GRAPH <' + globalData.organizationsGraph + '> {'
                    // + '   		?org ?b ?c;'
                    // + '    }       '
                    // + '  } GROUP BY ?org ?area'
                    // + '}';

            $scope.publicationsBySource = [];
            sparqlQuery.querySrv({query: queryBySource},
            function (rdf) {
                jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                  debugger;
                    if (compacted["@graph"])
                    {
                        waitingDialog.hide();
                        var model = [];
                        _.map(compacted["@graph"], function (resource) {
                            var model = {};
                            model["id"] = resource["@id"];
                            model["name"] = resource["uc:name"];
                            model["fullname"] = _.findWhere(resource["uc:fullName"],{'@language': $routeParams.lang})['@value'];
                            model["total"] = resource["uc:totalpublications"]["@value"];
                            model["lat"] = resource["uc:latitude"];
                            model["long"] = resource["uc:longitude"];
                            model["keyword"] = resource["dct:subject"]["@value"];
                            model["city"] = resource["uc:city"];
                            model["province"] = resource["uc:province"];
                            if (model["id"]){
                                $scope.publicationsBySource.push({id: model["id"], name: model["name"], fullname: model["fullname"], total: model["total"], latitude: model["lat"]
                                    , longitude: model["long"], city: model["city"], province: model["province"], keyword: model["keyword"]});
                            }
                        });
                        $scope.$apply(function () {
                            $scope.data = $scope.publicationsBySource;
                        });
                    }
                    else
                    {
                        alert("Informacion no encontrada");
                        waitingDialog.hide();

                    }
                });
            });
        }
      });
    }]);
