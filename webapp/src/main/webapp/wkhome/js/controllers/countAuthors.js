
wkhomeControllers.controller('countAuthors', ['$translate', '$routeParams', '$scope', '$window', 'globalData', 'sparqlQuery', 'searchData', 'Statistics',
    function ($translate, $routeParams, $scope, $window, globalData, sparqlQuery, searchData, Statistics) {

        //$window.location.hash = "/es/";

        $translate.use($routeParams.lang);

        //if click in pie-chart (Authors)
        $scope.ifClick = function (value)
        {
            searchData.genericData = value;
            $window.location.hash = "/" + $routeParams.lang + "/w/cloud?" + "datacloud";
        };

        Statistics.query({id: 'count_authors'}, function (data) {
          var endpoints = data['@graph'];
          var dataToSend = []
          if (endpoints) {
              endpoints.forEach(function (endpoint) {
                  dataToSend.push({label: endpoint['uc:name'], value: endpoint['uc:total']['@value']});
              });
                  $scope.data = {'entityName': 'Authors', 'data': dataToSend};
          }
        });

//*****************************************//
//*********FOR TRANSLATE*******************//
        //  $scope.translate = globalData.translateData; //query and load resource related with selected theme
        $scope.$watch('globalData.language', function (newValue, oldValue, scope) {
            //alert($scope.selectedItem);
            $scope.translate = globalData.translateData; //query and load resource related with selected theme
        });



    }]);
