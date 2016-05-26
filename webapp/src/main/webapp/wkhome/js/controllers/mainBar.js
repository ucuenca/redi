

wkhomeControllers.controller('mainBar', ['$translate', '$routeParams', '$scope', '$window', 'globalData', 'sparqlQuery', 'searchData',
    function ($translate, $routeParams, $scope, $window, globalData, sparqlQuery, searchData) {
       $translate.use($routeParams.lang);
       $scope.lang = globalData.language;
        $scope.$watch('globalData.language', function(){
            $scope.lang = globalData.language;
        });
    }]);
