wkhomeApp.directive('profileTemplatedataset', ['globalData', 'sparqlQuery', '$routeParams', '$window',
  function(globalData, sparqlQuery, $routeParams, $window) {


    return {
      restrict: 'E',
      require: '^ngModel',
      scope: {
        ngModel: '='
      },
      //  template: 'HOLA : {{ngModel.name}}',
      templateUrl: 'wkhome/partials/profileTemplatedataset.html',
      link: function(scope, iElement, iAttrs, ctrl) {
        // console.log("DIrectiva");
      }
    };
  }
]);
