wkhomeControllers.controller('keywordsCloud', ['$translate', '$routeParams', '$scope', 'globalData', 'sparqlQuery', 'searchData', '$window', 'Statistics', 'clustersTotals', 'subclustersTotals', '$translate' , '$rootScope' ,
    function ($translate, $routeParams, $scope, globalData, sparqlQuery, searchData, $window, Statistics, clustersTotals, subclustersTotals , $translate , $rootScope) {
       /* $("html, body").animate({
            scrollTop: 0
        }, 'slow', 'swing');*/

       /* $scope.selectedItem = undefined;
         $scope.data = [];
        Statistics.query({
            id: 'keywords_frequencypub_gt4'
        }, function (data) {
            $scope.areas = [];
            _.map(data["@graph"], function (keyword) {
                $scope.areas.push({
                    label: keyword["rdfs:label"]["@value"],
                    id: keyword["@id"]
                });
            });
        });*/
        var language = $translate.use();
          $scope.data = [];
          $scope.selectedItem = undefined;

       $rootScope.$on('$translateChangeSuccess', function(event, current, previous) {
        language = $translate.use();
        loadCombo ();
        });


         loadCombo ();

         function loadCombo () {
    Statistics.query({
      id: 'keywords_frequencypub_gt4'
    }, function (data) {
       language = $translate.use();
      $scope.relatedtags = [];

       console.log (data["@graph"])
       $scope.areas = [];
      _.map(data["@graph"], function (keyword) {
        
        array = keyword["rdfs:label"]
        var lan = {};
        lan[array[0]['@language']] = array[0]['@value'] ;
        lan[array[1]['@language']] = array[1]['@value'] ;
        
        var ims = {
          id: keyword["@id"],
          label: lan[language]
        };
        $scope.areas.push(ims);

      });

    });

    }

        renderAll ();




           function renderAll () {
                   waitingDialog.show();


            clustersTotals.query({}, function (res) {
         
                        _.map(res, function (area) {
                            var label = "";
                            if (language == "es" && area["labeles"]  ) {
                                label = "labeles";
                            } else {
                                label = "labelen";
                            }

                            $scope.data.push({
                                id: area["area"],
                                label: area[label],
                                value: area["totalAuthors"]
                            });
                        });
                        waitingDialog.hide();
            });
              }
         

    

       $scope.changeComboSelected  = function() {
                if ($scope.selectedItem != undefined && $scope.selectedItem.length > 0) {
               
               renderSub ();

            } 
            else {
                renderAll ();
            }

       }

       function renderSub () {
       /* waitingDialog.show();*/
             subclustersTotals.query({id: $scope.selectedItem}, function (res) {
               
                            $scope.data = [];
                            _.map(res, function (area) {
                                var label = "";
                                if (language == "es" && area["labeles"]  ) {
                                    label = "labeles";
                                } else {
                                    label = "labelen";
                                }
                                $scope.data.push({
                                    id: area["sc"],
                                    label: area[label],
                                    value: area["totalAuthors"]
                                });
                            });
                         /*   waitingDialog.hide();*/
                        });

       }

         $scope.$watch('selectedItem', function () { 
           renderSub ();
         });

     



    }
]);
