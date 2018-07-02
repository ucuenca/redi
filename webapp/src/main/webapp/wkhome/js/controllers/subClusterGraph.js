wkhomeControllers.controller('subCluster', ['$scope', '$window', 'globalData', 'sparqlQuery', 'searchData', '$routeParams', 'Statistics', 'querySubcluster' ,
    function ($scope, $window, globalData,   sparqlQuery, searchData , $routeParams , Statistics , querySubcluster) {
        //$scope.todos = [];

         var host = "http://localhost:8080";
         var authorURI = $routeParams;

      	Statistics.query({
          id: 'keywords_frequencypub_gt4'
        }, function(data) {
          $scope.relatedtags = [];
          _.map(data["@graph"], function(keyword) {
            $scope.relatedtags.push({
              id : keyword["@id"],
              tag: keyword["rdfs:label"]["@value"]
            });
          });
        });

              

     /*    $scope.$watch('areaCombo', function () { 
            alert ("Change");
           console.log ($scope.areaCombo);
          });*/
         $scope.changeCombo = function () {
         	querySubcluster.query({
          id: $scope.areaCombo
        }, function(data) {
          $scope.subtags = [];
          _.map(data.subclusters , function(keyword) {
            $scope.subtags.push({
              id : keyword["uri"],
              tag: keyword["label-en"]
            });
          });
        });
         }

         $scope.changeComboSub = function () {
         	    //$scope.$apply(function () {
         	    	$scope.datacl = {} ;
         	    	$scope.datacl = {
         	    	  	  cluster : $scope.areaCombo ,
         	    	  	  subcluster: $scope.areaCombosub
         	    	  };                          
         	    	   
                  //      });

         	//$scope.draw ($scope.areaCombo , $scope.areaCombosub); 
         }

         console.log ("COMBO");
         console.log($scope.areaCombo) ;
         console.log($scope) ;
     /*  console.log ("Cargando controller subcluster" );
       console.log ($routeParams.area);
       console.log ($routeParams.subarea);*/

    }]); //end genericcloudController
