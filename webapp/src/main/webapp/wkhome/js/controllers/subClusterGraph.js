wkhomeControllers.controller('subCluster', ['$scope', '$window', 'globalData', 'sparqlQuery', 'searchData', '$routeParams', 'Statistics', 'querySubcluster', 'reportService2', '$sce',
  function ($scope, $window, globalData, sparqlQuery, searchData, $routeParams, Statistics, querySubcluster, reportService2, $sce) {

    var cluster = $routeParams.cluster;
    var subcluster = $routeParams.subcluster;
    //  $scope.areaCombosub =  { "tag":"12313"}; 
    $scope.areaCombo = {};
    Statistics.query({
      id: 'keywords_frequencypub_gt4'
    }, function (data) {
      $scope.relatedtags = [];
      _.map(data["@graph"], function (keyword) {
        $scope.relatedtags.push({
          id: keyword["@id"],
          tag: keyword["rdfs:label"]["@value"]
        });
      });
    });

    $scope.changeCombo = function () {
      $scope.datacl = {};

      $scope.areaCombosub = {};
      $scope.datacl = {
        cluster: $scope.areaCombo.selected.id,
        subcluster: null
      };
      querySubcluster.query({
        id: $scope.areaCombo.selected.id
      }, function (data) {
        $scope.subtags = [];
        _.map(data.subclusters, function (keyword) {
          $scope.subtags.push({
            id: keyword["uri"],
            tag: keyword["label-en"]
          });
        });
      });
    }

    $scope.exportReport = function (d) {

      var cc = $scope.areaCombo.selected.id;
      var cc_ = $scope.areaCombo.selected.tag
      var sc = $scope.areaCombosub.selected ? $scope.areaCombosub.selected.id : undefined;
      var sc_ = $scope.areaCombosub.selected ? $scope.areaCombosub.selected.tag : undefined;
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
      $scope.datacl = {};
      $scope.datacl = {
        cluster: $scope.areaCombo.selected.id,
        subcluster: $scope.areaCombosub.selected.id
      };
      console.log($scope.areaCombosub);
    }

    if (cluster && subcluster) {
      $scope.areaCombo = cluster;
      $scope.changeCombo();
      $scope.areaCombosub = subcluster;
      $scope.changeComboSub();
    }

  }
]);
