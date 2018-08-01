
wkhomeControllers.controller('map', ['$routeParams', '$scope', '$window', 'globalData', 'sparqlQuery', 'searchData', 'Statistics', 'Countries' , 
    function ($routeParams, $scope, $window, globalData, sparqlQuery, searchData, Statistics, Countries) {
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
              id: keyword["@id"],
              value: keyword["rdfs:label"]["@value"]
            });
          });
        });
      //$scope.datamap = [];
    // $scope.mapOptions =  [];

     $scope.changeCountry = function() {
     $scope.datamap = $scope.mapOptions;
     //console.log ($scope.datamap);

    }

            Countries.query({
        }, function(data) {
           //  console.log ("Co Request");
           // console.log (data);
          $scope.countryoptions = [];
          _.map(data, function(keyword) {
            $scope.countryoptions.push({
              id: keyword["code"].toLowerCase(),
              value:   keyword["name"]
            });
          });
          $scope.mapOptions =  $scope.countryoptions[0].id;
           $scope.datamap =  $scope.countryoptions[0].id;
        });

        //default selectedTagItem =  Semantic Web  - > see in app.js
        $scope.$watch('selectedTagItem', function () {
            if ($scope.selectedTagItem) {
             var textcountry  = $("#country option:selected").text();
            waitingDialog.show("Consultando Ubicacion de Autores Relacionados con:  \"" + $scope.selectedTagItem + "\"");
            var queryBySource = globalData.PREFIX
                + 'CONSTRUCT {'
                + '  ?org dcterms:subject ?label_;'
                + '       uc:totalpublications ?totAuth;'
                + '       ?b ?c.'
                + '} WHERE {'
                + '  SELECT ?org ?b ?c  (count(DISTINCT ?author) as ?totAuth) (SAMPLE(?label) as ?label_)'
                + '  WHERE {   '
                + '    GRAPH  <' + globalData.clustersGraph + '>{   '
                + '      <' + $scope.selectedTagItem + '> a uc:Cluster;'
                + '           rdfs:label  ?label .'
                + '      ?author dct:isPartOf <' + $scope.selectedTagItem + '>;'
                + '               a foaf:Person.'
                + '    }'
                + '    GRAPH <' + globalData.centralGraph + '> {  '
                + '        ?author schema:memberOf  ?org .'
                + '    }'
                + '    GRAPH   <' + globalData.organizationsGraph + '> { '
                + '           ?org ?b ?c . ?org uc:country  ?co . FILTER  ( STR(?co) = "'+textcountry+'")'
                + '    }'
                + '  } GROUP BY ?org ?b ?c '
                + '}';

            $scope.publicationsBySource = [];
            sparqlQuery.querySrv({query: queryBySource},
            function (rdf) {
                jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {

                    if (compacted["@graph"])
                    {
                        waitingDialog.hide();
                        var model = [];
                        _.map(compacted["@graph"], function (resource) {
                            var model = {};
                            model["id"] = resource["@id"];
                            model["name"] = resource["uc:name"];
                            model["fullname"] = _.first(resource["uc:fullName"])['@value'];
                            model["total"] = resource["uc:totalpublications"]["@value"];
                            model["lat"] = resource["uc:latitude"];
                            model["long"] = resource["uc:longitude"];
                            model["keyword"] = resource["dct:subject"]["@value"];
                            model["city"] = resource["uc:city"];
                            model["province"] = resource["uc:province"];
                            if (model["id"]){
                                $scope.publicationsBySource.push({id: model["id"], name: model["name"], fullname: model["fullname"], z : parseFloat(model["total"]), lat : parseFloat(model["lat"])
                   , lon: parseFloat(model["long"]), city: model["city"], province: model["province"], keyword: model["keyword"]});
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
