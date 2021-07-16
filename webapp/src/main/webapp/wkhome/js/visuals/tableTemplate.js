
wkhomeApp.directive('tableTemplate', ['globalData', 'sparqlQuery', '$routeParams', '$window',
  function(globalData, sparqlQuery, $routeParams, $window) {


    return {
      restrict: 'E',
      scope: {
        data: '=',
        columnstr: '@',
        columns: '=?'
      },
      controller: function($scope){
        // check if it was defined.  If not - set a default
        if (angular.isDefined($scope.columnstr)) {
            $scope.columns = $scope.columnstr.split(",");
        }
        $scope.columns = angular.isDefined($scope.columns) ? $scope.columns : ['Name', 'Value'];
      },
      // template: 'HOLA : {{data}}',
      templateUrl: 'wkhome/partials/table.html',
      link: function(scope, iElement, iAttrs, ctrl) {
        /*console.log ("DATOS");
      console.log (scope);
      console.log (data);
         $('#example').DataTable( {
            data: data ,
            columns: [
              { title: "Name" },
              { title: "Value" },

            ]
          } );*/
      }
    };
  }
]);