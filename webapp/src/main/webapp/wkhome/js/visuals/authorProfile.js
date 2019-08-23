//var authorprof = angular.module('authorProfile', []);
//  d3  Factory



wkhomeApp.directive('profileTemplate', ['globalData', 'sparqlQuery', '$routeParams', '$window',
  function(globalData, sparqlQuery, $routeParams, $window) {


    return {
      restrict: 'E',
      require: '^ngModel',
      scope: {
        ngModel: '='
      },
      //  template: 'HOLA : {{ngModel.name}}',
      templateUrl: 'wkhome/partials/profileTemplate.html',
      link: function(scope, iElement, iAttrs, ctrl) {
        // console.log("DIrectiva");
      }
    };
  }
]);


wkhomeApp.directive('coauthorTemplate', ['globalData', 'sparqlQuery', '$routeParams', '$window',
  function(globalData, sparqlQuery, $routeParams, $window) {


    return {
      restrict: 'E',
      require: '^ngModel',
      scope: {
        ngModel: '=',
        clickonRelatedauthor: '&'
      },
      // template: ' Hola {{ngModel.authorname}} chao',
      template: ' <ul> <li class="listCoauthors" ng-repeat="coauthor in ngModel.data" > <a target="blank" ng-click="clickonRelatedauthor({uri: coauthor.id });"> <img ng-src="{{coauthor.img}}" class="img-circle" alt="Logo Cedia" width="30" height="30">  {{coauthor.authorname}} </a> </li> </ul>  ',
      link: function(scope, iElement, iAttrs, ctrl) {

        // console.log(scope);
      }
    };
  }
]);

wkhomeApp.directive('logoProfile', function(){
  function classifyURLS(scope, element, attrs, controllers){
    scope.type = undefined;
    if (scope.uri.indexOf("academic.microsoft.com") !== -1) {
      scope.type = "academics";
    } else if (scope.uri.indexOf("www.scopus.com") !== -1){
      scope.type = "scopus";
    } else if (scope.uri.indexOf("scholar.google.com") !== -1){
      scope.type = "scholar";
    }
  }

  return {
    restrict: 'E',
    scope: {
      uri: '@'
    },
    link: classifyURLS,
    template: '<a href="{{uri}}" target="_blank"><span><img ng-src="wkhome/images/{{type}}_icon.png" width="17" ng-attr-title="{{type}} profile"/></span></a>'
  };
});
