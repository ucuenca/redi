wkhomeControllers.controller('colProject', ['$scope', '$window', 'globalData', 'sparqlQuery', 'searchData', '$routeParams', 'Statistics', 'querySubcluster', 'reportService2', '$sce',
  function ($scope, $window, globalData, sparqlQuery, searchData, $routeParams, Statistics, querySubcluster, reportService2, $sce) {
  	 $scope.orgscombo = {};
  	 $scope.datacl = {};
  	 $scope.changeCombo = function () {

      console.log ("seleccionado" , $scope.orgscombo.selected.id );
       $scope.datacl = {};
       $scope.datacl.id = $scope.orgscombo.selected.id;
         
      }

     Statistics.query({
      id: 'barchar'
    }, function(data) {
    	var organizationdata = _.map(data["@graph"], function (item) {
         return  {
          id: item["@id"],
          tag: item["uc:name"]
        };});       


        $scope.orgs = organizationdata;
    });

  }
]);