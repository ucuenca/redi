
wkhomeApp.directive('tableTemplate', ['globalData', 'sparqlQuery', '$routeParams', '$window',
  function(globalData, sparqlQuery, $routeParams, $window) {


    return {
      restrict: 'E',
      scope: {
        data: '='
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