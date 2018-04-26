
//var authorprof = angular.module('authorProfile', []);
//  d3  Factory



wkhomeApp.directive('profileTemplate', [ 'globalData','sparqlQuery', '$routeParams' , '$window' ,
    function ( globalData, sparqlQuery , $routeParams, $window ) {
                
                
           return {
                restrict: 'E',
                require: '^ngModel',
                scope: {
                ngModel: '='
                },
            //  template: 'HOLA : {{ngModel.name}}',
         templateUrl: 'wkhome/partials/profileTemplate.html',
    link: function(scope, iElement, iAttrs, ctrl) {
        console.log("DIrectiva");
    }
  };
        }
    ]);


wkhomeApp.directive('coauthorTemplate', [ 'globalData','sparqlQuery', '$routeParams' , '$window' ,
    function ( globalData, sparqlQuery , $routeParams, $window ) {
                
                
           return {
                restrict: 'E',
                require: '^ngModel',
                scope: {
                ngModel: '=',
                clickonRelatedauthor: '&'
                },
             // template: ' Hola {{ngModel.authorname}} chao',
             template: ' <ul> <li class="listCoauthors" ng-repeat="coauthor in ngModel.data" > <a target="blank" ng-click="clickonRelatedauthor({uri: coauthor.id });"> <img src="{{coauthor.img}}" class="img-rounded" alt="Logo Cedia" width="30" height="30">  {{coauthor.authorname}} </a> </li> </ul>  ',
    link: function(scope, iElement, iAttrs, ctrl) {
   
        console.log(scope);
    }
  };
        }
    ]);