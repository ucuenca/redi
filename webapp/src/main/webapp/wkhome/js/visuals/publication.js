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
/*
wkhomeApp.directive('publicationTemplate', ['globalData', 'sparqlQuery', '$routeParams', '$window',
  function(globalData, sparqlQuery, $routeParams, $window) {


    return {
      restrict: 'E',
      require: '^ngModel',
      scope: {
        ngModel: '='
      },
       template: ' <h2><b>Hola <b> </h2> chao',
     // template: ' <ul> <li class="listCoauthors" ng-repeat="coauthor in ngModel.data" > <a target="blank" ng-click="clickonRelatedauthor({uri: coauthor.id });"> <img ng-src="{{coauthor.img}}" class="img-rounded" alt="Logo Cedia" width="30" height="30">  {{coauthor.authorname}} </a> </li> </ul>  ',
      link: function(scope, iElement, iAttrs, ctrl) {

        // console.log(scope);
      }
    };
  }
]);*/

wkhomeApp.directive('publicationTemplate', ['globalData', 'sparqlQuery', '$routeParams', '$window',
  function(globalData, sparqlQuery, $routeParams, $window) {


    return {
      restrict: 'E',
      require: '^ngModel',
      scope: {
        ngModel: '='
      },
      //  template: 'HOLA : {{ngModel.name}}',
      templateUrl: 'wkhome/partials/publicationProfile.html',
      link: function(scope, iElement, iAttrs, ctrl) {
        // console.log("DIrectiva");
      }
    };
  }
]);



wkhomeApp.directive('logoPublication', function(){

  function classifyURLS(scope, element, attrs, controllers){
    scope.source = "source";
    scope.type = "web";
    if (scope.uri.indexOf(".pdf") !== -1) {
      scope.type = "pdf";
    } else if (scope.uri.indexOf("www.scopus.com") !== -1){
      scope.type = "scopus";
    } else if (scope.uri.indexOf("ieee.org") !== -1){
      scope.type = "ieee";
    } else if (scope.uri.indexOf("springer.com") !== -1){
      scope.type = "springer";
    }  else if (scope.uri.indexOf("dblp") !== -1){
      scope.type = "dblp";
    } else if (scope.uri.indexOf("dspace") !== -1){
      scope.type = "dspace";
    } else if (scope.uri.indexOf("www.sciencedirect.com") !== -1){
      scope.type = "science_direct";
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
