wkhomeControllers.controller('subCluster', ['$scope', '$window', 'globalData', 'sparqlQuery', 'searchData', '$routeParams', 'Statistics', 'querySubcluster', 'reportService2', '$sce',
  function ($scope, $window, globalData, sparqlQuery, searchData, $routeParams, Statistics, querySubcluster, reportService2, $sce) {

     var cluster = $routeParams.cluster;
     var subcluster = $routeParams.subcluster;


    //  $scope.areaCombosub =  { "tag":"12313"}; 
    //tag: keyword["rdfs:label"]["@value"] == undefined ? keyword["rdfs:label"] : keyword["rdfs:label"]["@value"] 
    $scope.areaCombo = {};
    Statistics.query({
      id: 'keywords_frequencypub_gt4'
    }, function (data) {
      $scope.relatedtags = [];
      _.map(data["@graph"], function (keyword) {
        var ims = {
          id: keyword["@id"],
          tag: keyword["rdfs:label"]["@value"]
        };
        $scope.relatedtags.push(ims);
        if (cluster) {
            if (keyword["@id"] == cluster){
                $scope.areaCombo.selected = ims;
            }
        }
      });
    
      if (subcluster){
        $scope.changeCombo();        
      }

    });

    

    $scope.changeCombo = function () {

      $scope.areaCombosub = {};

      if (!subcluster){
        $scope.datacl = {};
        $scope.datacl = {
            cluster: $scope.areaCombo.selected.id,
            subcluster: null
         };
      }

      console.log ("seleccionado"+ $scope.areaCombo.selected)
      querySubcluster.query({
        id: $scope.areaCombo.selected.id
      }, function (data) {
        $scope.subtags = [];
        _.map(data.subclusters, function (keyword) {
            var imx = {
            id: keyword["uri"],
            tag: keyword["label-en"]
          };
          $scope.subtags.push(imx);

          if (subcluster){
            if (keyword["uri"] == subcluster){
                $scope.areaCombosub.selected = imx;
            }
          }
        });
            if (subcluster) {
                $scope.changeComboSub();
            }
      });
    }

    $scope.exportReport = function (d) {

      var cc = $scope.areaCombo.selected.id;
      var cc_ = $scope.areaCombo.selected.tag
      var sc = $scope.areaCombosub.selected && $scope.areaCombosub.selected.id ? $scope.areaCombosub.selected.id : undefined;
      var sc_ = $scope.areaCombosub.selected && $scope.areaCombosub.selected.id ? $scope.areaCombosub.selected.tag : undefined;
      $scope.loading = true;

      var prm = [];
      if (cc && sc) {
        prm = [cc, cc_, sc, sc_];
      } else {
        prm = [cc, cc_];
      }
      var params = {hostname: '', report: 'ReportAuthorCluster2', type: d, param1: prm};
      reportService2.search(params, function (response) {
        var res = '';
        for (var i = 0; i < Object.keys(response).length - 2; i++) {
          res += response[i];
        }
        if (res && res !== '' && res !== 'undefinedundefinedundefinedundefined') {
          $window.open($sce.trustAsResourceUrl($window.location.origin + res));
        } else {
          alert("Error al procesar el reporte. Por favor, espere un momento y vuelva a intentarlo. Si el error persiste, consulte al administrador del sistema.");
        }
        $scope.loading = false;
      });
    }

    $scope.selectedValue = function () {
      return $scope.datacl;
    }


    $scope.changeComboSub = function () {
      //$scope.datacl = {};
      $scope.datacl = {
        cluster: $scope.areaCombo.selected.id,
        subcluster: $scope.areaCombosub.selected.id
      };
      if (cluster) {
        cluster = undefined;
      }
      if (subcluster) {
        subcluster = undefined;
      }

    }
    if (cluster && subcluster ) {
      //console.log($scope.relatedtags);
      //$scope.areaCombo = { selected : {'id' : cluster, tag:''} };
      //$scope.changeCombo();
      //$scope.areaCombosub = { selected : {'id' : subcluster, tag:''}};
      //$scope.changeComboSub();
      

    }


   /* if (cluster && subcluster) {
      console.log (cluster)
      console.log (subcluster)
      console.log ($scope.areaCombo)
      $scope.areaCombo = cluster;
      $scope.changeCombo();
      $scope.areaCombosub = subcluster;
      $scope.changeComboSub();
    }*/



  }
]);