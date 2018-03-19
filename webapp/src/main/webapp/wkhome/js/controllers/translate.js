wkhomeControllers.controller('translate', ['sparqlQuery', 'searchData', '$translate', '$routeParams', '$scope', '$window', 'translateService', 'globalData',
    function (sparqlQuery, searchData, $translate, $routeParams, $scope, $window, translateService, globalData) {
 /*       $translate.use($routeParams.lang);

        $scope.lang = globalData.language;

        $scope.setLanguage = function (value) {
            globalData.language = value;
            $scope.lang = globalData.language;
            if ($routeParams.lang === 'es' && value == 'en') {
                $window.location.hash = $window.location.hash.replace('/es/', '/en/');
            }
            if ($routeParams.lang === 'en' && value == 'es') {
                $window.location.hash = $window.location.hash.replace('/en/', '/es/');
            }
        };

        $scope.refreshLang = function () {
            $scope.lang = $routeParams.lang;
        };*/


    }]); //end translate controller
