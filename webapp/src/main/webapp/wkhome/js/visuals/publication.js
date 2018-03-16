// TODO: build directive for authors publications list.
// .directive('authorPublications', function () {
//
//   function link(scope, element, attrs, controllers) {
//     debugger;
//     console.log(scope);
//   }
//
//   return {
//     restrict: 'E',
//     scope: {
//       coauthors: '=showCoauthors',
//       type: '=showType',
//       author: '@',
//       core: '@',
//     },
//     link: link,
//     scope: {},
//     templateUrl: 'wkhome/partials/publication.html'
//   }
// })

wkhomeApp.directive('logoPublication', function(){

  function classifyURLS(scope, element, attrs, controllers){
    scope.source = "source";
    scope.type = "web";
    if (scope.uri.indexOf(".pdf") !== -1) {
      scope.type = "pdf";
    }
  }

  return {
    restrict: 'E',
    scope: {
      uri: '@'
    },
    link: classifyURLS,
    template: '<a href="{{uri}}" target="_blank"><span><img ng-src="wkhome/images/{{type}}_source.png" style="float:none; margin: 0 10px 0 0;"/>{{source}}</span></a>'
  };
});
